package com.monitor.bus.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SPUtils {

	private static SharedPreferences sp;
	private static final String KEY_SHOW_MIRROR = "key_show_mirror";


	public static float[] getBasicPointData(Context context) {
		float[] data = new float[8];
		if (null == sp) {
			sp = context.getSharedPreferences("points", Context.MODE_APPEND);
		}
		for (int i = 0; i < data.length; i++) {
			data[i] = sp.getFloat("data" + i, 0f);
		}
		return data;
	}

	public static void setMirrorShow(Context context, boolean show) {
		if (null == sp) {
			sp = context.getSharedPreferences("points", Context.MODE_APPEND);
		}
		Editor e = sp.edit();
		e.putBoolean(KEY_SHOW_MIRROR, show);
		e.commit();
	}

	public static boolean getMirrorShow(Context context) {
		if (null == sp) {
			sp = context.getSharedPreferences("points", Context.MODE_APPEND);
		}
		return sp.getBoolean(KEY_SHOW_MIRROR, false);
	}
}
