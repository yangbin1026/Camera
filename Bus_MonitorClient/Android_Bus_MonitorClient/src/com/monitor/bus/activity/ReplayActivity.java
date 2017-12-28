package com.monitor.bus.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.monitor.bus.utils.MUtils;
import com.monitor.bus.bean.RecordInfo;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.control.VideoPlayControl;
import com.monitor.bus.utils.LogUtils;
import com.monitor.bus.view.MyVideoView;

/**
 * 录像回放Activity
 *
 */
public class ReplayActivity extends BaseActivity implements OnTouchListener{
	public static final String EXTRA_RECORDINFO="record_info";
	private static String TAG = "RecordActivity";
	private MyVideoView myVideoView;
	private Button pauseButton;//暂停按钮
	private boolean play_or_pause = false;//播放暂停切换标志 false 暂停 true 继续播放 
	GestureDetector mGestureDetector;
	private VideoPlayControl playControl;
	private RecordInfo devRecordInfo;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LogUtils.i(TAG, "+++++++++++++++++回放Activity onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 屏幕保持常亮
		
		Intent intent=this.getIntent();
		devRecordInfo = (RecordInfo) intent.getSerializableExtra(EXTRA_RECORDINFO);// 回放的文件名称
		
		myVideoView=(MyVideoView)findViewById(R.id.myVideoView);
		playControl = new VideoPlayControl(this, myVideoView);
		playControl.initVideoPlay(intent,VideoPlayControl.STREAM_TYPE_RECORD);//初始化界面
		mGestureDetector = new GestureDetector(new MySimpleGesture());
		myVideoView.setOnTouchListener(this);
		myVideoView.setLongClickable(true);
		pauseButton = (Button)findViewById(R.id.pause);
		/*默认开启监听*/
		Constants.ISOPEN_AUDIO = true;
		 //开始监听
		playControl.track.play();// 启动本地音频读取
	}

	@Override
	protected void onDestroy() {
		LogUtils.i(TAG, "+++++++++++++++++回放Activity销毁");
		playControl.track.release();
		playControl.track = null;

		Constants.FLAG_FULLSCREEN = 0;
		
		super.onDestroy();
	}

	@Override
	protected void onRestart() {
		LogUtils.i(TAG, "++++++onRestart++++++++++回放Activity onCreate");
		super.onRestart();
	}
	
	/**
	 * 播放
	 * @param view
	public void playVideo(View view){
		myVideoView.AVP_Native_Start(myVideoView.getHandleId(),status);
	}
	 */
	
	/**
	 * 停止
	 * @param view
	 */
	public void stopVideo(View view){
		if( null != devRecordInfo ){//设备端播放
			playControl.AVP_Native_RecordStop();
		}else{
			playControl.stopVideo();
			playControl.stopVideo();
		}
		
//		if( null != devRecordInfo ){//设备端播放
//			playControl.AVP_Native_RecordStop();
//		}else{
//			playControl.AVP_Native_Stop();
//		}
	} 
	
	/**
	 * 暂停
	 * @param view
	 */
	public void pauseVideo(View view){
		if(myVideoView.is_drawblack){//播放已停止
			MUtils.commonToast(this, R.string.video_stop);
			return;
		}
		if( null != devRecordInfo ){//设备端播放
			if(!play_or_pause){//暂停
				playControl.AVP_Native_RecordCtrl(5);
				play_or_pause=true;
				pauseButton.setText(R.string.play);
			}else{//继续播放
				playControl.AVP_Native_RecordCtrl(4);
				play_or_pause=false;
				pauseButton.setText(R.string.pause);
			}
			
		}else{
			if(!play_or_pause){//暂停
				LogUtils.i(TAG, "本地录像播放====暂停");
				playControl.track.pause();
				playControl.pauseVideo();
				play_or_pause=true;
				pauseButton.setText(R.string.play);
			}else{//继续播放
				LogUtils.i(TAG, "本地录像播放====继续播放");
				playControl.track.play();
				playControl.resumeVideo();
				play_or_pause=false;
				pauseButton.setText(R.string.pause);
			}
		}
	
	}
	
	/**
	 * 返回操作
	 */
	@Override
	public void onBackPressed() {
		if(1 == Constants.FLAG_FULLSCREEN){
			changeRecordScreenOrientation();
			return;
		}else{
			if( null != devRecordInfo ){//设备端播放
				playControl.AVP_Native_RecordStop();
			}else{
				playControl.stopVideo();
				playControl.stopVideo();
			}
			super.onBackPressed();
		}
	}
	
	/**
	 * 切换横竖屏方式
	 */
	public void changeRecordScreenOrientation(){
		 LinearLayout linearLayout = (LinearLayout) findViewById(R.id.record_linear);
		 RelativeLayout videoLayout = (RelativeLayout) findViewById(R.id.video_title);//标题布局
		if(0 == Constants.FLAG_FULLSCREEN){
			LogUtils.i(TAG, "+++++++++++++++++横向");
			linearLayout.setVisibility(View.GONE);// 隐藏布局
			videoLayout.setVisibility(View.GONE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
			Constants.FLAG_FULLSCREEN = 1;// 重置
		}else{
			LogUtils.i(TAG, "+++++++++++++++++竖向");
			linearLayout.setVisibility(View.VISIBLE);// 显示布局
			videoLayout.setVisibility(View.VISIBLE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
			Constants.FLAG_FULLSCREEN = 0;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		return mGestureDetector.onTouchEvent(event);
	}
	 private class MySimpleGesture extends SimpleOnGestureListener {
		
		 @Override
			public boolean onDoubleTap(MotionEvent e) {
			 changeRecordScreenOrientation();
				return super.onDoubleTap(e);
			}
		 
	 }
	
}
