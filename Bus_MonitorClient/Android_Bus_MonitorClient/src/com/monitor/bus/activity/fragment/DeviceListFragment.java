package com.monitor.bus.activity.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.monitor.bus.Constants;
import com.monitor.bus.utils.MUtils;
import com.monitor.bus.view.dialog.ShapeLoadingDialog.ShapeLoadingDialog;
import com.monitor.bus.Constants.CALLBACKFLAG;
import com.monitor.bus.activity.HomeActivity;
import com.monitor.bus.activity.R;
import com.monitor.bus.activity.UserMapActivity;
import com.monitor.bus.activity.RealTimeVideoActivity;
import com.monitor.bus.activity.UserGoogleMapActivity;
import com.monitor.bus.adapter.DeviceListAdapter;
import com.monitor.bus.bean.DeviceInfo;
import com.monitor.bus.bean.manager.DeviceManager;
import com.monitor.bus.control.LoginEventControl;
import com.monitor.bus.utils.LogUtils;
import com.monitor.bus.utils.MUtils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.style.UpdateAppearance;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

public class DeviceListFragment extends BaseFragment implements View.OnClickListener {
	private static String TAG = "DeviceListFragment";

	private ShapeLoadingDialog progressDialog;
	View view;
	ListView lv_device;
	TextView tv_all, tv_online;

	DeviceListAdapter mDeviceListAdapter;
	ArrayList<DeviceInfo> deviceInfos;
	DeviceManager deviceManager;
	Handler mHandler = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_video, container, false);
		deviceManager = DeviceManager.getInstance();
		setTitle();
		initView();
		initData();
		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	private void setTitle() {
		TextView title = (TextView) view.findViewById(R.id.tilte_name);
		title.setText(getContext().getString(R.string.dev_list));

		// Button bt_setting = (Button) view.findViewById(R.id.bt_setting);
		// bt_setting.setBackgroundDrawable(null);
		// bt_setting.setText(R.string.test);
		// bt_setting.setVisibility(View.VISIBLE);
		// bt_setting.setOnClickListener(this);
	}

	private void initView() {
		lv_device = (ListView) view.findViewById(R.id.lv_devicelist);
		tv_all = (TextView) view.findViewById(R.id.tv_all_device);
		tv_online = (TextView) view.findViewById(R.id.tv_online_device);
		tv_all.setOnClickListener(this);
		tv_online.setOnClickListener(this);

		lv_device.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				DeviceInfo info = mDeviceListAdapter.getDataByPosition(position);
				if (info.issDeviceGroup()) {
					// 组
					updataByPid(info.getGroupId());
				} else {
					Intent intent = new Intent(getActivity(), RealTimeVideoActivity.class);
					info.setCurrentChn(1);
					intent.putExtra(RealTimeVideoActivity.KEY_DEVICE_INFO, info);
					startActivity(intent);
				}
			}
		});

	}

	private void initData() {
		mDeviceListAdapter = new DeviceListAdapter(getContext());
		lv_device.setAdapter(mDeviceListAdapter);
		updataByPid("0");
	}

	private void updataByPid(String parent) {
		deviceInfos = deviceManager.getListByPId(parent);
		mDeviceListAdapter.setData(deviceInfos);
		mDeviceListAdapter.notifyDataSetChanged();
		if (deviceInfos.size() <= 0) {
			showWaittingDialog();
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					deviceManager.resetInfos();
					updataByPid("0");
				}
			}, 2000);
		} else {
			disMissWaittingDialog();
		}
	}

	private void updataByList(ArrayList<DeviceInfo> list) {
		deviceInfos = list;
		mDeviceListAdapter.setData(deviceInfos);
		mDeviceListAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onBackPress() {
		/*
		 * if (deviceList.size() != 0 &&
		 * "0".equals(deviceList.get(0).getParentId())) { DeviceInfo busInfo =
		 * getParentBusInfo(deviceList.get(0).getParentId()); if(busInfo!=null){
		 * loadDeviceInfoList(busInfo.getParentId()); } return true; }
		 */
		return false;
	}

	private void showWaittingDialog() {
		if (progressDialog != null) {
			progressDialog.show();
			return;
		}
		if (progressDialog == null) {
			progressDialog = new ShapeLoadingDialog.Builder(getContext()).cancelable(false)
					.canceledOnTouchOutside(false).loadText(R.string.loading_data_title).build();
		}
		progressDialog.setCancelable(true);
		progressDialog.setCanceledOnTouchOutside(true);
		progressDialog.show();
	}

	private void disMissWaittingDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.tv_all_device:
			updataByPid("0");
			tv_all.setTextColor(getContext().getResources().getColor(R.color.black));
			tv_online.setTextColor(getContext().getResources().getColor(R.color.bg_my_gray));
			break;
		case R.id.tv_online_device:
			updataByList(deviceManager.getOnlineDevice());
			tv_online.setTextColor(getContext().getResources().getColor(R.color.black));
			tv_all.setTextColor(getContext().getResources().getColor(R.color.bg_my_gray));

			break;
		case R.id.bt_setting:
			DeviceInfo info = DeviceManager.getInstance().getDeviceList().get(0);
			Intent intent = new Intent();
			intent.putExtra(RealTimeVideoActivity.KEY_DEVICE_INFO, info);
			intent.setClass(getContext(), UserGoogleMapActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}

	}
}
