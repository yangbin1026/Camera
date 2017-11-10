package com.monitor.bus.activity;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.monitor.bus.adapter.MyNotification;
import com.monitor.bus.consts.Constants;

/**
 * 公共activity
 */

public class BaseActivity extends Activity {
	private static String TAG = "BaseActivity";
	
	private MyNotification myNotification;//引用通知
	public boolean isCompleteExit = false;//是否完全退出程序
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		myNotification = new MyNotification(this);
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (!isAppOnForeground()) {
			// app 进入后台
			  //Constants.INSTANCE = this;
			if(!isCompleteExit){//
				if(Constants.IS_ACTIVE){
					Log.i(TAG, "app 进入后台-------------");
					myNotification.showNotification(null);
					// 全局变量
					Constants.IS_ACTIVE = false; // 记录当前已经进入后台
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!Constants.IS_ACTIVE) {
			// app 从后台唤醒，进入前台
			Log.i(TAG, "app 从后台唤醒，进入前台-------------");
			myNotification.cancelNotification();
			Constants.IS_ACTIVE = true;
		}
	}

	/**
	 * 程序是否在前台运行
	 * 
	 * @return
	 */
	public boolean isAppOnForeground() {
		// Returns a list of application processes that are running on the
		// device

		ActivityManager activityManager = (ActivityManager) getApplicationContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		String packageName = getApplicationContext().getPackageName();

		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		if (appProcesses == null)
			return false;

		for (RunningAppProcessInfo appProcess : appProcesses) {
			// The name of the process that this object is associated with.
			if (appProcess.processName.equals(packageName)
					&& appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				return true;
			}
		}

		return false;
	}
	
}
