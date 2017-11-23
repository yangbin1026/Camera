package com.monitor.bus.activity.fragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.monitor.bus.activity.FilterOptionActivity;
import com.monitor.bus.activity.R;
import com.monitor.bus.activity.VideoActivity;
import com.monitor.bus.adapter.AlarmListAdapter;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.model.AlarmInfo;
import com.monitor.bus.model.BusDeviceInfo;
import com.monitor.bus.utils.MyUtils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 警报页面
 * @author Administrator
 *
 */
public class AlarmFragment extends BaseFragment {
	private ListView alarmListView;
	private BusDeviceInfo currentDeviceInfo;
	private List<AlarmInfo> alarms;
	View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_alarm, container, false);
		initData();
		initView();
		return view;
	}

	private void initData() {
		alarms = new ArrayList<AlarmInfo>();
		alarms.addAll(Constants.ALARMINFOS);

		for (int i = 0; i < alarms.size(); i++) {
			//报警类型是移动，震动提示用户
			if (alarms.get(i).getExpresion().contains(getString(R.string.motion_detection))) {
				MyUtils.Vibrate(getActivity(), 1000);
			}
		}
	}

	private void initView() {
		alarmListView = (ListView) view.findViewById(R.id.alarmListView);
		AlarmListAdapter devRecordAdapter = new AlarmListAdapter(getContext(), alarms);
		alarmListView.setAdapter(devRecordAdapter);
		alarmListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

				AlarmInfo devRecordInfo = alarms.get(position);
				currentDeviceInfo = getBusInfo(devRecordInfo.getGuId());
				Log.i("-----", "当前设备信息:" + currentDeviceInfo);
				Log.i("------", "alarms;;;;;;;;;;;" + alarms);
				if (alarms.get(position).getExpresion().contains(getString(R.string.motion_detection))) {
					Log.i("------------------------", "移动侦测:" + devRecordInfo.getCurrentChn());
					currentDeviceInfo.setCurrentChn(devRecordInfo.getCurrentChn() - 1);
				} else {
					currentDeviceInfo.setCurrentChn(devRecordInfo.getCurrentChn());
				}

				if (currentDeviceInfo.getDeviceName() == null) {
					currentDeviceInfo.setDeviceName(devRecordInfo.getGuId());
				}
				Intent intent = new Intent();
				intent.putExtra("videoData", currentDeviceInfo);
				intent.setClass(getContext(), VideoActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * 根据id获取设备名称
	 * 
	 * @param guId
	 * @return
	 */
	public BusDeviceInfo getBusInfo(String guId) {
		Iterator<BusDeviceInfo> itr = Constants.BUSDEVICEDATA.iterator();
		BusDeviceInfo busInfo = null;
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
			startFilterOptionActivity();
			break;
		case 2:

			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/* 过滤报警信息 */
	private void startFilterOptionActivity() {
		Intent intent = new Intent(getContext(), FilterOptionActivity.class);
		startActivity(intent);
	}

}
