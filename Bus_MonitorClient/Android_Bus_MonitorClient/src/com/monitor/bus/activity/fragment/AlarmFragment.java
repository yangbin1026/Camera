package com.monitor.bus.activity.fragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.monitor.bus.activity.FilterOptionActivity;
import com.monitor.bus.activity.R;
import com.monitor.bus.activity.VideoActivity;
import com.monitor.bus.adapter.AlarmListAdapter;
import com.monitor.bus.adapter.AlarmSelectorAdapter;
import com.monitor.bus.bean.AlarmManager;
import com.monitor.bus.bean.DeviceManager;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.model.AlarmInfo;
import com.monitor.bus.model.DeviceInfo;
import com.monitor.bus.utils.MyUtils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat.LayoutParams;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 警报页面
 * 
 * @author Administrator
 *
 */
public class AlarmFragment extends BaseFragment implements View.OnClickListener {
	private ListView alarmListView;
	private DeviceInfo currentDeviceInfo;
	private List<AlarmInfo> alarms;
	View view;
	
	AlarmManager alarmManger=AlarmManager.getInstance();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_alarm, container, false);
		setTitle();
		initData();
		initView();
		return view;
	}

	private void setTitle() {
		TextView title = (TextView) view.findViewById(R.id.tilte_name);
		title.setText(getContext().getString(R.string.alarm_list));
		ImageButton ib_setting = (ImageButton) view.findViewById(R.id.ib_setting);
		ib_setting.setVisibility(View.VISIBLE);
		ib_setting.setOnClickListener(this);
	}

	private void initData() {
		alarms = new ArrayList<AlarmInfo>();
		alarms.addAll(AlarmManager.getInstance().getAlarmList());
		for (int i = 0; i < alarms.size(); i++) {
			// 报警类型是移动，震动提示用户
			if (alarms.get(i).getAlarmString().contains(getString(R.string.motion_detection))) {
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
				currentDeviceInfo = getDeviceInfo(alarmInfo.getdeviceId());
				Log.i("-----", "当前设备信息:" + currentDeviceInfo);
				if (alarms.get(position).getAlarmString().contains(getString(R.string.motion_detection))) {
					Log.i("------------------------", "移动侦测:" + alarmInfo.getChannelId());
					currentDeviceInfo.setCurrentChn(alarmInfo.getChannelId() - 1);
				} else {
					currentDeviceInfo.setCurrentChn(alarmInfo.getChannelId());
				}

				if (currentDeviceInfo.getDeviceName() == null) {
					currentDeviceInfo.setDeviceName(alarmInfo.getdeviceId());
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
		Iterator<DeviceInfo> itr = DeviceManager.getInstance().getDeviceList().iterator();
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
		View contentView = LayoutInflater.from(getContext()).inflate(R.layout.pop_alarm, null);
		PopupWindow mPopWindow = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				true);
		mPopWindow.setContentView(contentView);
		ListView lv_alarm_pop = (ListView) contentView.findViewById(R.id.lv_alarm_choose);
		AlarmSelectorAdapter adapter = new AlarmSelectorAdapter(getContext(),
				alarmManger.getAlarmTypeString());
		lv_alarm_pop.setAdapter(adapter);
		lv_alarm_pop.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int type=alarmManger.getAlarmType()[arg2];
				MyUtils.toast(getContext(), ""+type);
			}
		});

		// 显示PopupWindow
		mPopWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
	}

}
