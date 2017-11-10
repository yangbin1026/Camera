package com.monitor.bus.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;

import com.jniUtil.JNVPlayerUtil;
import com.jniUtil.MyUtil;
import com.monitor.bus.adapter.MyNotification;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.control.LoginEventControl;

/**
 * 主界面
 */
@SuppressLint("UseSparseArrays")
public class MainListActivity extends BaseActivity{
	
	private ArrayList<HashMap<String, Object>> mainListView = null;
	private PopupWindow mypopupWindow;
	private View view;
	private MyNotification myNotification;//引用通知
	private GridView mainList;
	//private Intent serviceIntent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("MainListActivity", "===========>onCreate");
		initMainListData();
		getWindow().setFlags(0x08000000, 0x08000000);
		//serviceIntent = new Intent(this,MonitorAppStatus.class);
		//startService(serviceIntent);
	}
	
	@Override
	public void onBackPressed() {
		//HOME键功能（回到主界面程序后台运行）
		Intent intent= new Intent(Intent.ACTION_MAIN); 
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);  
	}
	
	@Override
	protected void onDestroy() {
		if(myNotification != null){ 
			myNotification.cancelNotification();
		}
		//stopService(serviceIntent);//停止服务
		
		JNVPlayerUtil.JNV_N_GetAlarmStop("");//获取报警信息
		LoginEventControl.alarmTimer.cancel();
		Log.e("MainListActivity", "=======onDestroy");
		JNVPlayerUtil.JNV_N_Logout();//登出
		JNVPlayerUtil.JNV_UnInit();//释放（反初始化）
		Process.killProcess(Process.myPid()); 
//		System.exit(0);
		super.onDestroy();
	}

	/**
	 * 初始化主界面数据
	 */
	public void initMainListData(){
		myNotification = new MyNotification(this);
		MyUtil.initTitleName(this,R.layout.main_listview,R.string.main_title);
		mainList = (GridView) findViewById(R.id.gridview);
		mainList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				switch (position) {
				case 0:
					Intent intent_1=new Intent();
					intent_1.setClass(MainListActivity.this, RecordQueryActivity.class);
					startActivity(intent_1);
					break;
					
				case 1:
					Intent intent_2=new Intent();
					intent_2.setClass(MainListActivity.this, BusDeviceList.class);
					startActivity(intent_2);
					break;
					
				case 2:
					Intent intent_3=new Intent();
					MyUtil.startMapActivity(MainListActivity.this, intent_3);
					/*
					if(MyUtil.isChina(MainListActivity.this)){
					if(MyUtil.getDefMapIsBaiduMap(MainListActivity.this)){
						intent_3.setClass(MainListActivity.this, UserMapActivity.class);
					}else{
						if( MyUtil.checkGoogleMapModule(MainListActivity.this) )
							intent_3.setClass(MainListActivity.this, UserGoogleMapActivity.class);
					}
					startActivity(intent_3);
					}
					*/
					break;
					
				case 3:
					Intent intent_4=new Intent();
					intent_4.setClass(MainListActivity.this, FilelistActivity.class);
					startActivity(intent_4);
					break;
					
				case 4:
					Intent intent_5=new Intent();
					intent_5.setClass(MainListActivity.this, AlarmListActivity.class);
					startActivity(intent_5);
					break;
					
				case 5:
					if (mypopupWindow == null) {
						LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						view = layoutInflater.inflate(R.layout.about, null);
						// 创建一个PopuWidow对象 
						 mypopupWindow = new PopupWindow(view, 350,200, true);
						}
					//mypopupWindow.setAnimationStyle(R.anim.popup_anim);
						// 使其聚集
					mypopupWindow.setFocusable(true);
						// 设置允许在外点击消失
					mypopupWindow.setOutsideTouchable(true);
						// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
					mypopupWindow.setBackgroundDrawable(new BitmapDrawable());
						mypopupWindow.showAtLocation(view, 
				                Gravity.CENTER | Gravity.CENTER, 0, 0);//在屏幕的中间位置显示 
						mypopupWindow.update(); 
					break;
				default:
					break;
				}
				
			}
		});

		 //生成动态数组，加入数据 
	       mainListView = new ArrayList<HashMap<String, Object>>(); 
	        for(int i=0;i<6;i++) 
	        {
	       if(i==0){
	        HashMap<String, Object> map = new HashMap<String, Object>(); 
	                map.put("ItemImage", R.drawable.play_back);//图像资源的ID 
	                map.put("ItemTitle",  getString(R.string.record_stream)); 
	                mainListView.add(map);
	        }else if(i==1){
	        HashMap<String, Object> map = new HashMap<String, Object>(); 
	                map.put("ItemImage", R.drawable.dev_list);//图像资源的ID 
	                map.put("ItemTitle", getString(R.string.dev_list)); 
	                mainListView.add(map);
	        }else if(i==2){
	        HashMap<String, Object> map = new HashMap<String, Object>(); 
	                map.put("ItemImage", R.drawable.user_map);//图像资源的ID 
	                map.put("ItemTitle", getString(R.string.electronMap)); 
	                mainListView.add(map);
	        }else if(i==3){
		        HashMap<String, Object> map = new HashMap<String, Object>(); 
                map.put("ItemImage", R.drawable.picture_manage);//图像资源的ID 
                map.put("ItemTitle", getString(R.string.pic_list)); 
                mainListView.add(map);
	        }else if(i==4){
		        HashMap<String, Object> map = new HashMap<String, Object>(); 
                map.put("ItemImage", R.drawable.alarm_list);//图像资源的ID 
                map.put("ItemTitle", getString(R.string.alarm_list)); 
                mainListView.add(map);
	        }else{
	        HashMap<String, Object> map = new HashMap<String, Object>(); 
	                map.put("ItemImage", R.drawable.about);//图像资源的ID 
	                map.put("ItemTitle", getString(R.string.about)); 
	                mainListView.add(map);
	        }
	            
	        } 
			
			SimpleAdapter simpleAdapter = new SimpleAdapter(this,
					mainListView, R.layout.main_listitem,
					new String[] { "ItemImage","ItemTitle" }, new int[] { R.id.main_imageview,R.id.main_textview });
			mainList.setAdapter(simpleAdapter);
	}
	
	@Override
	protected void onStop() {
		Log.e("MainListActivity", "==============onStop");
		super.onStop();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			isCompleteExit = true;
			finish(); 
			break;
		case 1:
			
			break;
		case 2:
			startActivity(new Intent(this, SettingActivity.class));
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, R.string.exitComplete);
		menu.add(0, 1, 1, R.string.cancel);
		int versionLevel = android.os.Build.VERSION.SDK_INT;
		menu.add(0, 2, 2, R.string.setting);
		return super.onCreateOptionsMenu(menu);
	}
	//触摸打开菜单按钮
	 @Override
	public boolean onTouchEvent(MotionEvent event) {
		 this.openOptionsMenu();
		return super.onTouchEvent(event);
	}
} 
