package com.monitor.bus.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

/**
 * 
 * @author yb
 */
public class LogUtils {
	public static boolean Debug = true;
	SimpleDateFormat sdf;
	private static final long maxLen = 5 * 1024 * 1024; // 5M
	private static final int maxCount = 5;
	private static final String LOG_NAME = "myLog";

	
	final static String TAG = "LogUtils";
	private static LogUtils mUtils;
	File logDir = new File(Environment.getExternalStorageDirectory(), "ALog");
	File currentLogFile ;
	PrintWriter out = null;
	int curentIndex = 0;

	final static int FLUSH_COUNT = 20;
	int logCount = 0;

	private LogUtils() {
	}
	public  static LogUtils getInstance(){
		if(mUtils==null){
			mUtils=new LogUtils();
		}
		return mUtils;
	}
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
	public static void i(String tag, String msg) {
		if (Debug)
			Log.i(tag, msg);
	}

	public void localLog(String tag, String log) {
		if (log == null) {
			log="LOG IS NULL!!!";
			return;
		}
		currentLogFile= new File(logDir, LOG_NAME + ".log");
		Log.i("myyangbin", "currentLogFile=="+currentLogFile.getAbsolutePath());
		if (out == null) {
			try {
				Log.i("myyangbin", "outpath"+currentLogFile.getAbsolutePath());
				this.out = new PrintWriter(new FileOutputStream(currentLogFile, true));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Log.i("myyangbin", "out=="+out);
		if (currentLogFile.length() > maxLen) {
			Log.i("myyangbin", "out>MaxLen");
			out.close();
			if (curentIndex > maxCount) {
				curentIndex = 0;
			} else {
				curentIndex++;
			}
			try {
				Log.i("myyangbin", "new out"+curentIndex+"：："+logDir.getAbsolutePath());
				this.currentLogFile = new File(logDir, LOG_NAME + curentIndex + ".log");
				this.out = new PrintWriter(this.currentLogFile);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
			
		tag += ":";
		if(sdf==null){
			 sdf= new SimpleDateFormat("yyyyMMdd-HH-mm-ss");
		}
		out.println(sdf.format(new Date(System.currentTimeMillis())) + ": " + tag + "___" + log);
		out.flush();
	}

}
