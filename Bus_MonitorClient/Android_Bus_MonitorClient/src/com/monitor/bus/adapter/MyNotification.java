package com.monitor.bus.adapter;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;

import com.monitor.bus.activity.HomeActivity;
import com.monitor.bus.activity.R;
/**
 * @Description: Notification扩展类
 *
 */
public class MyNotification {
	private Intent notificationIntent;
	private NotificationManager notificationManager;
	private Activity currentContext;
	public MyNotification(Activity context) {
		currentContext = context;
		  // 创建一个NotificationManager的引用
        notificationManager = (NotificationManager)context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
	}
	
    // 显示Notification
	public void showNotification(String alarmInfo) {
        // 定义Notification的各种属性
        Notification notification = new Notification();
        CharSequence contentText;
       if(null != alarmInfo){//报警状态
    		
    		notification.icon = R.drawable.alert_logo;
    		notification.tickerText =currentContext.getString(R.string.alarm);
    		contentText =  alarmInfo;// 通知栏内容
    	   
       }else{
    	   notification.icon = R.drawable.online_logo;
    	   notification.tickerText =currentContext.getString(R.string.online);
    	   contentText =  currentContext.getString(R.string.back_stage);// 通知栏内容
       }
        notification.when = System.currentTimeMillis();
        // 将此通知放到通知栏的"Ongoing"即"正在运行"组中
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        // 点击后自动清除Notification
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.defaults = Notification.DEFAULT_LIGHTS;
        notification.ledARGB = Color.BLUE;
        notification.ledOnMS = 5000; 
        // 设置通知的事件消息
        CharSequence contentTitle = currentContext.getString(R.string.product_name); // 通知栏标题
        notificationIntent = new Intent(currentContext,HomeActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        
        PendingIntent contentIntent = PendingIntent.getActivity(
        		currentContext, 0, notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(currentContext, contentTitle, contentText, contentIntent);
        // 把Notification传递给NotificationManager
        notificationManager.notify(0, notification);
	}
	 
	// 取消通知
	public void cancelNotification(){
		notificationManager.cancel(0);
	}
}
