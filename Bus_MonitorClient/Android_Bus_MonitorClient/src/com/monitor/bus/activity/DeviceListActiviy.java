package com.monitor.bus.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.jniUtil.MyUtil;
import com.monitor.bus.adapter.MyExpandableListAdapter;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.consts.Constants.CALLBACKFLAG;
import com.monitor.bus.control.LoginEventControl;
import com.monitor.bus.model.DeviceInfo;

/**
 * 公交设备列表
 * 
 */
public class DeviceListActiviy extends BaseActivity { 

	private static String TAG = "BusDeviceList";
	private List<DeviceInfo> devList = null;
	private List<List<Map<String, String>>> childs;// 存储列表子菜单数据
	// private String parentId = null;
	ExpandableListView expandView;
	HashMap<String, String> groupMap;
	List<HashMap<String, String>> listView;
	MyExpandableListAdapter simpleExpandableAdapter;
	private DeviceInfo curCtlDevInfo; // 当前可操作的设备
	private HashMap<String, String> groupLocationMap;// 存储组及所处位置
	private ProgressDialog progressDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/* 初始化数据 */
		MyUtil.initTitleName(this, R.layout.dev_listview, R.string.dev_list);
		expandView = (ExpandableListView) findViewById(R.id.expandView);
		registerForContextMenu(expandView);
		if (0 == Constants.DEVICE_LIST.size()) {
			progressDialog = LoginEventControl.myProgress;
			progressDialog = new ProgressDialog(this);
			progressDialog.setTitle(R.string.loading_data_title);
			progressDialog.setMessage(this.getString(R.string.waiting));
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.show();
		}
		expandView.setOnGroupClickListener(new OnGroupClickListener() {// 组单击事件
					@Override
					public boolean onGroupClick(ExpandableListView parent,
							View v, int groupPosition, long id) { 
						if ("0".equals(listView.get(groupPosition).get(
								"login_status"))) {
							return true;
						} else if ("".equals(listView.get(groupPosition).get(
								"login_status"))) {
							loadDeviceInfoList(listView.get(groupPosition).get(
									"parent_id"));
							return true;
						} else {
							return false; 
						}
					}
				});

		expandView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				Intent intent = new Intent();
				DeviceInfo deviceInfo = devList.get(groupPosition);
				deviceInfo.setCurrentChn(childPosition +1);//设置设备通道号
				intent.putExtra("videoData", deviceInfo);
				intent.setClass(DeviceListActiviy.this, VideoActivity.class);
				startActivity(intent);
				return false;
			}
		});
		loadDeviceInfoList("0");
	}

	@Override
	public void onResume() {

		registerBoradcastReceiver();// 注册广播接收器
		super.onResume();
	}

	public void loadDeviceInfoList(String parentId) {
		devList = new ArrayList<DeviceInfo>();
		listView = new ArrayList<HashMap<String, String>>();
		childs = new ArrayList<List<Map<String, String>>>();
		groupLocationMap = new HashMap<String, String>();
		devList = getData(parentId);// 获取设备列表

		Log.i("BusDeviceList", "++++++devList.size()+++++++" + devList.size()); 

		for (int i = 0; i < devList.size(); i++) {
			List<Map<String, String>> child = new ArrayList<Map<String, String>>();
			groupMap = new HashMap<String, String>();
			if ("0".equals(devList.get(i).getIsDeviceGroup())) {
				groupMap.put(Constants.DEVLIST_GROUP_KEY, devList.get(i)
						.getDeviceName());
				groupMap.put("login_status", devList.get(i).getOnLine() + "");
				groupMap.put("parent_id", devList.get(i).getGroupId());
				groupLocationMap.put(devList.get(i).getGuId() + "", i + ""); 
//				groupLocationMap.put(devList.get(i).getNewGuId() + "", i + "");
				if (devList.get(i).getEncoderNumber() >= 1) {// 通道数大于1
					for (int k = 1; k <= devList.get(i).getEncoderNumber(); k++) {
						HashMap<String, String> childMap = new HashMap<String, String>();

						childMap.put(Constants.DEVLIST_CHILD_KEY,
								Constants.CHANNEL_PREFIX_NAME + k);

						child.add(childMap);
					}
				}
			} else {
				groupMap.put(Constants.DEVLIST_GROUP_KEY, devList.get(i)
						.getGroupName());
				groupMap.put("login_status", "");
				groupMap.put("parent_id", devList.get(i).getGroupId());
			}

			listView.add(groupMap);
			childs.add(child);
		}
		// 创建ExpandableList的Adapter容器
		// 参数: 1.上下文 2.一级集合 3.一级样式文件 4. 一级条目键值 5.一级显示控件名
		// 6. 二级集合 7. 二级样式 8.二级条目键值 9.二级显示控件名
		simpleExpandableAdapter = new MyExpandableListAdapter(this, listView,
				R.layout.dev_listview_groups,
				new String[] { Constants.DEVLIST_GROUP_KEY },
				new int[] { R.id.textGroup }, childs,
				R.layout.dev_listview_childs,
				new String[] { Constants.DEVLIST_CHILD_KEY },
				new int[] { R.id.textChild }, expandView);
		// 加入列表
		expandView.setAdapter(simpleExpandableAdapter);
	}

	/**
	 * 获取所属组的所有设备
	 * 
	 * @param parentId
	 * @return
	 */
	private List<DeviceInfo> getData(String parentId) {
		List<DeviceInfo> listBus = new ArrayList<DeviceInfo>();
		for (DeviceInfo busInfo : Constants.DEVICE_LIST) {
			if (parentId.equals(busInfo.getParentId())) {
				listBus.add(busInfo);
			}
		}
		return listBus;
	}

	public DeviceInfo getParentBusInfo(String parentId) {
		Iterator<DeviceInfo> itr = Constants.DEVICE_LIST.iterator();
		DeviceInfo busInfo = null;
		while (itr.hasNext()) {
			busInfo = itr.next();
			if (parentId.equals(busInfo.getGroupId())) {
				return busInfo;
			}
		}
		return null;
	}

	// --------呼出菜单的点击事件
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getOrder()) {
		case 0:// 进入电子地图
			Intent intent = new Intent();
			intent.putExtra(UserMapActivity.KEY_DEVICE_INFO, curCtlDevInfo);
			MyUtil.startMapActivity(this, intent);
			
////			if(MyUtil.isChina(this.getBaseContext())){
//			if(MyUtil.getDefMapIsBaiduMap(this)){
//				intent.setClass(this, UserMapActivity.class);
//			}else{
//				intent.setClass(this, UserGoogleMapActivity.class);
//			}
//			startActivity(intent);
			break;
		}
		curCtlDevInfo = null;
		return true;
	}

	// --------父菜单的长按事件，及相应的呼出菜单
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Log.i(TAG, "父菜单的长按事件！");
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		int type = ExpandableListView
				.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			curCtlDevInfo = (DeviceInfo) devList.get((int) info.id);
			if ("0".equals(curCtlDevInfo.getIsDeviceGroup())) {
				menu.setHeaderTitle(R.string.devOperate);
				menu.add(0, 0, 0, R.string.queryMap);
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (devList.size() != 0) {
			String parentId = devList.get(0).getParentId();
			if ("0".equals(parentId)) {
				Intent intent = new Intent();
				intent.setClass(this, MainListActivity.class);
				startActivity(intent);
				finish();
			} else {
				DeviceInfo busInfo = getParentBusInfo(parentId);

				loadDeviceInfoList(busInfo.getParentId());
			}
		} else {
			Intent intent = new Intent();
			intent.setClass(this, MainListActivity.class);
			startActivity(intent);
			finish();
		}
	}

	@Override
	protected void onStop() {
		unregisterReceiver(mBroadcastReceiver);
		super.onStop();
	}

	public void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction("ACTION_NAME");
		// 注册广播
		registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	// 广播接收器
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("ACTION_NAME")) {
				int eventType = intent.getIntExtra("eventType", 0);//
				Log.i(TAG, "========广播接收器=======当前登陆状态:" + eventType);
				if (!Constants.IS_CASCADE_SERVER && eventType == CALLBACKFLAG.GET_EVENT_DEVLIST
						|| Constants.IS_CASCADE_SERVER && eventType == CALLBACKFLAG.JNET_EET_EVENT_SERVER_LIST) {
					progressDialog.dismiss();
					loadDeviceInfoList("0");
				} else {
					String DEVID = intent.getStringExtra("devID");//
					String myLocation = groupLocationMap.get(DEVID);
					if(myLocation != null || "".equals(myLocation)){
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
}
