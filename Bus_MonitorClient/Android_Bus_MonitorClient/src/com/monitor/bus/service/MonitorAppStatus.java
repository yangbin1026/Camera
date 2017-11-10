package com.monitor.bus.service;


import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.jniUtil.JNVPlayerUtil;

/**
 * @Description: 监听CPU使用率
 * @author mzp
 * 
 */
public class MonitorAppStatus extends Service {
	private static final String TAG = "MonitorAppStatus";
	//定义Handler对象
	Handler mhandler = null;
	
	@Override
	public void onCreate() {
		mhandler = new Handler();
		super.onCreate();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	        mhandler.post(monitorStatus);   //Handler将线程马上加入到线程队列去，没有延时	
	        Log.i(TAG, "++++++++++++++启动服务");
	        
		return super.onStartCommand(intent, flags, startId);
	}
	
	 @Override
	    public void onDestroy() {
		 Log.i(TAG, "++++++++++++++终止服务");
		 mhandler.removeCallbacks(monitorStatus);
	        super.onDestroy();
	    } 

	
    
    //监控设备登陆状态
	Runnable monitorStatus= new Runnable() {
		//匿名内部类方式，实现run()方法
		public void run() {
            try { 
            	//Log.i(TAG, "其实我一直在默默无闻。。。。。。。");
            	int cpuCount = JNVPlayerUtil.JNV_UpdateCpuInfo();
            	for(int i = 0;i<=cpuCount;i++){
            		
            		Log.e(TAG, "CPU的使用率："+(JNVPlayerUtil.JNV_GetCPUUsage(i)/100 +
            				JNVPlayerUtil.JNV_GetCPUUsage(i)%100)*1.0 +"%");
            	}
            } catch (Exception e) { 
                e.printStackTrace(); 
            } 
			mhandler.postDelayed(monitorStatus, 1000);  
		}
	};	

}
