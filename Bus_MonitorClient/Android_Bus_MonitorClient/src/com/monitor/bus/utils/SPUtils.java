package com.monitor.bus.utils;

import com.monitor.bus.bean.LoginInfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SPUtils {

	private static SharedPreferences sp;
	private static SharedPreferences sp_login;
	private static String TAG = SPUtils.class.getSimpleName();
	private static final String MY_SP_KEY = "appkey";
	public static final String SHARED_PREFERENCES_NAME = "setting";

	// sp LoginInfo
	private static final String LOGIN_INFO = "login_info";
	private static final String KEY_INFO_NAME = "login_name";
	private static final String KEY_INFO_PW = "login_pw";
	private static final String KEY_INFO_IP = "login_ip";
	private static final String KEY_INFO_PORT = "login_port";

	public static final String KEY_AUTO_LOGIN = "key_auto_login";
	public static final String KEY_GSP_CHECK = "key_gps_check";
	public static final String KEY_LOCAL = "key_local";
	public static final String KEY_REMEMBER_USERINFO = "remember_userinfo";
	public static final String KEY_REMEMBER_SHOWMODE = "show_mode"; //0:视频 1：地图 2：视频地图
	public static final String KEY_REMEMBER_ISGOOGLEMAP = "selectmap";

	public static void saveString(Context context, String key, String value) {
		LogUtils.d(TAG, "saveString:" + key + "：" + value);
		if (null == sp) {
			sp = context.getSharedPreferences(MY_SP_KEY, Context.MODE_APPEND);
		}
		Editor e = sp.edit();
		e.putString(key, value);
		e.commit();
	}

	public static String getString(Context context, String key, String defaultvalue) {
		LogUtils.d(TAG, "getString:" + key + "：" + defaultvalue);
		if (null == sp) {
			sp = context.getSharedPreferences(MY_SP_KEY, Context.MODE_APPEND);
		}
		return sp.getString(key, defaultvalue);
	}

	public static void saveBoolean(Context context, String key, boolean value) {
		LogUtils.d(TAG, "saveBoolean:" + key + "：" + value);
		if (null == sp) {
			sp = context.getSharedPreferences(MY_SP_KEY, Context.MODE_APPEND);
		}
		Editor e = sp.edit();
		e.putBoolean(key, value);
		e.commit();
	}

	public static boolean getBoolean(Context context, String key, boolean defaultvalue) {
		LogUtils.d(TAG, "getBoolean:" + key + "：" + defaultvalue);
		if (null == sp) {
			sp = context.getSharedPreferences(MY_SP_KEY, Context.MODE_APPEND);
		}
		return sp.getBoolean(key, defaultvalue);
	}

	public static void saveInt(Context context, String key, int value) {
		LogUtils.d(TAG, "saveint:" + key + "：" + value);
		if (null == sp) {
			sp = context.getSharedPreferences(MY_SP_KEY, Context.MODE_APPEND);
		}
		Editor e = sp.edit();
		e.putInt(key, value);
		e.commit();
	}
	
	public static int getInt(Context context, String key, int defaultvalue) {
		LogUtils.d(TAG, "getInt:" + key + "：" + defaultvalue);
		if (null == sp) {
			sp = context.getSharedPreferences(MY_SP_KEY, Context.MODE_APPEND);
		}
		return sp.getInt(key, defaultvalue);
	}

	public static int getBoolean(Context context, String key, int defaultvalue) {
		LogUtils.d(TAG, "getint:" + key + "：" + defaultvalue);
		if (null == sp) {
			sp = context.getSharedPreferences(MY_SP_KEY, Context.MODE_APPEND);
		}
		return sp.getInt(key, defaultvalue);
	}

	public static float[] getBasicPointData(Context context) {
		float[] data = new float[8];
		if (null == sp) {
			sp = context.getSharedPreferences(MY_SP_KEY, Context.MODE_APPEND);
		}
		for (int i = 0; i < data.length; i++) {
			data[i] = sp.getFloat("data" + i, 0f);
		}
		return data;
	}

	public static void saveLoginInfo(Context context, LoginInfo info) {
		if (null == sp_login) {
			sp_login = context.getSharedPreferences(MY_SP_KEY, Context.MODE_APPEND);
		}
		LogUtils.getInstance().localLog(TAG, info.toString());
		Editor e = sp.edit();
		e.putString(KEY_INFO_NAME, info.getUserName());
		e.putString(KEY_INFO_PW, info.getPassWord());
		e.putString(KEY_INFO_IP, info.getIp());
		e.putInt(KEY_INFO_PORT, info.getPort());
		e.commit();
		LogUtils.d(TAG, "saveLoginInfo:" + info.toString());
	}

	public static LoginInfo getLoginInfo(Context context) {
		if (null == sp_login) {
			sp_login = context.getSharedPreferences(MY_SP_KEY, Context.MODE_APPEND);
		}
		LoginInfo info = new LoginInfo();
		info.setUserName(sp_login.getString(KEY_INFO_NAME, ""));
		info.setPassWord(sp_login.getString(KEY_INFO_PW, ""));
		info.setIp(sp_login.getString(KEY_INFO_IP, ""));
		info.setPort(sp_login.getInt(KEY_INFO_PORT, 0));
		LogUtils.d(TAG, "getLoginInfo:" + info.toString());
		return info;
	}

}
