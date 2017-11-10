package com.monitor.bus.activity;
import java.io.File;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.jniUtil.MyUtil;
import com.monitor.bus.adapter.DevRecordListAdapter;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.consts.Constants.CALLBACKFLAG;
import com.monitor.bus.control.LoginEventControl;
import com.monitor.bus.model.DevRecordInfo;

/**
 * 设备端录像文件列表
 */
public class DevRecordListActivity extends BaseActivity {

	private ListView recordListView;
	private static String TAG = "DevRecordListActivity";
	private String guId;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		MyUtil.initTitleName(this,R.layout.local_listview,R.string.remote_record);
		Intent intent = getIntent();
		guId = intent.getStringExtra("guid");
		recordListView = (ListView) findViewById(R.id.localListView);  
		if( 0 == Constants.DEVRECORDINFOS.size()){
			LoginEventControl.myProgress = ProgressDialog.show(this, getString(R.string.loading_data_title), getString(R.string.waiting),true,true);
		}
		recordListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				DevRecordInfo devRecordInfo = Constants.DEVRECORDINFOS.get(position);
				devRecordInfo.setGuId(guId);
				Intent intent = new Intent();
				intent.putExtra("devRecordInfo", devRecordInfo);
				intent.setClass(DevRecordListActivity.this, RecordActivity.class);
				startActivity(intent);
			}
		});
		
	}

	@Override
	protected void onResume() { 
		registerBoradcastReceiver();//注册广播接收器
		DevRecordListAdapter devRecordAdapter = new DevRecordListAdapter(this,Constants.DEVRECORDINFOS);
		recordListView.setAdapter(devRecordAdapter);
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		unregisterReceiver(mBroadcastReceiver);
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		File devRecordFile = new File(Constants.DEVRECORD_PASTH);
		if(devRecordFile.exists()){	//存在删除
			devRecordFile.delete();
		}
		Constants.DEVRECORDINFOS.clear();
		super.onDestroy();
	}

	public void registerBoradcastReceiver(){ 
        IntentFilter myIntentFilter = new IntentFilter(); 
        myIntentFilter.addAction("ACTION_NAME"); 
        //注册广播       
        registerReceiver(mBroadcastReceiver, myIntentFilter); 
    } 
	
	//广播接收器
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){ 
        @Override 
        public void onReceive(Context context, Intent intent) { 
            String action = intent.getAction(); 
            if(action.equals("ACTION_NAME")){ 
            	int eventType = intent.getIntExtra("eventType", 0);// 
            	Log.i(TAG, "========广播接收器=======当前登陆状态:"+eventType);
            	if(eventType == CALLBACKFLAG.GET_EVENT_RECLIST){
            		LoginEventControl.myProgress.dismiss();
            		DevRecordListAdapter devRecordAdapter = new DevRecordListAdapter(DevRecordListActivity.this,Constants.DEVRECORDINFOS);
            		recordListView.setAdapter(devRecordAdapter);
            		if(Constants.DEVRECORDINFOS.size() == 0){
            			MyUtil.commonToast(DevRecordListActivity.this, R.string.not_dev_recordfile);
            		}
            	}
            }
        } 
         
    }; 
	

}
