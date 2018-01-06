package com.monitor.bus.bean.manager;

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
import com.monitor.bus.activity.R;
import com.monitor.bus.bdmap.ErrorCodeReceiver;
import com.monitor.bus.bean.DeviceInfo;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.utils.MUtils;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.view.View;

public class BaiduMapManager extends BaseMapManager {
	private Context mContext;

	private MapView mapView;
	private BaiduMap mBaiduMap;
	private Marker mMarkerA; // 覆盖图标
	BitmapDescriptor bdA = BitmapDescriptorFactory.fromResource(R.drawable.bus_image_map);

	float zoom = 11.0f; // 地图放大等级默认 11级
	LatLng center = new LatLng(39.915071, 116.403907); // 地图定位中心点, 默认 天安门

	BMapManager bManager;
	ErrorCodeReceiver errorCodeReceiver;
	MapStatus.Builder builder;
	DeviceInfo deviceInfo;

	public BaiduMapManager(Context context) {
		mContext = context;
	}

	@Override
	public void setDeviceInfo(DeviceInfo info) {
		deviceInfo = info;
		if (deviceInfo == null) {
			MUtils.toast(mContext, "请先选择设备");
		}
		registReciver();
	}

	@Override
	public void onCreat() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResum() {
		initView();
		initOverlay();
		mapView.onResume();
	}

	@Override
	public void onPause() {
		mapView.onPause();
	}

	@Override
	public void onDestory() {
		mapView.onDestroy();
		unRegistReciver();
	}

	/**
	 * 清除所有Overlay
	 *
	 * @param contentView
	 */
	private void clearOverlay() {
		mBaiduMap.clear();
		mMarkerA = null;
	}

	private void resetOverLay() {
		clearOverlay();
		initOverlay();
	}

	private void initView() {
		mapView = (MapView) ((Activity) mContext).findViewById(R.id.bmapView);
		mapView.setVisibility(View.VISIBLE);
		((Activity) mContext).findViewById(R.id.rl_googlemap).setVisibility(View.GONE);;
		mBaiduMap = mapView.getMap();
	}

	/**
	 * 初始化图层
	 */
	private void initOverlay() {
		if (null == deviceInfo) {
			MUtils.toast(mContext, "未监测到设备");
		}
		center = new LatLng(deviceInfo.getLatitude(), deviceInfo.getLongitude());
		// 定义地图状态
		MapStatus mMapStatus = new MapStatus.Builder().target(center).zoom(zoom).build();
		// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
		// 改变地图状态
		mBaiduMap.setMapStatus(mMapStatusUpdate);

		// 坐标
		LatLng llA = new LatLng(deviceInfo.getLatitude(), deviceInfo.getLongitude());
		MarkerOptions ooA = new MarkerOptions().position(llA).icon(bdA).zIndex(9).draggable(true);
		// 掉下动画
		ooA.animateType(MarkerAnimateType.drop);
		mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
		// 刷新地图
		// mapView.refresh();
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker arg0) {
				if (arg0 == mMarkerA) {
				}
				return false;
			}
		});

	}

	/**
	 * 注册广播，sdk错误广播，定位广播
	 * 
	 * @param context
	 */
	private void registReciver() {
		errorCodeReceiver = new ErrorCodeReceiver(mContext);
		// 注册 SDK 广播监听者
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
		iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
		iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
		mContext.registerReceiver(errorCodeReceiver, iFilter);

		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(Constants.ACTION_LOGIN_EVENT);
		// registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	private void unRegistReciver() {
		mContext.unregisterReceiver(errorCodeReceiver);
	}
}
