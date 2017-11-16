package com.monitor.bus.utils;

import android.util.Log;

/**
 * 
 * @author yb
 */
public class LogUtils {
	public static boolean Debug = true;

	public static void d(String tag, String msg) {
		if (Debug)
			Log.d(tag, msg);
	}

	public static void e(String tag, String msg) {
		if (Debug)
			Log.e(tag, msg);
	}

	public static void v(String tag, String msg) {
		if (Debug)
			Log.v(tag, msg);
	}

}
