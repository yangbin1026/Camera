package com.monitor.bus.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.MarkerOptions.MarkerAnimateType;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.inner.GeoPoint;
import com.jniUtil.GpsToBaiduUtil;
import com.jniUtil.MyUtil;
import com.monitor.bus.adapter.SpinnerBusAdapter;
import com.monitor.bus.bdmap.ErrorCodeReceiver;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.consts.Constants.CALLBACKFLAG;
import com.monitor.bus.model.BusDeviceInfo;

import Decoder.BASE64Decoder;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.Spinner;

/**
 * 地图
 */
public class UserMapActivity extends Activity {
	public static final String TAG = "TAGUserMapAct";
	public static final String KEY_DEVICE_INFO = "DevObj";

	Context mContext;
	BMapManager bManager;
	ErrorCodeReceiver errorCodeReceiver;
	
	
	private String guid;
	private boolean isToOtherMap = false;
	private boolean busListEntrance = false;// 设备列表入口
	private double longitude, latitude;
	private boolean isBroadcastRegister = false;
	protected boolean IsExit = false;
	private int baseDirect;
	private int num = 2, i = 1;
	float zoom = 11.0f; // 地图放大等级默认 11级
	
	private BusDeviceInfo curBusDeviceInfo;// 当前可操作的设备
	private LinearLayout myLayout;
	private List<BusDeviceInfo> listBus;
	private Spinner queryDevList;
	private GeoPoint pt1 = null, pt2 = null, ptLast = null;

	private MapView mapView;
	private BaiduMap mBaiduMap;
	private Marker mMarkerA;
	MapStatus.Builder builder;
	LatLng center = new LatLng(39.915071, 116.403907); // 地图定位中心点, 默认 天安门

	// 覆盖图标
	BitmapDescriptor bdA = BitmapDescriptorFactory.fromResource(R.drawable.bus_image_map);
	Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_mapview);
		mContext=this;
		intent = getIntent();
		registReciver(this);
		initDeviceInfo();

		initView();
		initMap();
		initOverlay();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
		unregisterReceiver(mBroadcastReceiver);
		unregisterReceiver(errorCodeReceiver);
		IsExit = true;
		guid = "";
	}

	private void initView() {
		mapView = (MapView) findViewById(R.id.bmapView);
	}

	private void initDeviceInfo() {
		curBusDeviceInfo = (BusDeviceInfo) intent.getSerializableExtra(KEY_DEVICE_INFO);
		if (curBusDeviceInfo != null) {// 设备列表入口
			Log.i(TAG, "deviceinfo: " + curBusDeviceInfo.toString());
			busListEntrance = true;
			myLayout = (LinearLayout) findViewById(R.id.devSelect);// 设备下拉列表布局隐藏
			myLayout.setVisibility(View.GONE);
		} else {// 主菜单电子地图入口
			busListEntrance = false;
			getBusDevices();// 获取数据
			if (listBus.size() == 0) {
				myLayout = (LinearLayout) findViewById(R.id.devSelect);// 设备下拉列表布局隐藏
				myLayout.setVisibility(View.GONE);
				// getCurrentLocation();// 定位手机的位置
			} else {
				curBusDeviceInfo = listBus.get(0);
				Log.i(TAG, "deviceinfo: " + curBusDeviceInfo.toString());

				queryDevList = (Spinner) findViewById(R.id.spinner_queryDevList);
				SpinnerBusAdapter queryDevListAdapter = new SpinnerBusAdapter(this, R.layout.spinner_item, listBus);
				queryDevListAdapter.setDropDownViewResource(R.layout.spinner_checkview);
				queryDevList.setAdapter(queryDevListAdapter);
				queryDevList.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1, int location, long arg3) {
						if (guid != null) {
							guid = "";
						}
						curBusDeviceInfo = listBus.get(location);
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
			}
		}
	}

	private void initMap() {
		mBaiduMap = mapView.getMap();
		center = new LatLng(curBusDeviceInfo.getLatitude(), curBusDeviceInfo.getLongitude());
		// 定义地图状态
		MapStatus mMapStatus = new MapStatus.Builder().target(center).zoom(zoom).build();
		// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
		// 改变地图状态
		mBaiduMap.setMapStatus(mMapStatusUpdate);

	}

	/**
	 * 注册广播，sdk错误广播，定位广播
	 * 
	 * @param context
	 */
	private void registReciver(Context context) {
		errorCodeReceiver = new ErrorCodeReceiver(context);
		// 注册 SDK 广播监听者
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
		iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
		iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
		registerReceiver(errorCodeReceiver, iFilter);

		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction("ACTION_NAME");
		registerReceiver(mBroadcastReceiver, myIntentFilter);
	}


	// 广播接收对象
	// gps位置广播
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			String action = intent.getAction();
			if (action.equals("ACTION_NAME")) {
				int eventType = intent.getIntExtra("eventType", 0);//
				if (isBroadcastRegister && eventType == CALLBACKFLAG.DEVICE_EVENT_GPS_INFO) {// GPS基本
					String gpsDevID = intent.getStringExtra("gpsDevID");
					if (curBusDeviceInfo != null && curBusDeviceInfo.getGuId().equals(gpsDevID)) {
						// Log.e(TAG, "====有木有接收到广播");
						// 经纬度
						longitude = intent.getDoubleExtra("gpsBaseLongitude", 0);
						latitude = intent.getDoubleExtra("gpsBaseLatitude", 0);
						baseDirect = intent.getIntExtra("gpsBaseDirect", 0);
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {

									if (IsExit)
										return;

									Log.i("ii", "获取的gps:经度:" + longitude + " 纬度:" + latitude);
									String guid_tmp = guid;
									Map<String, String> maps = new HashMap<String, String>();
									maps = GpsToBaiduUtil.ConvertGpsToBaidu(guid_tmp, latitude, longitude);
									if (maps != null && maps.size() > 0) {
										guid_tmp = maps.get("guid");
										if (!guid_tmp.equals(guid))
											return;

										if (IsExit)
											return;

										Log.i("ii", "百度api解析数据:" + maps.get("x") + "  " + maps.get("y"));
										BASE64Decoder decoder = new BASE64Decoder();
										String x = null, y = null;
										x = maps.get("x");
										y = maps.get("y");
										byte[] byte_x = decoder.decodeBuffer(x);
										byte[] byte_y = decoder.decodeBuffer(y);
										if (Double.parseDouble(new String(byte_x)) == 0.0
												|| Double.parseDouble(new String(byte_y)) == 0.0) {
											onReceive(context, intent);
										}
										Log.i("ii", "run()经度:" + Double.parseDouble(new String(byte_x)) + " 纬度:"
												+ Double.parseDouble(new String(byte_y)));
										Message msg = new Message();
										Bundle bundle = new Bundle();
										bundle.putDouble("lat", Double.parseDouble(new String(byte_y)));
										bundle.putDouble("lon", Double.parseDouble(new String(byte_x)));
										bundle.putInt("num", num++);
										msg.setData(bundle);
//										handler2.sendMessage(msg);
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}).start();
					}
				}
			}
		}

	};

	// 获取所有的bus设备
	private void getBusDevices() {
		listBus = new ArrayList<BusDeviceInfo>();
		for (BusDeviceInfo busInfo : Constants.BUSDEVICEDATA) {
			if ("0".equals(busInfo.getIsDeviceGroup())) {
				listBus.add(busInfo);
			}
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, R.string.video_stream);
		menu.add(0, 1, 1, R.string.cancel);
		menu.add(0, 2, 2, R.string.googleMap);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			Intent intent = new Intent();
			curBusDeviceInfo.setCurrentChn(1);
			intent.putExtra("videoData", curBusDeviceInfo);
			intent.setClass(this, VideoActivity.class);
			startActivity(intent);
			break;
		case 1:

			break;
		case 2:
			// 谷歌地图
			if (MyUtil.checkGoogleMapModule(this)) {
				isToOtherMap = true;
				IsExit = true;
				guid = "";
				// 刷新地图
				// mapView.getOverlays().clear();
				Intent intent2 = new Intent();
				if (busListEntrance) {
					intent2.putExtra(UserMapActivity.KEY_DEVICE_INFO, curBusDeviceInfo);
				}
				intent2.setClass(this, UserGoogleMapActivity.class);
				startActivity(intent2);
				finish();
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 初始化图层
	 */
	private void initOverlay() {
		// 坐标
		LatLng llA = new LatLng(curBusDeviceInfo.getLatitude(), curBusDeviceInfo.getLongitude());
		// LatLng llA = new LatLng((double) (latitude * 1E6), (double)
		// (longitude * 1E6));
		MarkerOptions ooA = new MarkerOptions().position(llA).icon(bdA).zIndex(9).draggable(true);
		// 掉下动画
		ooA.animateType(MarkerAnimateType.drop);
		mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
		// 刷新地图
		// mapView.refresh();
		 mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker arg0) {
				if(arg0==mMarkerA){
					Intent intent = new Intent(mContext,VideoActivity.class);
					curBusDeviceInfo.setCurrentChn(1);
					intent.putExtra("videoData", curBusDeviceInfo);
					startActivity(intent);
				}
				return false;
			}
		});
	}

}