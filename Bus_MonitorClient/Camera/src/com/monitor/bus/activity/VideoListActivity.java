package com.monitor.bus.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.monitor.bus.Constants;
import com.monitor.bus.Constants.CALLBACKFLAG;
import com.monitor.bus.adapter.DevRecordListAdapter;
import com.monitor.bus.bean.RecordDBInfo;
import com.monitor.bus.bean.RecordInfo;
import com.monitor.bus.bean.manager.RecordManager;
import com.monitor.bus.database.DatabaseHelper;
import com.monitor.bus.utils.LogUtils;
import com.monitor.bus.utils.MUtils;

/**
 * 远程录像文件列表
 */
public class VideoListActivity extends BaseActivity {
	public static final String EXTRA_RECODINFO = "recodInfo";
	private static String TAG = "VideoListActivity";
	boolean isRegisted=false;

	private RecordInfo extra_recordInfo;
	ProgressDialog myProgressDialog;

	private ListView lv_recoder;
	private Context mContext;
	
	DatabaseHelper dbHelper;
	ArrayList<RecordInfo> testLocalListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext=this;
		setContentView(R.layout.local_listview);
		Intent intent = getIntent();
		extra_recordInfo = (RecordInfo) intent.getSerializableExtra(EXTRA_RECODINFO);
		initView();
		initData();

	}

	@Override
	protected void onDestroy() {
		if(isRegisted){
			unregisterReceiver(mBroadcastReceiver);
		}
		File devRecordFile = new File(Constants.DEVRECORD_PASTH);
		if (devRecordFile.exists()) { // 存在删除
			devRecordFile.delete();
		}
		RecordManager.getInstance(mContext).clean();
		super.onDestroy();
	}

	private void initView() {
		lv_recoder = (ListView) findViewById(R.id.lv_recoder);
		lv_recoder.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				RecordInfo deviceInfo = testLocalListView.get(position);
				deviceInfo.setDeviceId(extra_recordInfo.getDeviceId());
				Intent intent = new Intent();
				intent.putExtra(ReplayActivity.EXTRA_FIELPATH, deviceInfo.getPath()+deviceInfo.getFileName());
				intent.putExtra(ReplayActivity.EXTRA_ID, deviceInfo.getDeviceId());
				intent.setClass(mContext, ReplayActivity.class);
				startActivity(intent);
			}
		});
	}

	private void initData() {
		if(extra_recordInfo.isLocalVideo()){
			//本地
			dbHelper = new DatabaseHelper(this, Constants.DATABASE_NAME);
			testLocalListView = dbHelper.queryAllDBList(extra_recordInfo.getStartTime(), extra_recordInfo.getEndTime());
			LogUtils.d(TAG,"initDate():size="+testLocalListView.size());
			DevRecordListAdapter devRecordAdapter = new DevRecordListAdapter(this, testLocalListView);
			lv_recoder.setAdapter(devRecordAdapter);
		}else{
			//远程
			registerBoradcastReceiver();
			if (RecordManager.getInstance(mContext).getAllSize()==0) {
				myProgressDialog = ProgressDialog.show(this, getString(R.string.loading_data_title),
						getString(R.string.waiting), true, true);
			}else{
				DevRecordListAdapter devRecordAdapter = new DevRecordListAdapter(this, RecordManager.getInstance(mContext).getAll());
				lv_recoder.setAdapter(devRecordAdapter);
			}
		}
		
		
	}

	private void registerBoradcastReceiver() {
		isRegisted=true;
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(Constants.ACTION_LOGIN_EVENT);
		registerReceiver(mBroadcastReceiver, myIntentFilter);
	}
	
	/**
	 * 获取录像listReceiver，远程录像
	 */
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Constants.ACTION_LOGIN_EVENT)) {
				int eventType = intent.getIntExtra(Constants.WHAT_LOGIN_EVENT_TYPE, 0);
				LogUtils.i(TAG, "========广播接收器=======当前登陆状态:" + eventType);
				if (eventType == CALLBACKFLAG.GET_EVENT_RECLIST) {
					myProgressDialog.dismiss();
					DevRecordListAdapter devRecordAdapter = new DevRecordListAdapter(VideoListActivity.this,
							RecordManager.getInstance(mContext).getAll());
					lv_recoder.setAdapter(devRecordAdapter);
					if (RecordManager.getInstance(mContext).getAllSize() == 0) {
						MUtils.commonToast(mContext, R.string.not_dev_recordfile);
					}
				}
			}
		}

	};

}
