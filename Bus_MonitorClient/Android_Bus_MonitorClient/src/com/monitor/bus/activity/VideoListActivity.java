package com.monitor.bus.activity;

import java.io.File;

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

import com.monitor.bus.adapter.DevRecordListAdapter;
import com.monitor.bus.bean.DevRecordInfo;
import com.monitor.bus.bean.RecodInfo;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.consts.Constants.CALLBACKFLAG;
import com.monitor.bus.utils.LogUtils;
import com.monitor.bus.utils.MUtils;

/**
 * 远程录像文件列表
 */
public class VideoListActivity extends BaseActivity {
	public static final String EXTRA_RECODINFO = "recodInfo";
	private static String TAG = "VideoListActivity";

	private RecodInfo recodinfo;
	ProgressDialog myProgressDialog;

	private ListView lv_recoder;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local_listview);
		Intent intent = getIntent();
		recodinfo = (RecodInfo) intent.getSerializableExtra(EXTRA_RECODINFO);

		registerBoradcastReceiver();// 注册广播接收器
		initView();
		initData();

	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mBroadcastReceiver);
		File devRecordFile = new File(Constants.DEVRECORD_PASTH);
		if (devRecordFile.exists()) { // 存在删除
			devRecordFile.delete();
		}
		Constants.RECORD_LIST.clear();
		super.onDestroy();
	}

	private void initView() {
		lv_recoder = (ListView) findViewById(R.id.lv_recoder);
		lv_recoder.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				DevRecordInfo devRecordInfo = Constants.RECORD_LIST.get(position);
				devRecordInfo.setGuId(recodinfo.getDeviceId());
				Intent intent = new Intent();
				intent.putExtra("devRecordInfo", devRecordInfo);
				intent.setClass(mContext, ReplayActivity.class);
				startActivity(intent);
			}
		});
	}

	private void initData() {
		DevRecordListAdapter devRecordAdapter = new DevRecordListAdapter(this, Constants.RECORD_LIST);
		lv_recoder.setAdapter(devRecordAdapter);
		if (0 == Constants.RECORD_LIST.size()) {
			myProgressDialog = ProgressDialog.show(this, getString(R.string.loading_data_title),
					getString(R.string.waiting), true, true);
		}
	}

	private void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction("ACTION_NAME");
		registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("ACTION_NAME")) {
				int eventType = intent.getIntExtra(Constants.WHAT_LOGIN_EVENT_TYPE, 0);
				LogUtils.i(TAG, "========广播接收器=======当前登陆状态:" + eventType);
				if (eventType == CALLBACKFLAG.GET_EVENT_RECLIST) {
					myProgressDialog.dismiss();
					DevRecordListAdapter devRecordAdapter = new DevRecordListAdapter(VideoListActivity.this,
							Constants.RECORD_LIST);
					lv_recoder.setAdapter(devRecordAdapter);
					if (Constants.RECORD_LIST.size() == 0) {
						MUtils.commonToast(mContext, R.string.not_dev_recordfile);
					}
				}
			}
		}

	};

}
