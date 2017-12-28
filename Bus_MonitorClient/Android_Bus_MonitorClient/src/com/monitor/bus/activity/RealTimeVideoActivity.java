package com.monitor.bus.activity;

import java.io.File;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;
import com.monitor.bus.utils.MUtils;
import com.monitor.bus.bean.DeviceInfo;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.utils.LogUtils;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.monitor.bus.bean.BaiduMapManager;
import com.monitor.bus.bean.BaseMapManager;
import com.monitor.bus.bean.GoogleMapManager;
import com.monitor.bus.control.VideoPlayControl;
import com.monitor.bus.database.DatabaseHelper;
import com.monitor.bus.view.MyVideoView;

/**
 * 实时视频Activity
 * 
 */
public class RealTimeVideoActivity extends FragmentActivity implements OnTouchListener, View.OnClickListener {
	private static String TAG = "VideoActivity";
	public static final String KEY_DEVICE_INFO = "key_device_info";

	String recordFilePath = null;// 当前录像文件存储路径
	String times = "";// 当前文件名称
	boolean isCapturePicture = false;// 是否有操作过抓拍
	boolean isRecording = false;

	private MyVideoView myVideoView;
	private TextView tv_tilte;// 标题名称
	private Button bt_setting;
	private ImageButton ib_record, ib_takePhoto, ib_voice, ib_mic;

	private Intent intent;
	private VideoPlayControl playControl;
	private DatabaseHelper db;// 数据库操作对象
	public Timer recordTimer;// 计时器

	private float x, y;
	private int mx, my;

	private DeviceInfo deviceInfo;// 设备信息
	private String titleString;
	BaseMapManager mMapManager;

	boolean isGoogleMap = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_realtime);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 屏幕保持常亮

		initTitle();
		initView();
		initData();
		mMapManager.onCreat();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapManager.onResum();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapManager.onPause();
	}

	@Override
	protected void onStop() {
		if (isRecording) {// 停止录像
			stopRecord();
			isRecording = false;
		}
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

		myVideoView.is_drawblack = true;
		myVideoView.isPlaying = false;
		myVideoView.videoWidth = 0;
		myVideoView.videoHeight = 0;
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
		mMapManager.onDestory();
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
		bt_setting.setText(R.string.channle);
		bt_setting.setVisibility(View.VISIBLE);
		bt_setting.setOnClickListener(this);

	}

	private void initData() {
		intent = this.getIntent();
		deviceInfo = (DeviceInfo) intent.getSerializableExtra(KEY_DEVICE_INFO);
		if (deviceInfo == null) {
			MUtils.toast(this, "设备为空");
			return;
		}
		titleString = deviceInfo.getDeviceName() + " - " + "channel_" + deviceInfo.getCurrentChn();
		tv_tilte.setText(titleString);

		playControl = new VideoPlayControl(this, myVideoView);
		playControl.initVideoPlay(intent, VideoPlayControl.STREAM_TYPE_REAL);// 初始化界面
		if (isGoogleMap) {
			mMapManager = new GoogleMapManager(this);
		} else {
			mMapManager = new BaiduMapManager(this);
		}
		mMapManager.setDeviceInfo(deviceInfo);
	}

	private void initView() {
		myVideoView = (MyVideoView) findViewById(R.id.myVideoView);
		myVideoView.setOnTouchListener(this);
		ib_record = (ImageButton) findViewById(R.id.ib_record);
		ib_takePhoto = (ImageButton) findViewById(R.id.ib_takephoto);
		ib_voice = (ImageButton) findViewById(R.id.ib_voice);
		ib_mic = (ImageButton) findViewById(R.id.ib_mic);
		ib_record.setOnClickListener(this);
		ib_takePhoto.setOnClickListener(this);
		ib_voice.setOnClickListener(this);
		ib_mic.setOnClickListener(this);

	}

	private void startRecord() {
		if (!myVideoView.isNormalPlay()) {
			return;
		}
		recordFilePath = MUtils.getCurrentFilePath(Constants.RECORD_FILE_PATH, deviceInfo);
		File file = new File(recordFilePath);
		if (!file.exists()) {// 目录不存在
			file.mkdirs();
		}
		times = MUtils.getCurrentDateTime(Constants.YMD_HMS_FORMAT);
		String path = recordFilePath + times + Constants.RECORD_FILE_FORMAT;
		LogUtils.i(TAG, "-------------start record!");
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
				db.insertRecordInfo(times, times + Constants.RECORD_FILE_FORMAT, recordFilePath);
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
			LogUtils.i(TAG, "+++++++++++++++++hengxiang");
			rl_control.setVisibility(View.GONE);// 隐藏布局
			title.setVisibility(View.GONE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏
			Constants.FLAG_FULLSCREEN = 1;// 重置
		} else {
			LogUtils.i(TAG, "+++++++++++++++++shuxiang");
			rl_control.setVisibility(View.VISIBLE);// 显示布局
			title.setVisibility(View.VISIBLE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// 竖屏
			Constants.FLAG_FULLSCREEN = 0;
		}
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
		case R.id.ib_mic:
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