package com.monitor.bus.activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.jniUtil.MyUtil;
import com.monitor.bus.adapter.AlarmListAdapter;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.model.AlarmInfo;
import com.monitor.bus.model.DeviceInfo;
import com.monitor.bus.utils.MyUtils;

/**
 * 报警信息list
 */
public class AlarmListActivity extends BaseActivity {
	private ListView alarmListView;
	// private static String TAG = "AlarmListActivity";
	private DeviceInfo currentDeviceInfo;
	private List<AlarmInfo> alarms;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyUtil.initTitleName(this, R.layout.alarm_listview, R.string.alarm_list);
		alarmListView = (ListView) findViewById(R.id.alarmListView);
		alarmListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {

				AlarmInfo devRecordInfo = alarms.get(position);
				currentDeviceInfo = getBusInfo(devRecordInfo.getGuId());
				/*Toast.makeText(getBaseContext(), currentDeviceInfo.getDeviceName(), 0).show();*/
				Log.i("-----", "当前设备信息:" + currentDeviceInfo);
				Log.i("------", "alarms;;;;;;;;;;;" + alarms);
				if (alarms.get(position).getExpresion().contains(getString(R.string.motion_detection))) {
					Log.i("------------------------",
							"移动侦测:" + devRecordInfo.getCurrentChn());
					currentDeviceInfo.setCurrentChn(devRecordInfo
							.getCurrentChn() - 1);
				} else {
					currentDeviceInfo.setCurrentChn(devRecordInfo
							.getCurrentChn());
				}

				if (currentDeviceInfo.getDeviceName() == null) {
					currentDeviceInfo.setDeviceName(devRecordInfo.getGuId());
				}
				Intent intent = new Intent();
				intent.putExtra("videoData", currentDeviceInfo);
				intent.setClass(AlarmListActivity.this, VideoActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onResume() {
		alarms = new ArrayList<AlarmInfo>();
		alarms.addAll(Constants.ALARM_LIST);
		
		for (int i = 0; i < alarms.size(); i++) {
			if (alarms.get(i).getExpresion().contains(getString(R.string.motion_detection))) {
				MyUtils
						.Vibrate(AlarmListActivity.this, 1000);// 震动提示用户
			}
		}
		
		AlarmListAdapter devRecordAdapter = new AlarmListAdapter(this, alarms);
		alarmListView.setAdapter(devRecordAdapter);
		super.onResume();
	}

	/**
	 * 根据id获取设备名称
	 * 
	 * @param guId
	 * @return
	 */
	public DeviceInfo getBusInfo(String guId) {
		Iterator<DeviceInfo> itr = Constants.DEVICE_LIST.iterator();
		DeviceInfo busInfo = null;
		while (itr.hasNext()) {
			busInfo = itr.next();
			if (guId.equals(busInfo.getGuId())) {
				return busInfo;
			}
		}
		return null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			onResume();
			break;
		case 1:
			onFilter();
			break;
		case 2:

			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/* 过滤报警信息 */
	private void onFilter() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, FilterOptionActivity.class);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, R.string.refresh);
		menu.add(0, 1, 1, R.string.filter);
		menu.add(0, 2, 2, R.string.cancel);
		return super.onCreateOptionsMenu(menu);
	}
}
