package com.monitor.bus.activity.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.monitor.bus.utils.MUtils;
import com.monitor.bus.activity.HomeActivity;
import com.monitor.bus.activity.R;
import com.monitor.bus.activity.UserMapActivity;
import com.monitor.bus.activity.RealTimeVideoActivity;
import com.monitor.bus.adapter.DeviceListAdapter;
import com.monitor.bus.bean.DeviceInfo;
import com.monitor.bus.bean.DeviceManager;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.consts.Constants.CALLBACKFLAG;
import com.monitor.bus.control.LoginEventControl;
import com.monitor.bus.utils.LogUtils;
import com.monitor.bus.utils.MUtils;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.style.UpdateAppearance;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

public class DeviceListFragment extends BaseFragment implements View.OnClickListener{
	private static String TAG = "BusDeviceList";
	private HashMap<String, String> groupLocationMap;// 存储组及所处位置
	
	private ProgressDialog progressDialog;
	View view;
	ListView lv_device;
	TextView tv_all,tv_online;
	DeviceManager deviceManager = DeviceManager.getInstance();
	
	DeviceListAdapter mDeviceListAdapter;
	ArrayList<DeviceInfo> deviceInfos;
	DeviceManager manager= DeviceManager.getInstance();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_video, container, false);
		mDeviceListAdapter=new DeviceListAdapter(getContext());
		setTitle();
		initView();
		updataByPid("0");

		registerBoradcastReceiver();// 注册广播接收器
		showWaittingDialog();
		return view;
	}

	@Override
	public void onDestroyView() {
		unregisterReceiver();
		super.onDestroyView();
	}

	private void setTitle() {
		TextView title = (TextView) view.findViewById(R.id.tilte_name);
		title.setText(getContext().getString(R.string.dev_list));
	}

	private void initView() {
		lv_device = (ListView) view.findViewById(R.id.lv_devicelist);
		lv_device.setAdapter(mDeviceListAdapter);
		tv_all=(TextView) view.findViewById(R.id.tv_all_device);
		tv_online=(TextView) view.findViewById(R.id.tv_online_device);
		tv_all.setOnClickListener(this);
		tv_online.setOnClickListener(this);
		
		lv_device.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				DeviceInfo info= mDeviceListAdapter.getDataByPosition(position);
				if(info.getIsDeviceGroup().equals("1")){
					//组
					updataByPid(info.getGroupId());
				}else{
					Intent intent=new Intent(getActivity(),RealTimeVideoActivity.class);
					info.setCurrentChn(1);
					intent.putExtra(RealTimeVideoActivity.KEY_DEVICE_INFO, info);
					startActivity(intent);
				}
			}
		});
		
	}
	
	
	private void updataByPid(String id){
		deviceInfos=manager.getListByPId(id);
		mDeviceListAdapter.setData(deviceInfos);
		mDeviceListAdapter.notifyDataSetChanged();
	}
	
	private void updataByList(ArrayList<DeviceInfo> list){
		deviceInfos=list;
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

	private void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction("ACTION_NAME");
		// 注册广播
		getContext().registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	private void unregisterReceiver() {
		getContext().unregisterReceiver(mBroadcastReceiver);
	}

	private void showWaittingDialog() {
		if (0 == DeviceManager.getInstance().getDeviceList().size()) {
			progressDialog = LoginEventControl.myProgress;
			progressDialog = new ProgressDialog(getContext());
			progressDialog.setTitle(R.string.loading_data_title);
			progressDialog.setMessage(this.getString(R.string.waiting));
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.show();
		}
	}

	private void disMissWaittingDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	// 广播接收器
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("ACTION_NAME")) {
				int eventType = intent.getIntExtra(Constants.WHAT_LOGIN_EVENT_TYPE, 0);//
				if (!Constants.IS_CASCADE_SERVER && eventType == CALLBACKFLAG.GET_EVENT_DEVLIST
						|| Constants.IS_CASCADE_SERVER && eventType == CALLBACKFLAG.JNET_EET_EVENT_SERVER_LIST) {
					//获取成功
					disMissWaittingDialog();
					updataByPid("0");
				} else {
					String DEVID = intent.getStringExtra("devID");//
					String myLocation = groupLocationMap.get(DEVID);
					if (myLocation != null || "".equals(myLocation)) {
						int location = Integer.parseInt(myLocation);
						if (eventType == 4096) {// 设备上线
						} else if (eventType == 8192) {// 设备离线
						}
					}
				}
			}
		}

	};

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.tv_all_device:
			updataByPid("0");
			break;
		case R.id.tv_online_device:
			updataByList(manager.getOnlineDevice());
			break;
		default:
			break;
		}
		
	}
}
