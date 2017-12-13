package com.monitor.bus.activity.fragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.monitor.bus.activity.R;
import com.monitor.bus.activity.RealTimeVideoActivity;
import com.monitor.bus.adapter.AlarmListAdapter;
import com.monitor.bus.adapter.AlarmSelectorAdapter;
import com.monitor.bus.bean.AlarmInfo;
import com.monitor.bus.bean.AlarmManager;
import com.monitor.bus.bean.DeviceInfo;
import com.monitor.bus.bean.DeviceManager;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.utils.LogUtils;
import com.monitor.bus.utils.MUtils;

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
import android.widget.Button;

/**
 * 警报页面
 * 
 * @author Administrator
 *
 */
public class AlarmFragment extends BaseFragment implements View.OnClickListener {
	private ListView alarmListView;
	private AlarmListAdapter mAlarmAdapter;

	private DeviceInfo currentDeviceInfo;
	private List<AlarmInfo> alarmList = new ArrayList<AlarmInfo>();
	View contentView;
	View popContentView;
	PopupWindow mPopWindow;
	
	AlarmManager alarmManger;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = inflater.inflate(R.layout.fragment_alarm, container, false);
		alarmManger = AlarmManager.getInstance(getContext());
		setTitle();
		initData();
		initView();
		return contentView;
	}

	private void setTitle() {
		TextView title = (TextView) contentView.findViewById(R.id.tilte_name);
		title.setText(getContext().getString(R.string.alarm_list));
		Button bt_setting = (Button) contentView.findViewById(R.id.bt_setting);
		bt_setting.setBackgroundDrawable(null);
		bt_setting.setText("类型");
		bt_setting.setVisibility(View.VISIBLE);
		bt_setting.setOnClickListener(this);
	}

	private void initData() {
		MUtils.toast(getContext(), "addSize:" + alarmManger.getAlarmList().size());
		alarmList.addAll(alarmManger.getAlarmList());
		for (int i = 0; i < alarmList.size(); i++) {
			// 报警类型是移动，震动提示用户
			if (alarmList.get(i).getAlarmString().contains(getString(R.string.motion_detection))) {
				MUtils.Vibrate(getActivity(), 1000);
				break;
			}
		}
	}

	private void initView() {
		alarmListView = (ListView) contentView.findViewById(R.id.alarmListView);
		mAlarmAdapter = new AlarmListAdapter(getContext(), alarmList);
		popContentView = LayoutInflater.from(getContext()).inflate(R.layout.pop_alarm, null);
		mPopWindow = new PopupWindow(popContentView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		mPopWindow.setContentView(popContentView);

		alarmListView.setAdapter(mAlarmAdapter);

		alarmListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

				AlarmInfo alarmInfo = alarmList.get(position);
				currentDeviceInfo = getDeviceInfo(alarmInfo.getdeviceId());
				LogUtils.i("-----", "当前设备信息:" + currentDeviceInfo);
				if (alarmList.get(position).getAlarmString().contains(getString(R.string.motion_detection))) {
					currentDeviceInfo.setCurrentChn(alarmInfo.getChannelId() - 1);
				} else {
					currentDeviceInfo.setCurrentChn(alarmInfo.getChannelId());
				}

				if (currentDeviceInfo.getDeviceName() == null) {
					currentDeviceInfo.setDeviceName(alarmInfo.getdeviceId());
				}
				Intent intent = new Intent();
				intent.putExtra(RealTimeVideoActivity.KEY_DEVICE_INFO, currentDeviceInfo);
				intent.setClass(getContext(), RealTimeVideoActivity.class);
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
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.bt_setting:
			showPopWindow();
			break;

		default:
			break;
		}

	}

	private void showPopWindow() {
		ListView lv_alarm_pop = (ListView) popContentView.findViewById(R.id.lv_alarm_choose);
		AlarmSelectorAdapter adapter = new AlarmSelectorAdapter(getContext(), alarmManger.getAlarmTypeString());
		lv_alarm_pop.setAdapter(adapter);
		lv_alarm_pop.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int type = alarmManger.getAlarmType()[arg2];
				mAlarmAdapter.setData(alarmManger.getAlarmListbyType(type));
				mAlarmAdapter.notifyDataSetChanged();
				mPopWindow.dismiss();
			}
		});

		// 显示PopupWindow
		mPopWindow.showAtLocation(contentView, Gravity.TOP, 0, 0);
	}

}
