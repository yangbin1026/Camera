package com.monitor.bus.activity;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.monitor.bus.adapter.NotifycationManager;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.utils.MUtils;

/**
 * 公共activity
 */

public class BaseActivity extends Activity {
	private static String TAG = "BaseActivity";
	
	private NotifycationManager myNotification;//引用通知
	
	public boolean isCompleteExit = false;//是否完全退出程序
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		myNotification = new NotifycationManager(this);
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (!isAppOnForeground()) {
			// app 进入后台
			if(!isCompleteExit){//
				if(MUtils.isBackGround(this)){
					myNotification.showNotification(null);
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!MUtils.isBackGround(this)){
			// app 从后台唤醒，进入前台
			myNotification.cancelNotification();
		}
	}

	/**
	 * 程序是否在前台运行
	 * 
	 * @return
	 */
	private boolean isAppOnForeground() {
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
