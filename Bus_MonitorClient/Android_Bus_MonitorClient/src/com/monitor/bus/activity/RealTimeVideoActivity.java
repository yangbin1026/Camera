package com.monitor.bus.activity;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;
import com.monitor.bus.utils.MUtils;
import com.monitor.bus.utils.SPUtils;
import com.monitor.bus.bean.DeviceInfo;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.utils.LogUtils;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.monitor.bus.bean.manager.BaiduMapManager;
import com.monitor.bus.bean.manager.BaseMapManager;
import com.monitor.bus.bean.manager.GoogleMapManager;
import com.monitor.bus.control.VideoPlayControl;
import com.monitor.bus.database.DatabaseHelper;
import com.monitor.bus.view.MyVideoView;
import com.monitor.bus.view.dialog.DateUtil;
import com.monitor.bus.view.dialog.MyDataPickerDialog;

/**
 * 实时视频Activity
 * 
 */
public class RealTimeVideoActivity extends FragmentActivity implements OnTouchListener, View.OnClickListener {
	private static String TAG = "VideoActivity";
	public static final String KEY_DEVICE_INFO = "key_device_info";

	private String recordFilePath = null;// 当前录像文件存储路径
	private String recordFileName = "";// 当前文件名称:当前时间
	boolean isCapturePicture = false;// 是否有操作过抓拍
	boolean isRecording = false;

	private MyVideoView myVideoView;
	private TextView tv_tilte;// 标题名称
	private Button bt_setting;
	private ImageButton ib_record, ib_takePhoto, ib_voice, ib_mic;
	private Dialog chooseChannelDialog;
	private LinearLayout ll_control;
	private RelativeLayout rl_map,rl_video;

	private Intent intent;
	private VideoPlayControl playControl;
	private DatabaseHelper db;// 数据库操作对象
	public Timer recordTimer;// 计时器
	private DeviceInfo deviceInfo;// 设备信息
	private String titleString;
	BaseMapManager mMapManager;
	Context mContext;
	private Handler mHandler;

	boolean isGoogleMap = false;
	int showMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_realtime);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 屏幕保持常亮
		mContext=this;
		mHandler =new Handler();
		initTitle();
		initView();
		initData();
		if(mMapManager!=null){
			mMapManager.onCreat();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(showMode ==0 || showMode==2){
			startAll();
		}
		if(mMapManager!=null){
			mMapManager.onResum();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(mMapManager!=null){
			mMapManager.onPause();
		}
	}

	@Override
	protected void onStop() {
		stopAll();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (isCapturePicture) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//如果是4.4及以上版本
//                Intent mediaScanIntent = new Intent(
//                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                Uri contentUri = Uri.fromFile(mPhotoFile); //out is your output file
//                mediaScanIntent.setData(contentUri);
//                this.sendBroadcast(mediaScanIntent);
            } else {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                		Uri.parse("file://" + Environment.getExternalStorageDirectory())));// 刷新相册环境
            }
		}
		if(mMapManager!=null){
			mMapManager.onDestory();
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

	private void initTitle() {
		tv_tilte = (TextView) findViewById(R.id.tilte_name);
		tv_tilte.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		bt_setting = (Button) findViewById(R.id.bt_setting);
		bt_setting.setBackgroundDrawable(null);
		bt_setting.setText(R.string.channel);
		bt_setting.setVisibility(View.VISIBLE);
		bt_setting.setOnClickListener(this);

	}

	private void initView() {
		myVideoView = (MyVideoView) findViewById(R.id.myVideoView);
		myVideoView.setOnTouchListener(this);
		
		rl_map=(RelativeLayout) findViewById(R.id.rl_map);
		rl_video=(RelativeLayout) findViewById(R.id.rl_video);
		ll_control=(LinearLayout) findViewById(R.id.ll_control);
		
		ib_record = (ImageButton) findViewById(R.id.ib_record);
		ib_takePhoto = (ImageButton) findViewById(R.id.ib_takephoto);
		ib_voice = (ImageButton) findViewById(R.id.ib_voice);
		ib_mic = (ImageButton) findViewById(R.id.ib_mic);
		
		ib_record.setOnClickListener(this);
		ib_takePhoto.setOnClickListener(this);
		ib_voice.setOnClickListener(this);
		ib_mic.setOnClickListener(this);

	}

	private void initData() {
		intent = this.getIntent();
		deviceInfo = (DeviceInfo) intent.getSerializableExtra(KEY_DEVICE_INFO);
		if (deviceInfo == null) {
			MUtils.toast(this, "设备为空");
			return;
		}
		showMode=SPUtils.getInt(mContext, SPUtils.KEY_REMEMBER_SHOWMODE, 2);
		LogUtils.d(TAG, "showMode="+showMode);
		switch (showMode) {
		case 0://视频
			rl_map.setVisibility(View.GONE);
			break;
		case 1://地图
			rl_video.setVisibility(View.GONE);
			ll_control.setVisibility(View.GONE);
			RelativeLayout.LayoutParams params=(LayoutParams) rl_map.getLayoutParams();
			params.height=LayoutParams.MATCH_PARENT;
			rl_map.setLayoutParams(params);
			break;
		case 2:
			
			break;
	
		default:
			break;
		}
		
		titleString = deviceInfo.getDeviceName() + " - " + "channel_" + deviceInfo.getCurrentChn();
		tv_tilte.setText(titleString);
	
		if(showMode ==1 || showMode==2){
			isGoogleMap=SPUtils.getBoolean(mContext, SPUtils.KEY_REMEMBER_SELECTMAP, false);
			if (isGoogleMap) {
				mMapManager = new GoogleMapManager(this);
			} else {
				mMapManager = new BaiduMapManager(this);
			}
			mMapManager.setDeviceInfo(deviceInfo);
		}
	}

	private void startRecord() {
		LogUtils.i(TAG, "-start record()");
		if (!myVideoView.isNormalPlay()) {
			return;
		}
		recordFilePath = MUtils.getCurrentFilePath(Constants.RECORD_FILE_PATH, deviceInfo);
		File file = new File(recordFilePath);
		if (!file.exists()) {// 目录不存在
			file.mkdirs();
		}
		recordFileName = MUtils.getCurrentDateTime(DateUtil.DB_FORMAT);
		String path = recordFilePath + recordFileName + Constants.RECORD_FILE_FORMAT;
		int i = playControl.recordStart(path);
		if (i == 0) {
			MUtils.commonToast(this, R.string.record_fail);
			LogUtils.i(TAG, "-------------start record faild!");
			return;
		}

		// 创建定时器,绘制录像圆点
		recordTimer = new Timer();
		recordTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (myVideoView.shoudDrawCircle) {
					myVideoView.shoudDrawCircle = false;
				} else {
					myVideoView.shoudDrawCircle = true;
				}

			}
		}, 0, 1000);
	}

	private void stopRecord() {
		LogUtils.i(TAG, "stopRecord()");
		if (db == null) {
			db = new DatabaseHelper(this, Constants.DATABASE_NAME);// 初始化数据操作对象
		}
		int i = playControl.recordStop();
		if (i == 1) {
			try {
				db.insertRecordInfo(recordFileName, recordFileName + Constants.RECORD_FILE_FORMAT, recordFilePath);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Log.e(TAG, "-------------stop record faild！");
			MUtils.commonToast(this, R.string.stop_recordfail);
			return;
		}
		myVideoView.shoudDrawCircle = false;
		recordTimer.cancel();
	}
	
	private void startAll(){
		playControl = new VideoPlayControl(this, myVideoView);
		playControl.initRealPlay(deviceInfo);;// 初始化界面
	}
	
	private void stopAll(){
		if (isRecording) {// 停止录像
			stopRecord();
			isRecording = false;
		}
		if(playControl!=null){
			if (Constants.ISOPEN_AUDIO) {// 停止监听
				Constants.ISOPEN_AUDIO = false;
				playControl.track.stop();// 停止音频
			}
			if (playControl.track != null) {
				playControl.track.release();
				playControl.track = null;
			}
			
			if (Constants.ISOPEN_TALK) {
				Constants.ISOPEN_TALK = false;
				playControl.closeTalk();
				playControl.audioRecord.stop();
				
			}
			if (playControl.audioRecord != null) {
				playControl.audioRecord.release();
				playControl.audioRecord = null;
				
			}
			
			playControl.stopStream();
		}

		myVideoView.is_drawblack = true;
		myVideoView.isPlaying = false;
		myVideoView.videoWidth = 0;
		myVideoView.videoHeight = 0;
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
		LinearLayout ll_control = (LinearLayout) findViewById(R.id.ll_control);// 按键布局
		RelativeLayout title = (RelativeLayout) findViewById(R.id.titlelayout);// 标题布局
		if (0 == Constants.FLAG_FULLSCREEN) {
			LogUtils.i(TAG, "+++++++++++++++++hengxiang");
			ll_control.setVisibility(View.GONE);// 隐藏布局
			title.setVisibility(View.GONE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏
			Constants.FLAG_FULLSCREEN = 1;// 重置
		} else {
			LogUtils.i(TAG, "+++++++++++++++++shuxiang");
			ll_control.setVisibility(View.VISIBLE);// 显示布局
			title.setVisibility(View.VISIBLE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// 竖屏
			Constants.FLAG_FULLSCREEN = 0;
		}
	}
	
	private void showChannelDialog(List<String> mlist) {
		if(chooseChannelDialog==null){
			
			MyDataPickerDialog.Builder builder = new MyDataPickerDialog.Builder(mContext);
			chooseChannelDialog = builder.setData(mlist).setSelection(1).setTitle(mContext.getString(R.string.cancel))
					.setOnDataSelectedListener(new MyDataPickerDialog.OnDataSelectedListener() {
						@Override
						public void onDataSelected(String itemValue, int position) {
							Log.i(TAG, "selectchannel:" + itemValue + "  position:" + position);
							deviceInfo.setCurrentChn(position+1);
							titleString = deviceInfo.getDeviceName() + " - " + "channel_" + deviceInfo.getCurrentChn();
							tv_tilte.setText(titleString);
							stopAll();
							mHandler.postDelayed(new Runnable() {
								public void run() {
									startAll();
								}
							}, 1000);
						}
						
						@Override
						public void onCancel() {
							
						}
					}).create();
		}

		chooseChannelDialog.show();
	}

	// private class MySimpleGesture extends SimpleOnGestureListener {
	//
	// @Override
	// public boolean onDoubleTap(MotionEvent e) {
	// changeScreenOrientation();
	// return super.onDoubleTap(e);
	// }
	//
	// @Override
	// public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
	// float velocityY) {
	// return super.onFling(e1, e2, velocityX, velocityY);
	// }
	//
	// @Override
	// public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
	// float distanceY) {
	//
	// if (e1.getX() - e2.getX() > 50) {
	// yunCtrl(Constants.PTZ_DERECTION.PTZ_LEFT);
	// return true;
	// } else if (e1.getX() - e2.getX() < -50) {
	// yunCtrl(Constants.PTZ_DERECTION.PTZ_RIGHT);
	// return true;
	// } else if (e1.getY() - e2.getY() > 50) {
	// yunCtrl(Constants.PTZ_DERECTION.PTZ_UP);
	// return true;
	// } else if (e1.getY() - e2.getY() < -50) {
	// yunCtrl(Constants.PTZ_DERECTION.PTZ_DOWN);
	// return true;
	// }
	// return super.onScroll(e1, e2, distanceX, distanceY);
	// }
	//
	// }

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.ib_record:
			// 录像
			if (!myVideoView.isNormalPlay()) {
				LogUtils.i(TAG, "can record, is not playing");
				return;
			}
			if (isRecording) {
				stopRecord();
			} else {
				startRecord();
			}
			isRecording = !isRecording;
			ib_record.setImageResource(isRecording? R.drawable.record_off:R.drawable.record_on);
			break;
		case R.id.ib_voice:
			if(playControl==null){
				return;
			}
			// 监听
			if (myVideoView.isNormalPlay()) {// 正常播放
				if (Constants.ISOPEN_AUDIO) {// 监听已开启
					// 停止监听
					playControl.track.stop();// 停止音频
					ib_voice.setImageDrawable(getResources().getDrawable(R.drawable.voice_off));
				} else {// 监听已关闭
					// 开始监听
					playControl.track.play();// 启动本地音频读取
					ib_voice.setImageDrawable(getResources().getDrawable(R.drawable.voice_on));
				}
				Constants.ISOPEN_AUDIO = !Constants.ISOPEN_AUDIO;
			}
			break;
		case R.id.ib_mic:
			// 对讲
			if (myVideoView.isNormalPlay()) {// 正常播放
				if (Constants.ISOPEN_TALK) {// 对讲已开启
					Constants.ISOPEN_TALK = false;
					// 停止对讲
					playControl.closeTalk();
					playControl.audioRecord.stop();
					ib_mic.setImageDrawable(getResources().getDrawable(R.drawable.mic_on));
					
				} else {// 对讲已关闭
					// 重新设置按下时的背景图片
					Constants.ISOPEN_TALK = true;
					// 打开对讲
					playControl.audioRecord.startRecording();// 开启录音
					playControl.openTalk();// 打开对讲
					ib_mic.setImageDrawable(getResources().getDrawable(R.drawable.mic_off));
				}
			}
			break;
		case R.id.ib_takephoto:
			if (!myVideoView.isNormalPlay()) {
				return;
			}
			playControl.CapturePicture();
			isCapturePicture = true;
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
		case R.id.bt_setting:
			// 通道按钮
			List<String> channel = new ArrayList<String>();
			for(int i=1; i<6;i++){
				channel.add(mContext.getString(R.string.channel)+i);
			}
			showChannelDialog(channel);
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
		return super.onTouchEvent(event);

	}
}