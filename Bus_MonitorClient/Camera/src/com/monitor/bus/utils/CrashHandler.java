package com.monitor.bus.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.monitor.bus.activity.R;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class CrashHandler implements UncaughtExceptionHandler {
	
	private static final String TAG = "CrashHandler";
	
	
	 //限制文件数量
	private static final int File_Count = 10;
	private final String Save_Path = ""+Environment.getExternalStorageDirectory()+File.separator+"ALog";
	private SimpleDateFormat format = null;
	private Context context;
	private static CrashHandler mCrashHandler = null;
	/**
	 * 收集的日志信息
	 */
	private LinkedHashMap<String, String> logInfos = new LinkedHashMap<String, String>();
	/**
	 * 这个类是系统默认异常类
	 */
	private Thread.UncaughtExceptionHandler mUncaught = null;
	
	private CrashHandler() {}
	public static synchronized CrashHandler getInstance() {
		if(mCrashHandler == null) {
			mCrashHandler = new CrashHandler();
		}
		return mCrashHandler;
	}
	/**
	 * 初始化
	 */
	public void init(Context context) {
		try {
			if(context == null) 
				return;
			this.context = context;
			if(mUncaught == null) {
				/**
				 * 获取系统默认异常类
				 */
				mUncaught = Thread.getDefaultUncaughtExceptionHandler();
				/**
				 * 之后设置当前类为该程序默认处理异常的类
				 */
				Thread.setDefaultUncaughtExceptionHandler(this);
			}
		} catch (Exception e) {
			Log.e(TAG, "init - "+e.getMessage());
		}
		
	}
	
	/**
	 * 这个回调方法是当程序遇到异常的时候调用
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		/**
		 * 遇到异常的情况下，如果用户不需要处理异常或者是处理异常的时候出错，那么由系统来交接这个异常处理
		 */
		if(!handleException(ex) && mUncaught != null) {
			mUncaught.uncaughtException(thread, ex);
		}else {
			try {
				Thread.sleep(2000);
			} catch (Exception e) {
				LogUtils.e(TAG,ex.getMessage());
				e.printStackTrace();
			}
			
			/**
			 * 如果用户选择处理并且处理完成，那么由用户自定义怎么操作
			 */
			//这里我是直接退出杀死进程
			/**
			 * 其实这里要杀死进程还不是特别完善，这里不能确定进程是否真的被杀掉
			 */
			android.os.Process.killProcess(android.os.Process.myPid());
			java.lang.System.exit(1);
		}
		

	}
	/**
	 * 用户处理异常操作
	 * @param ex
	 * @return
	 */
	private boolean handleException(Throwable ex) {
		try {
			/**
			 * 如果异常类为空，那么交给UncaughtException处理
			 */
			if(ex == null)
				return false;
			/**
			 * 可以做一个Toast提示
			 * 可以忽略
			 */
			new Thread(new Runnable() {
				@Override
				public void run() {
					Looper.prepare();
					MUtils.toast(context, R.string.errorout);
					Looper.loop();
				}
			}).start();
			
			/**
			 * 之后收集手机设备信息
			 * 以及当前应用的一些信息
			 */
			collectDeviceInfo(context);
			
			/**
			 * 将异常日志写入文件中
			 */
			exceptionLogWriteToFile(ex);
			
			return true;
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, "handleException - "+e.getMessage());
		}
		
		return false;
	}
	/**
	 * 将异常设备信息和日志写入文件之中
	 * @param ex
	 */
	private void exceptionLogWriteToFile(Throwable ex) {
		// TODO Auto-generated method stub
		try {
			StringBuffer buffer = new StringBuffer();
			/**
			 * 设备信息
			 */
			if(logInfos != null && logInfos.size() > 0) {
				for(Map.Entry<String, String> entry:logInfos.entrySet()) {
					buffer.append(entry.getKey()+" = "+entry.getValue()+"\n");
				}
			}
			
			/**
			 * 异常信息写入文件
			 */
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			ex.printStackTrace(printWriter);
			Throwable cause = ex.getCause();
			while(cause != null) {
				cause.printStackTrace(printWriter);
				cause = cause.getCause();
			}
			/**
			 * 关闭管道
			 */
			printWriter.close();
			String result = writer.toString();
			Log.e(TAG, "exceptionWhite2File:"+result);
			buffer.append("\nException:\n");
			buffer.append(result);
			String timeTemp = ""+System.currentTimeMillis();
			if(format == null) {
				format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			}
			/**
			 * 当前时间
			 */
			String times = format.format(new Date());
			String fileName = "crash-"+times+"-"+timeTemp+".log";
			
			/**
			 * btjwt130
			 * 写入文件
			 */
//			Log.e(TAG, ""+buffer.toString());
			writeFileToSdcard(fileName, new String(buffer.toString().getBytes(),"UTF-8"));
			/**
			 * 限制日志文件的数量
			 */
			cleanCrashLogCount(Save_Path);
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, "exceptionLogWriteToFile - "+e.getMessage());
		}
		
	}
	
	Comparator<File> comparator = new Comparator<File>() {
		
		@Override
		public int compare(File lhs, File rhs) {
			// TODO Auto-generated method stub
			/**
			 * 如果当前文件的修改时间大于另一文件修改时间
			 */
			if(lhs.lastModified() > rhs.lastModified()) {
				return 1;
			}else if(lhs.lastModified() < rhs.lastModified()){
				
				return -1;
			}
			
			return 0;
		}
	};
	
	/**
	 * 限制异常日志文件的数量
	 * @param path 
	 */
	private void cleanCrashLogCount(String path) {
		// TODO Auto-generated method stub
		try {
			File file = new File(path);
			if(file != null && file.isDirectory()) {
				File[] files = file.listFiles();
				if(files.length > File_Count) {
					Arrays.sort(files, comparator);
					for(int i = 0 ; i < files.length - File_Count ; i++) {
						files[i].delete();
					}
				}
			}
			
		} catch (Exception e) {
			Log.e(TAG, "cleanCrashLogCount - "+e.getMessage());
		}
		
	}
	
	
	
	/**
	 * 将数据写入文件
	 * @param fileName 文件路径
	 * @param buffer 数据内容
	 */
	private void writeFileToSdcard(String fileName, String buffer) {
		try {
			File flod = new File(Save_Path);
			if(!flod.exists()) {
				flod.mkdirs();
			}
			File file = new File(flod.getAbsolutePath(), fileName);
			if(!file.exists()) {
				
				file.createNewFile();
			}
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
//			FileWriter fileWriter = new FileWriter(file, true);
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
			bufferedWriter.write(buffer);
			bufferedWriter.flush();
			bufferedWriter.close();
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, "writeFileToSdcard - "+e.getMessage());
		}
	}
	/**
	 * 收集当前应用以及设备的信息
	 * @param context
	 */
	private void collectDeviceInfo(Context context) {
		try {
			if(context == null)
				return;
			PackageManager manager = context.getPackageManager();
			if(manager != null) {
				PackageInfo info = manager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
				if(info != null) {
					String versionCode = ""+info.versionCode;
					String versionName = ""+info.versionName;
					String packName = ""+info.packageName;
					logInfos.put("VersionCode", versionCode);
					logInfos.put("VersionName", versionName);
					logInfos.put("PackName", packName);
				}
			}
			/**
			 * 获取手机型号，系统版本，以及SDK版本
			 */
			logInfos.put("手机型号:", android.os.Build.MODEL);
			logInfos.put("系统版本", ""+android.os.Build.VERSION.SDK);
			logInfos.put("Android版本", android.os.Build.VERSION.RELEASE);
			/**
			 * 获取
			 */
			Field[] fields = Build.class.getDeclaredFields();
			if(fields != null && fields.length > 0) {
				for(Field field:fields) {
					field.setAccessible(true);
					logInfos.put(field.getName(), field.get(null).toString());
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "collectDeviceInfo - "+e.getMessage());
		}
	}
}
