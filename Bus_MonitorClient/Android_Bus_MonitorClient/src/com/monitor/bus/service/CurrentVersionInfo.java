package com.monitor.bus.service;

import com.monitor.bus.activity.R;

import android.content.Context;

/**
 * 获取当前旧的应用程序版本信息(包括程序的代码版本，程序名称等)
 *
 */
public class CurrentVersionInfo {
	private static final String TAG = "Config";
	public static final String appPackName = "com.monitor.bus.activity";
	//获取旧应用程序版本号
	public static int getVerCode(Context context) throws Exception{
		int verCode = -1;
		verCode = context.getPackageManager().getPackageInfo(appPackName, 0).versionCode;
		return verCode;
	}
	//获取旧应用程序名称版本
	public static String getVerName(Context context) throws Exception{
		String verName = "";
		verName = context.getPackageManager().getPackageInfo(appPackName, 0).versionName;
		return verName;
	}
	//获取旧应用程序的名字
	public static String getAppName(Context context){
		String appName = context.getResources().getText(R.string.app_name).toString();
		return appName;
	}
}
