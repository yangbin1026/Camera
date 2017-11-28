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
import com.monitor.bus.model.DeviceInfo;
import com.monitor.bus.utils.MyUtils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 警报页面
 * @author Administrator
 *
 */
public class AlarmFragment extends BaseFragment implements View.OnClickListener{
	private ListView alarmListView;
	private DeviceInfo currentDeviceInfo;
	private List<AlarmInfo> alarms;
	View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_alarm, container, false);
		setTitle();
		initData();
		initView();
		return view;
	}

	private void setTitle() {
		TextView title= (TextView) view.findViewById(R.id.tilte_name);
		title.setText(getContext().getString(R.string.alarm_list));
		ImageButton ib_setting=(ImageButton) view.findViewById(R.id.ib_setting);
		ib_setting.setVisibility(View.VISIBLE);
		ib_setting.setOnClickListener(this);
	}

	private void initData() {
		alarms = new ArrayList<AlarmInfo>();
		alarms.addAll(Constants.ALARM_LIST);
		for (int i = 0; i < alarms.size(); i++) {
			//报警类型是移动，震动提示用户
			if (alarms.get(i).getExpresion().contains(getString(R.string.motion_detection))) {
				MyUtils.Vibrate(getActivity(), 1000);
				break;
			}
		}
	}

	private void initView() {
		alarmListView = (ListView) view.findViewById(R.id.alarmListView);
		alarmListView.setAdapter(new AlarmListAdapter(getContext(), alarms));
		alarmListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

				AlarmInfo alarmInfo = alarms.get(position);
				currentDeviceInfo = getDeviceInfo(alarmInfo.getGuId());
				Log.i("-----", "当前设备信息:" + currentDeviceInfo);
				if (alarms.get(position).getExpresion().contains(getString(R.string.motion_detection))) {
					Log.i("------------------------", "移动侦测:" + alarmInfo.getCurrentChn());
					currentDeviceInfo.setCurrentChn(alarmInfo.getCurrentChn() - 1);
				} else {
					currentDeviceInfo.setCurrentChn(alarmInfo.getCurrentChn());
				}

				if (currentDeviceInfo.getDeviceName() == null) {
					currentDeviceInfo.setDeviceName(alarmInfo.getGuId());
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
	public DeviceInfo getDeviceInfo(String guId) {
		Iterator<DeviceInfo> itr = Constants.DEVICE_LIST.iterator();
		DeviceInfo deviceInfo = null;
		while (itr.hasNext()) {
			deviceInfo = itr.next();
			if (guId.equals(deviceInfo.getGuId())) {
				return deviceInfo;
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
			startFilterActivity();
			break;
		case 2:

			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/* 过滤报警信息 */
	private void startFilterActivity() {
		Intent intent = new Intent(getContext(), FilterOptionActivity.class);
		startActivity(intent);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.ib_setting:
			showPopWindow();
			break;

		default:
			break;
		}
		
	}

	private void showPopWindow() {
		// TODO Auto-generated method stub
		
	}

}
