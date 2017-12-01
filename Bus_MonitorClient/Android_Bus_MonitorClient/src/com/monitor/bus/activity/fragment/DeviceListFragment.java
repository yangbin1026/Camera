package com.monitor.bus.activity.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.jniUtil.MyUtil;
import com.monitor.bus.activity.HomeActivity;
import com.monitor.bus.activity.R;
import com.monitor.bus.activity.UserMapActivity;
import com.monitor.bus.activity.VideoActivity;
import com.monitor.bus.adapter.MyExpandableListAdapter;
import com.monitor.bus.bean.DeviceManager;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.consts.Constants.CALLBACKFLAG;
import com.monitor.bus.control.LoginEventControl;
import com.monitor.bus.model.DeviceInfo;
import com.monitor.bus.utils.LogUtils;
import com.monitor.bus.utils.MUtils;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

public class DeviceListFragment extends BaseFragment implements View.OnClickListener{
	private static String TAG = "BusDeviceList";

	private List<DeviceInfo> deviceList = null;

	private List<List<Map<String, String>>> childs;// 存储列表子菜单数据
	private HashMap<String, String> groupLocationMap;// 存储组及所处位置

	List<HashMap<String, String>> list;
	HashMap<String, String> groupMap;
	MyExpandableListAdapter simpleExpandableAdapter;
	private ProgressDialog progressDialog;
	ListView lv_device;
	View view;
	TextView tv_all,tv_online;

	DeviceManager deviceManager = DeviceManager.getInstance();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_video, container, false);
		setTitle();
		initView();

		registerBoradcastReceiver();// 注册广播接收器

		showWaittingDialog();
		loadDeviceInfoList("0");
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
		tv_all=(TextView) view.findViewById(R.id.tv_all_device);
		tv_online=(TextView) view.findViewById(R.id.tv_online_device);
		tv_all.setOnClickListener(this);
		tv_online.setOnClickListener(this);
		/*
		 * lv_device.setOnGroupClickListener(new OnGroupClickListener() {//
		 * 组单击事件
		 * 
		 * @Override public boolean onGroupClick(ExpandableListView parent, View
		 * v, int groupPosition, long id) { if
		 * ("0".equals(list.get(groupPosition).get("login_status"))) { return
		 * true; } else if
		 * ("".equals(list.get(groupPosition).get("login_status"))) {
		 * loadDeviceInfoList(list.get(groupPosition).get("parent_id")); return
		 * true; } else { return false; } } });
		 * 
		 * lv_device.setOnChildClickListener(new OnChildClickListener() {
		 * 
		 * @Override public boolean onChildClick(ExpandableListView parent, View
		 * v, int groupPosition, int childPosition, long id) { Intent intent =
		 * new Intent(); DeviceInfo deviceInfo = deviceList.get(groupPosition);
		 * deviceInfo.setCurrentChn(childPosition + 1);// 设置设备通道号
		 * intent.putExtra("videoData", deviceInfo);
		 * intent.setClass(getContext(), VideoActivity.class);
		 * startActivity(intent); return false; } });
		 */
	}

	public void loadDeviceInfoList(String parentId) {
		list = new ArrayList<HashMap<String, String>>();
		childs = new ArrayList<List<Map<String, String>>>();
		groupLocationMap = new HashMap<String, String>();
		deviceList = deviceManager.getDeviceInfoByParentId(parentId);

		MUtils.debugToast(getContext(), "parentDeviceSize:" + deviceList.size());
		for (int i = 0; i < deviceList.size(); i++) {
			List<Map<String, String>> child = new ArrayList<Map<String, String>>();
			groupMap = new HashMap<String, String>();
			if ("0".equals(deviceList.get(i).getIsDeviceGroup())) {
				// 设备组
				groupMap.put(Constants.DEVLIST_GROUP_KEY, deviceList.get(i).getDeviceName());

				groupMap.put("login_status", deviceList.get(i).getOnLine() + "");
				groupMap.put("parent_id", deviceList.get(i).getGroupId());
				groupLocationMap.put(deviceList.get(i).getGuId() + "", i + "");

				if (deviceList.get(i).getEncoderNumber() >= 1) {// 通道数大于1
					for (int k = 1; k <= deviceList.get(i).getEncoderNumber(); k++) {
						HashMap<String, String> childMap = new HashMap<String, String>();

						childMap.put(Constants.DEVLIST_CHILD_KEY, Constants.CHANNEL_PREFIX_NAME + k);

						child.add(childMap);
					}
				}
			} else {
				// 设备
				groupMap.put(Constants.DEVLIST_GROUP_KEY, deviceList.get(i).getGroupName());
				groupMap.put("login_status", "");
				groupMap.put("parent_id", deviceList.get(i).getGroupId());
			}

			list.add(groupMap);
			childs.add(child);
		}
		// 创建ExpandableList的Adapter容器
		// 参数: 1.上下文 2.一级集合 3.一级样式文件 4. 一级条目键值 5.一级显示控件名
		// 6. 二级集合 7. 二级样式 8.二级条目键值 9.二级显示控件名
		// simpleExpandableAdapter = new MyExpandableListAdapter(
		// getContext(),
		// list, R.layout.dev_listview_groups,new String[] {
		// Constants.DEVLIST_GROUP_KEY }, new int[] { R.id.textGroup },
		// childs,R.layout.dev_listview_childs, new String[] {
		// Constants.DEVLIST_CHILD_KEY },new int[] { R.id.textChild },
		// lv_device);
		// 加入列表
		// lv_device.setAdapter(simpleExpandableAdapter);
	}

	public DeviceInfo getParentBusInfo(String parentId) {
		Iterator<DeviceInfo> itr = DeviceManager.getInstance().getDeviceList().iterator();
		DeviceInfo busInfo = null;
		while (itr.hasNext()) {
			busInfo = itr.next();
			if (parentId.equals(busInfo.getGroupId())) {
				return busInfo;
			}
		}
		return null;
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
				int eventType = intent.getIntExtra("eventType", 0);//
				LogUtils.i(TAG, "========广播接收器=======当前登陆状态:" + eventType);
				if (!Constants.IS_CASCADE_SERVER && eventType == CALLBACKFLAG.GET_EVENT_DEVLIST
						|| Constants.IS_CASCADE_SERVER && eventType == CALLBACKFLAG.JNET_EET_EVENT_SERVER_LIST) {
					disMissWaittingDialog();
					loadDeviceInfoList("0");
				} else {
					String DEVID = intent.getStringExtra("devID");//
					String myLocation = groupLocationMap.get(DEVID);
					if (myLocation != null || "".equals(myLocation)) {
						int location = Integer.parseInt(myLocation);
						if (eventType == 4096) {// 设备上线
							simpleExpandableAdapter.updateView(location, true);
						} else if (eventType == 8192) {// 设备离线
							simpleExpandableAdapter.updateView(location, false);
						}
					}
				}
			}
		}

	};

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}
}
