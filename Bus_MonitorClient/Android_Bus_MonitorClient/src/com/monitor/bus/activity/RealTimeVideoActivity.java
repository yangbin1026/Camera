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
import com.monitor.bus.bean.DeviceInfo;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.control.VideoPlayControl;
import com.monitor.bus.database.DatabaseHelper;
import com.monitor.bus.utils.LogUtils;
import com.monitor.bus.view.MyVideoView;

/**
 * 实时视频Activity
 * 
 */
public class RealTimeVideoActivity extends BaseActivity implements OnTouchListener, View.OnClickListener {

	private static String TAG = "VideoActivity";

	String recordFilePath = null;// 当前录像文件存储路径
	String times = "";// 当前文件名称

	boolean isCapturePicture = false;// 是否有操作过抓拍
	boolean isRecording = false;// 当前录像状态标志 true 开始录像 false 停止录像

	private MyVideoView myVideoView;
	private Button mirrorButton;// 镜像按钮
	private TextView text_tilte_name;// 标题名称

	private Intent intent;
	GestureDetector mGestureDetector;// 手势监测
	private VideoPlayControl playControl;
	private DeviceInfo currentDeviceInfo;// 设备信息
	private DatabaseHelper db;// 数据库操作对象
	public Timer recordTimer;// 计时器

	private float x, y;
	private int mx, my;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 屏幕保持常亮
		setContentView(R.layout.activity_realtime);
		Constants.STREAM_PLAY_TYPE = 1;// 设置播放类型为实时视频
		
		initData();
		initView();
	}

	private void initData() {
		intent = this.getIntent();
		currentDeviceInfo = (DeviceInfo) intent.getSerializableExtra("videoData");
		mGestureDetector = new GestureDetector(new MySimpleGesture());
	}

	private void initView() {
		text_tilte_name = (TextView) findViewById(R.id.tilte_name);
		myVideoView = (MyVideoView) findViewById(R.id.myVideoView);
		myVideoView.setOnTouchListener(this);
		playControl = new VideoPlayControl(this, myVideoView);
		playControl.initVideoPlay(intent);// 初始化界面
		if (currentDeviceInfo != null) {// 标题栏设置: 设备名称-当前通道号
			text_tilte_name.setText(
					currentDeviceInfo.getDeviceName() + " - " + "channel_" + currentDeviceInfo.getCurrentChn());
		}
	}


	@Override
	protected void onStop() {
		if (isRecording) {// 停止录像
			try {
				stopRecord();
				isRecording = false;
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
		myVideoView.isPlaying = false;
		myVideoView.videoWidth = 0;
		myVideoView.videoHeight = 0;
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (isCapturePicture) {
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
					Uri.parse("file://" + Environment.getExternalStorageDirectory())));// 刷新相册环境
		}
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if (1 == Constants.FLAG_FULLSCREEN) {
			changeScreenOrientation();
			return;
		} else {
			super.onBackPressed();
		}
	}

	private void startRecord() {
		if (!myVideoView.isNormalPlay()) {
			return;
		}
		recordFilePath = MyUtil.getCurrentFilePath(Constants.RECORD_FILE_PATH, currentDeviceInfo);
		File file = new File(recordFilePath);
		if (!file.exists()) {// 目录不存在
			file.mkdirs();
		}
		times = MyUtil.getCurrentDateTime(Constants.YMD_HMS_FORMAT);
		String path = recordFilePath + times + Constants.RECORD_FILE_FORMAT;
		LogUtils.i(TAG, "-------------开始录像！");
		int i = playControl.RecordStart(path);
		if (i == 0) {
			MyUtil.commonToast(this, R.string.record_fail);
			LogUtils.i(TAG, "-------------开始录像失败！");
			return;
		}

		// 创建定时器,绘制录像圆点
		recordTimer = new Timer();
		recordTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (myVideoView.is_draw_circle) {
					myVideoView.is_draw_circle = false;
				} else {
					myVideoView.is_draw_circle = true;
				}

			}
		}, 0, 1000);
	}

	private void stopRecord() throws ParseException {
		if(db==null){
			db = new DatabaseHelper(this, Constants.DATABASE_NAME);// 初始化数据操作对象
		}
		LogUtils.i(TAG, "-------------停止录像！");
		int i = playControl.RecordStop();
		if (i == 1) {
			db.insertRecordInfo(times, times + Constants.RECORD_FILE_FORMAT, recordFilePath);
		} else {
			Log.e(TAG, "-------------停止录像失败！");
			MyUtil.commonToast(this, R.string.stop_recordfail);
			return;
		}
		myVideoView.is_draw_circle = false;
		recordTimer.cancel();
	}
	
	private void takePhoto() {
		if (!myVideoView.isNormalPlay()) {
			return;
		}
		playControl.CapturePicture();
		isCapturePicture = true;
	}


	/**
	 * 云台控制
	 */
	private void yunCtrl(int control) {
		if (!myVideoView.isNormalPlay()) {
			return;
		}
		playControl.PTZCtrl(control);

	}

	/**
	 * 切换横竖屏方式
	 */
	private void changeScreenOrientation() {
		RelativeLayout rl_control = (RelativeLayout) findViewById(R.id.rl_control);// 按键布局
		RelativeLayout title = (RelativeLayout) findViewById(R.id.titlelayout);// 标题布局
		if (0 == Constants.FLAG_FULLSCREEN) {
			LogUtils.i(TAG, "+++++++++++++++++横向");
			rl_control.setVisibility(View.GONE);// 隐藏布局
			title.setVisibility(View.GONE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏
			Constants.FLAG_FULLSCREEN = 1;// 重置
		} else {
			LogUtils.i(TAG, "+++++++++++++++++竖向");
			rl_control.setVisibility(View.VISIBLE);// 显示布局
			title.setVisibility(View.VISIBLE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// 竖屏
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
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			return super.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

			if (e1.getX() - e2.getX() > 50) {
				yunCtrl(Constants.PTZ_DERECTION.PTZ_LEFT);
				return true;
			} else if (e1.getX() - e2.getX() < -50) {
				yunCtrl(Constants.PTZ_DERECTION.PTZ_RIGHT);
				return true;
			} else if (e1.getY() - e2.getY() > 50) {
				yunCtrl(Constants.PTZ_DERECTION.PTZ_UP);
				return true;
			} else if (e1.getY() - e2.getY() < -50) {
				yunCtrl(Constants.PTZ_DERECTION.PTZ_DOWN);
				return true;
			}
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.ib_record:
			// 录像
			if (myVideoView.isNormalPlay()) {// 正常播放
				if (isRecording) {// 录像已开启
					isRecording = false;
					try {
						// 停止录像
						stopRecord();
					} catch (ParseException e) {
						e.printStackTrace();
					}
				} else {// 录像已关闭

					// 重新设置按下时的背景图片
					isRecording = true;
					// 开始录像
					startRecord();
				}
			}

			break;
		case R.id.ib_voice:
			// 监听
			if (myVideoView.isNormalPlay()) {// 正常播放
				if (Constants.ISOPEN_AUDIO) {// 监听已开启
					Constants.ISOPEN_AUDIO = false;
					// 停止监听
					playControl.track.stop();// 停止音频
				} else {// 监听已关闭
					// 重新设置按下时的背景图片
					Constants.ISOPEN_AUDIO = true;
					// 开始监听
					playControl.track.play();// 启动本地音频读取
				}
			}
			break;
		case R.id.ib_speak:
			// 对讲
			if (myVideoView.isNormalPlay()) {// 正常播放
				if (Constants.ISOPEN_TALK) {// 对讲已开启
					Constants.ISOPEN_TALK = false;
					// 停止对讲
					playControl.closeTalk();
					playControl.audioRecord.stop();
				} else {// 对讲已关闭

					// 重新设置按下时的背景图片
					Constants.ISOPEN_TALK = true;
					// 打开对讲
					playControl.audioRecord.startRecording();// 开启录音
					playControl.openTalk();// 打开对讲
				}
			}
			break;
		case R.id.ib_takephoto:
			takePhoto();
			break;
		case R.id.aboutLayoutId:
			// 镜像
			if (myVideoView.isNormalPlay()) {// 正常播放
				if (Constants.DERECTION_STATE) {// 正
					playControl.AVP_SetMirror(Constants.MIRROE_REVERSE);
				} else {// 反
					playControl.AVP_SetMirror(Constants.MIRROE_NORMAL);
				}
				Constants.DERECTION_STATE = !Constants.DERECTION_STATE;
			}
			break;

		default:
			break;
		}

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
		case R.id.iv_up:
			// 上
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				yunCtrl(Constants.PTZ_DERECTION.PTZ_DOWN);

			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				yunCtrl(Constants.PTZ_DERECTION.PTZ_STOP);
			}

			break;
		case R.id.iv_down:
			// xia
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				yunCtrl(Constants.PTZ_DERECTION.PTZ_DOWN);

			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				yunCtrl(Constants.PTZ_DERECTION.PTZ_STOP);
			}

			break;
		case R.id.iv_left:
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				yunCtrl(Constants.PTZ_DERECTION.PTZ_LEFT);

			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				yunCtrl(Constants.PTZ_DERECTION.PTZ_STOP);
			}

			break;
		case R.id.iv_right:
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				yunCtrl(Constants.PTZ_DERECTION.PTZ_RIGHT);

			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				yunCtrl(Constants.PTZ_DERECTION.PTZ_STOP);
			}

			break;
		case R.id.aboutLayoutId:

			break;

		default:
			break;
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			x = event.getX();
			y = event.getY();
			break;
		case MotionEvent.ACTION_UP:
			yunCtrl(Constants.PTZ_DERECTION.PTZ_STOP);
			break;
		case MotionEvent.ACTION_MOVE:
			mx = (int) (event.getRawX() - x);
			my = (int) (event.getRawY() - y);
			// v.layout(mx, my, mx + v.getWidth(), my + v.getHeight());
			break;
		}
		return mGestureDetector.onTouchEvent(event);

	}
}