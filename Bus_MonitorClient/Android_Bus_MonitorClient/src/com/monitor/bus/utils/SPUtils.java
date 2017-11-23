package com.monitor.bus.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SPUtils {

	private static SharedPreferences sp;
	private static final String MY_SP_KEY = "appkey";
	private static final String KEY_SHOW_MIRROR = "key_show_mirror";
	
	
	public static void setString(Context context, String key,String value) {
		if (null == sp) {
			sp = context.getSharedPreferences(MY_SP_KEY, Context.MODE_APPEND);
		}
		Editor e = sp.edit();
		e.putString(key, value);
		e.commit();
	}
	
	public static String getString(Context context,String key,String defaultvalue){
		if (null == sp) {
			sp = context.getSharedPreferences(MY_SP_KEY, Context.MODE_APPEND);
		}
		return sp.getString(key, defaultvalue);
	}
	
	public static void setBoolean(Context context, String key,boolean value) {
		if (null == sp) {
			sp = context.getSharedPreferences(MY_SP_KEY, Context.MODE_APPEND);
		}
		Editor e = sp.edit();
		e.putBoolean(key, value);
		e.commit();
	}
	public static boolean getBoolean(Context context,String key,boolean defaultvalue){
		if (null == sp) {
			sp = context.getSharedPreferences(MY_SP_KEY, Context.MODE_APPEND);
		}
		return sp.getBoolean(key, defaultvalue);
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

	public static void setMirrorShow(Context context, boolean show) {
		if (null == sp) {
			sp = context.getSharedPreferences(MY_SP_KEY, Context.MODE_APPEND);
		}
		Editor e = sp.edit();
		e.putBoolean(KEY_SHOW_MIRROR, show);
		e.commit();
	}

	public static boolean getMirrorShow(Context context) {
		if (null == sp) {
			sp = context.getSharedPreferences(MY_SP_KEY, Context.MODE_APPEND);
		}
		return sp.getBoolean(KEY_SHOW_MIRROR, false);
	}

	/**
	 * 保存字符串共享参数
	 */
	public static void saveStringPreferences(Context context, String key, String value) {
		if (null == sp) {
			sp = context.getSharedPreferences(MY_SP_KEY, Context.MODE_APPEND);
		}
		sp.edit().putString(key, value).commit();
	}

	/**
	 * 保存布尔型共享参数
	 */
	public static void saveBooleanPreferences(Context context, String key, boolean value) {
		if (null == sp) {
			sp = context.getSharedPreferences(MY_SP_KEY, Context.MODE_APPEND);
		}
		sp.edit().putBoolean(key, value).commit();
	}

}
