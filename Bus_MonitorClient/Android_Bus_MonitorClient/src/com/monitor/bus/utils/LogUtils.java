package com.monitor.bus.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.SharedPreferences;
import android.util.Log;

/**
 * 
 * @author yb
 */
public class LogUtils {
	public static boolean Debug = true;
	
	final static String TAG = "LogUtils";
    public static boolean SHOW_LOGCAT = true;
    
    final static int FLUSH_COUNT = 20;
    
    private final static String SP_NAME_LOG_INDEX_SUFFIX = "_logIndex";//后缀
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    
    SharedPreferences sp = null;

    String spNameLogIndex = null;
    
    File logDir = null;
    
    File currentFile = null;
    
    PrintWriter out = null;
    
    int curentIndex = 0;
    
    int logCount = 0;
    
    private static final String LOG_NAME = "myLog";
    private static final long maxLen = 5 * 1024 * 1024; // 5M
    private static final int maxCount = 5;

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
	
	 public void localLog(String tag, String log) {
	        if (log == null) {
	            Log.d(TAG, "log is null");
	            return;
	        }
	        
	        if (out == null) {
	           Log.d(TAG, "out is null: " + this.toString());
	           return;
	        }
	        if (currentFile.length() > maxLen) {
	            out.close();
	            
	            if (curentIndex > maxCount) {
	                curentIndex = 0;
	            }
	            currentFile.renameTo(new File(logDir, LOG_NAME + "_" + curentIndex + ".log"));
	            curentIndex++;
	            sp.edit().putInt(spNameLogIndex, curentIndex);
	            sp.edit().commit();
	            Log.d(TAG, "write startIndex: " + curentIndex);
	            try {
	                this.currentFile = new File(logDir, LOG_NAME + ".log");
	                this.out = new PrintWriter(this.currentFile);
	            } catch (IOException e) {
	                Log.d(TAG, "File not found: " + currentFile.getAbsolutePath());
	                return;
	            }
	        }
	        
	        tag += "        ";
	        if (tag.length() > 8) {
	            tag = tag.substring(0, 8);
	        }
	        out.println(sdf.format(new Date(System.currentTimeMillis())) + ": " + tag + "-" + log);
	        if (logCount++ % FLUSH_COUNT == 0) {
	            out.flush();
	        }
	    }

}
