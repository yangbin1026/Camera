package com.monitor.bus.activity;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jniUtil.MyUtil;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.control.VideoPlayControl;
import com.monitor.bus.database.DatabaseHelper;
import com.monitor.bus.model.BusDeviceInfo;
import com.monitor.bus.view.MyVideoView;

/**
 * 实时视频Activity
 * 
 */
public class VideoActivity extends BaseActivity implements OnTouchListener {

	private static String TAG = "VideoActivity";
	private MyVideoView myVideoView;
	private Intent intent;
	private BusDeviceInfo currentDevData;// 设备信息
	private DatabaseHelper db;// 数据库操作对象
	String recordFilePath = null;// 当前录像文件存储路径
	String times = "";// 当前文件名称
	private TextView text_tilte_name;// 标题名称

	boolean isCapturePicture = false;//是否有操作过抓拍
	boolean record_state = false;// 当前录像状态标志 true 开始录像 false 停止录像
	private Button recordButton;// 录像按钮

	//boolean monitor_state = false;// 当前监听状态标志 true 开始监听 false 停止监听
	private Button monitorButton;// 监听按钮

	//boolean talk_state = false;// 当前对讲状态标志 true 开始对讲 false 停止对讲
	private Button talkButton;// 对讲按钮

	public Button mirrorButton;// 镜像按钮
 
	// 云台控制
	private ImageButton ptzUp;
	private ImageButton ptzDown;
	private ImageButton ptzLeft;
	private ImageButton ptzRight;
	public Timer recordTimer;// 计时器

	GestureDetector mGestureDetector;
	private VideoPlayControl playControl;
	
	private float x,y;
	private int mx,my;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Constants.STREAM_PLAY_TYPE=1;//设置播放类型为实时视频
		db = new DatabaseHelper(this, Constants.DATABASE_NAME);//初始化数据操作对象
		Constants.INSTANCE = this;
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 屏幕保持常亮
		intent = this.getIntent();
		currentDevData = (BusDeviceInfo) intent.getSerializableExtra("videoData");
		Log.i(TAG, currentDevData+"");
	}
	
	@Override
	protected void onResume() {
		setContentView(R.layout.video_stream);
		text_tilte_name = (TextView) findViewById(R.id.tilte_name);
		mirrorButton = (Button) findViewById(R.id.mirror_button);
		
		myVideoView = (MyVideoView) findViewById(R.id.myVideoView);
		playControl = new VideoPlayControl(this, myVideoView);
		myVideoView.setOnTouchListener(this);
		if (currentDevData != null) {// 标题栏设置: 设备名称-当前通道号
			text_tilte_name.setText(currentDevData.getDeviceName() + " - " + Constants.CHANNEL_PREFIX_NAME + currentDevData.getCurrentChn());
			mGestureDetector = new GestureDetector(new MySimpleGesture());
			myVideoView.setLongClickable(true);
		}
		/*-----------录像-------------*/
		recordButton = (Button) findViewById(R.id.record_button);
		recordButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (isNormalPlay()) {// 正常播放
					if (event.getAction() == MotionEvent.ACTION_UP) {// 手指抬起
						if (record_state) {// 录像已开启
							recordButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.close));
							record_state = false;
							try {
								// 停止录像
								stopRecordVideo();
							} catch (ParseException e) {
								e.printStackTrace();
								Log.e(TAG, "++++++++++录像失败！");
							}
						} else {// 录像已关闭

							// 重新设置按下时的背景图片
							recordButton.setBackgroundDrawable(getResources()
									.getDrawable(R.drawable.open));
							record_state = true;
							// 开始录像
							startRecordVideo();
						}
					}
				}
				return false;
			}
		});

		/*-----------监听------------ */
		monitorButton = (Button) findViewById(R.id.monitor_button);

		monitorButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (isNormalPlay()) {// 正常播放
					if (event.getAction() == MotionEvent.ACTION_UP) {// 手指抬起
						if (Constants.ISOPEN_AUDIO) {// 监听已开启
							monitorButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.close));
							Constants.ISOPEN_AUDIO = false;
							// 停止监听
							playControl.track.stop();// 停止音频
						} else {// 监听已关闭
							// 重新设置按下时的背景图片
							monitorButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.open));
							Constants.ISOPEN_AUDIO = true;
							// 开始监听
							playControl.track.play();// 启动本地音频读取
						}
					}
				}
				return false;
			}
		});
		/*-----------对讲------------*/
		talkButton = (Button) findViewById(R.id.talk_button);
		talkButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (isNormalPlay()) {// 正常播放
					if (event.getAction() == MotionEvent.ACTION_UP) {// 手指抬起
						if (Constants.ISOPEN_TALK) {// 对讲已开启
							talkButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.close));
							Constants.ISOPEN_TALK = false;
							// 停止对讲
							playControl.closeTalk();
							playControl.audioRecord.stop();
						} else {// 对讲已关闭

							// 重新设置按下时的背景图片
							talkButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.open));
							Constants.ISOPEN_TALK = true;
							// 打开对讲
							playControl.audioRecord.startRecording();// 开启录音
							playControl.openTalk();// 打开对讲
						}
					}
				}
				return false;
			}
		});

		/*-----------镜像------------*/
		
		mirrorButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (isNormalPlay()) {// 正常播放
					if (event.getAction() == MotionEvent.ACTION_UP) {// 手指抬起
						if (Constants.DERECTION_STATE) {// 正

							mirrorButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.close));
							// 重新设置按下时的背景图片
							Constants.DERECTION_STATE = false;
							playControl.AVP_SetMirror(Constants.MIRROE_REVERSE);
						} else {// 反
								// 重新设置按下时的背景图片
							Constants.DERECTION_STATE = true;
							mirrorButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.open));
							playControl.AVP_SetMirror(Constants.MIRROE_NORMAL);
						}
					}
				}
				return false;
			}
		});
		
		/*--------------云台管理-------------------------*/
		ptzUp = (ImageButton) findViewById(R.id.ptzUp);
		ptzDown = (ImageButton) findViewById(R.id.ptzDown);
		ptzLeft = (ImageButton) findViewById(R.id.ptzLeft);
		ptzRight = (ImageButton) findViewById(R.id.ptzRight);

		ptzUp.setOnTouchListener(new OnTouchListener() {// 云台-上

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					ptzUp.setBackgroundDrawable(getResources().getDrawable(R.drawable.up_1));
					upPTZCtrl();

				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					ptzUp.setBackgroundDrawable(getResources().getDrawable(R.drawable.up_2));
					stopPtzCtrl();
				}
				return false;
			}
		});

		ptzDown.setOnTouchListener(new OnTouchListener() {// 云台-下

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					ptzDown.setBackgroundDrawable(getResources().getDrawable(R.drawable.down_1));
					downPTZCtrl();

				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					ptzDown.setBackgroundDrawable(getResources().getDrawable(R.drawable.down_2));
					stopPtzCtrl();
				}
				return false;
			}
		});

		ptzLeft.setOnTouchListener(new OnTouchListener() {// 云台-左

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					ptzLeft.setBackgroundDrawable(getResources().getDrawable(R.drawable.left_1));
					leftPTZCtrl();

				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					ptzLeft.setBackgroundDrawable(getResources().getDrawable(R.drawable.left_2));
					stopPtzCtrl();
				}
				return false;
			}
		});

		ptzRight.setOnTouchListener(new OnTouchListener() {// 云台-右

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					ptzRight.setBackgroundDrawable(getResources().getDrawable(R.drawable.right_1));
					rightPTZCtrl();

				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					ptzRight.setBackgroundDrawable(getResources().getDrawable(R.drawable.right_2));
					stopPtzCtrl();
				}
				return false;
			}
		});
		playControl.initVideoPlay(intent);//初始化界面
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		if (record_state) {// 停止录像
			try {
				stopRecordVideo();
				record_state = false;
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		if (Constants.ISOPEN_AUDIO) {// 停止监听
			Constants.ISOPEN_AUDIO = false;
			playControl.track.stop();// 停止音频
		}
		playControl.track.release();
		playControl.track = null;
		

		if (Constants.ISOPEN_TALK) {
			Constants.ISOPEN_TALK = false;
			playControl.closeTalk();
			playControl.audioRecord.stop();
 
		}
		playControl.audioRecord.release();
		playControl.audioRecord = null;
		 
		playControl.stopStream();
		
		
		
		myVideoView.is_drawblack = true;
		myVideoView.flag = -1;
		myVideoView.videoWidth = 0;
		myVideoView.videoHeight = 0;
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if(isCapturePicture){
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, 
					Uri.parse("file://"+ Environment.getExternalStorageDirectory())));// 刷新相册环境
			Log.i(TAG, "++++++++++onDestroy！");
		}
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		if(1 == Constants.FLAG_FULLSCREEN){
			changeScreenOrientation();
			return;
		}else{
			super.onBackPressed();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	

	/**
	 * 实时视频播放--------暂时无用
	 * 
	 * @param view
	 */
	public void playVideo(View view) {
		//playControl.startStream();
	}

	/**
	 * 实时视频停止--------暂时无用
	 * 
	 * @param view
	 */
	public void stopVideo(View view) {
		if (!isNormalPlay()) {
			return;
		}
		playControl.stopStream();
	}

	/**
	 * 进入设备列表
	 * 
	 * @param view
	 */
	public void goDeviceListActivity(View view) {
		Intent intent = new Intent();
		intent.setClass(this, BusDeviceList.class);
		//intent.putExtra("busDevData", currentDevData);
		startActivity(intent);
		finish();
	}

	/**
	 * 录像
	 * 
	 * @param view
	 * @throws ParseException
	 */
	public void startRecordVideo() {
		if (!isNormalPlay()) {
			return;
		}
		recordFilePath = MyUtil.getCurrentFilePath(Constants.RECORD_FILE_PATH,
				currentDevData);
		File file = new File(recordFilePath);
		if (!file.exists()) {// 目录不存在
			file.mkdirs();
		}
		times = MyUtil.getCurrentDateTime(Constants.YMD_HMS_FORMAT);
		String path = recordFilePath + times + Constants.RECORD_FILE_FORMAT;
		Log.i(TAG, "-------------开始录像！");
		int i = playControl.RecordStart(path);
		if (i == 0) {
			MyUtil.commonToast(this, R.string.record_fail);
			return;
		}

		// 创建定时器
		recordTimer = new Timer();
		recordTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Log.i(TAG, "+++++++++++++录像线程ID："+ Thread.currentThread().getId()+"+++++++++名称："+Thread.currentThread().getName());
				if (myVideoView.is_draw_circle) {
					myVideoView.is_draw_circle = false;
				} else {
					myVideoView.is_draw_circle = true;
				}

			}
		}, 0, 1000);
	}

	/**
	 * 停止录像
	 * 
	 * @throws ParseException
	 */
	public void stopRecordVideo() throws ParseException {
		Log.i(TAG, "-------------停止录像！");
		int i = playControl.RecordStop();
		if (i == 1) {
			db.insertRecordInfo(times, times + Constants.RECORD_FILE_FORMAT,recordFilePath);
		} else {
			Log.e(TAG, "-------------停止录像失败！");
			MyUtil.commonToast(this, R.string.stop_recordfail);
			return;
		}
		myVideoView.is_draw_circle = false;
		recordTimer.cancel();
	}

	/**
	 * 云台控制-上
	 * 
	 * @param view
	 */
	public void upPTZCtrl() {
		if (!isNormalPlay()) {
			return;

		}
		playControl.PTZCtrl(Constants.PTZ_DERECTION.PTZ_UP);

	}

	/**
	 * 云台控制-下
	 */
	public void downPTZCtrl() {
		if (!isNormalPlay()) {
			return;
		}
		playControl.PTZCtrl(Constants.PTZ_DERECTION.PTZ_DOWN);
	}

	/**
	 * 云台控制-左
	 */
	public void leftPTZCtrl() {
		if (!isNormalPlay()) {
			return;
		}

	playControl.PTZCtrl(Constants.PTZ_DERECTION.PTZ_LEFT);
	}

	/**
	 * 云台控制-右
	 */
	public void rightPTZCtrl() {
		if (!isNormalPlay()) {
			return;
		}
		playControl.PTZCtrl(Constants.PTZ_DERECTION.PTZ_RIGHT);
	}

	/**
	 * 云台控制-停止
	 * 
	 * @param view
	 */
	public void stopPtzCtrl() {
		if (!isNormalPlay()) {
			return;
		}
		playControl.PTZCtrl(Constants.PTZ_DERECTION.PTZ_STOP);
	}

	/**
	 * 抓拍
	 * 
	 * @param view
	 * @throws IOException 
	 */
	public void videoCapture(View view) throws IOException {
		if (!isNormalPlay()) {
			return;
		}
		playControl.CapturePicture();
		isCapturePicture = true;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}
	
	/*
	@Override
	protected void onResume() {
		super.onResume();

	}*/

	/**
	 * 判断是否正常播放
	 * 
	 * @param currentDevData
	 *            播放数据
	 * @param stop
	 *            停止实时视频返回的值
	 * @return
	 */
	public boolean isNormalPlay() {
		if (myVideoView.is_drawblack || -1 == myVideoView.flag) {
			return false;
		}
		return true;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			x = event.getX();
			y = event.getY();
			break;
		case MotionEvent.ACTION_UP:
			stopPtzCtrl();
			break;
		case MotionEvent.ACTION_MOVE:
			mx = (int) (event.getRawX() - x);
			my = (int) (event.getRawY() - y);
			v.layout(mx, my, mx + v.getWidth(), my + v.getHeight());
			break;
		}
		return mGestureDetector.onTouchEvent(event);

	}
	/**
	 * 切换横竖屏方式
	 */
	public void changeScreenOrientation(){
		RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);//按键布局
		RelativeLayout videoLayout = (RelativeLayout) findViewById(R.id.titlelayout);//标题布局
		if(0 == Constants.FLAG_FULLSCREEN){
			Log.i(TAG, "+++++++++++++++++横向");
			relativeLayout.setVisibility(View.GONE);// 隐藏布局
			videoLayout.setVisibility(View.GONE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
			Constants.FLAG_FULLSCREEN = 1;// 重置
		}else{
			Log.i(TAG, "+++++++++++++++++竖向");
			relativeLayout.setVisibility(View.VISIBLE);// 显示布局
			videoLayout.setVisibility(View.VISIBLE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
			Constants.FLAG_FULLSCREEN = 0;
		}
	}

	private class MySimpleGesture extends SimpleOnGestureListener {
		
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			changeScreenOrientation();
			return super.onDoubleTap(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return super.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {

				if (e1.getX() - e2.getX() > 50) {
					leftPTZCtrl();
					Log.i(TAG, "+++++++++++++++++左");
					return true;
				} else if (e1.getX() - e2.getX() < -50) {
					rightPTZCtrl();
					Log.i(TAG, "+++++++++++++++++右");
					return true;
				} else if (e1.getY() - e2.getY() > 50) {
					upPTZCtrl();
					Log.i(TAG, "+++++++++++++++++上");
					return true;
				} else if (e1.getY() - e2.getY() < -50) {
					downPTZCtrl();
					Log.i(TAG, "+++++++++++++++++下");
					return true;
				}
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		

	}
	
	/*调节摄像头远近功能  , 缩小*/
	public void videoSuo(View view) throws IOException{
		if(myVideoView.displayHeight > 0 && myVideoView.displayWidth > 0){
			myVideoView.displayHeight -= 90;
			myVideoView.displayWidth -= 90;
		}
	}
	/*调节摄像头远近功能  , 放大*/
	public void videoFang(View view){
		myVideoView.displayHeight += 90;
		myVideoView.displayWidth += 90;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.i(TAG, "+++++onConfigurationChanged++++++++newConfig:"+newConfig.orientation);
		Constants.SCREEN_CHANGE_STATUS = true;
		super.onConfigurationChanged(newConfig);
	}
}