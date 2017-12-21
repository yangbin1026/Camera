package com.monitor.bus.bean;

import java.io.File;
import java.util.LinkedList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jniUtil.GpsCorrection;
import com.jniUtil.JNVPlayerUtil;
import com.monitor.bus.activity.R;
import com.monitor.bus.bdmap.GoogleCheckGPSAsyncTask;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.consts.Constants.CALLBACKFLAG;
import com.monitor.bus.utils.LogUtils;
import com.monitor.bus.utils.MUtils;
import com.monitor.bus.utils.SPUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class GoogleMapManager extends BaseMapManager {
	private static final String TAG = "GoogleMapManager";
	private Context mContext;
	public static final String GPS_FIX_FILE_NAME = "commondata.gft";
	public static final int ZOOM = 16;
	public static final int MSG_WHAT_GET_GPS_START = 1;
	public static final int MSG_WHAT_NEW_LOCATION = 2;

	private boolean IsAsynCheckGPS = false;// gps校正任务是否完成
	private boolean isAnimationEnd = false;

	private RelativeLayout rl_googlemap;
	private GoogleMap mapView;
	private Marker mMarker;
	private LatLng prePoint;
	private LatLng curPoint;
	private Polyline mPolyline;// 路线

	private LinkedList<LatLng> mLatLngs = new LinkedList<LatLng>();

	private DeviceInfo deviceInfo = null; // 当前可操作的设备

	public OnLocationChangedListener mLocationChangedListenerListener;

	boolean isGPSCheck;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_WHAT_GET_GPS_START:
				// 开始获取gps信息
				LogUtils.i(TAG, "请求GPS信息参数：" + deviceInfo.getNewGuId());
				JNVPlayerUtil.JNV_N_GetGPSStart(deviceInfo.getNewGuId());// 请求下发GPS数据
				break;
			case MSG_WHAT_NEW_LOCATION:
				// 刷新位置
				IsAsynCheckGPS = (Boolean) msg.obj;
				registerBoradcastReceiver();// 注册广播接收器
				break;
			default:
				break;
			}
		};
	};

	public GoogleMapManager(Context context) {
		mContext = context;
	}

	public void setDeviceInfo(DeviceInfo info) {
		deviceInfo = info;
		if (deviceInfo == null) {
			MUtils.toast(mContext, "请先选择设备");
		}
	}

	@Override
	public void onCreat() {
		isGPSCheck = SPUtils.getBoolean(mContext, SPUtils.KEY_GSP_CHECK, false);
		initView();
		initDevLocationGPS();
		initData();
		if (IsAsynCheckGPS) {
			registerBoradcastReceiver();// 注册广播接收器
		}

	}

	@Override
	public void onDestory() {
		mContext.unregisterReceiver(mBroadcastReceiver);
		JNVPlayerUtil.JNV_N_GetGPSStop(deviceInfo.getNewGuId());
	}

	private void initView() {
		isAnimationEnd = false;
		((Activity)mContext).findViewById(R.id.rl_googlemap).setVisibility(View.VISIBLE);
		mapView = ((SupportMapFragment) ((FragmentActivity) mContext).getSupportFragmentManager()
				.findFragmentById(R.id.googleMapView)).getMap();
		mapView.getUiSettings().setRotateGesturesEnabled(false);// 禁用旋转手势
		mapView.setMyLocationEnabled(true);// 开启本机位置图层
		mapView.getUiSettings().setMyLocationButtonEnabled(false);
		mapView.setLocationSource(new LocationSource() {

			@Override
			public void activate(OnLocationChangedListener arg0) {
				mLocationChangedListenerListener = arg0;
			}

			@Override
			public void deactivate() {
				mLocationChangedListenerListener = null;

			}

		});
		mapView.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				if (marker.equals(mMarker)) {
					Toast.makeText(mContext, "设备名称：" + deviceInfo.getDeviceName(), Toast.LENGTH_SHORT).show();
					return true;
				}
				return false;
			}
		});
	}

	private void initData() {
		String f = MUtils.saveIfNeed(mContext, GPS_FIX_FILE_NAME, R.raw.commondata);
		File targetFile = new File(f);
		if (targetFile.exists() && isGPSCheck && !GpsCorrection.getInstance().IsInitialize()) {
			new GoogleCheckGPSAsyncTask(mContext, handler, targetFile.getPath()).execute();
		}

	}

	/**
	 * 初始化对应设备的GPS信息
	 */
	private void initDevLocationGPS() {
		LogUtils.i(TAG, "运行到initDevLocationGPS");
		if (0d != deviceInfo.getLatitude() && 0d != deviceInfo.getLongitude()) {
			double longitude = deviceInfo.getLongitude();
			double latitude = deviceInfo.getLatitude();
			LogUtils.i(TAG, "表中获得：lon=" + longitude + ",lat=" + latitude);

			mapView.clear();
			mLatLngs.clear();

			LatLng myPoint = MUtils.fromWgs84ToGoogle(latitude, longitude);
			mMarker = mapView.addMarker(
					new MarkerOptions().anchor(0.5f, 0.5f).position(myPoint).title("设备名称：" + deviceInfo.getDeviceName())
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_image_map)));

			mPolyline = mapView.addPolyline(new PolylineOptions().color(Color.GREEN).width(4));

			moveCamera(myPoint, ZOOM);
		} else {
			getCurrentLocation();
		}
		handler.sendEmptyMessage(MSG_WHAT_GET_GPS_START);
	}

	/**
	 * 没有device位置信息时，获取当前位置显示
	 */
	private void getCurrentLocation() {
		LogUtils.getInstance().localLog(TAG, "getCurrentLocation()!!!!!!!!!");
		LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		// ACCURACY_FINE 较高精确度
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		String provider = locationManager.getBestProvider(criteria, true);
		LogUtils.i(TAG, "位置提供者" + provider);

		Location location = locationManager.getLastKnownLocation(provider);// locationManager.GPS_PROVIDER
		if (location != null)
			updateLocation(location);

		locationManager.requestLocationUpdates(provider, 3000, (float) 10.0, new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				updateLocation(location);
			}

			@Override
			public void onProviderDisabled(String provider) {
				LogUtils.i("onProviderDisabled", "come in");
			}

			@Override
			public void onProviderEnabled(String provider) {
				LogUtils.i("onProviderEnabled", "come in");
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}
		});
	}

	/**
	 * 移动摄像头 ,移动视窗到位置
	 * 
	 * @param zoom
	 *            不进行缩放时，指定为0
	 */
	private void moveCamera(LatLng latLng, int zoom) {
		if (zoom == 0) {
			if (isAnimationEnd)
				mapView.animateCamera(CameraUpdateFactory.newLatLng(latLng));
		} else {
			mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom), new CancelableCallback() {
				@Override
				public void onFinish() {
					LogUtils.i(TAG, "动画结束了！！");
					isAnimationEnd = true;
				}

				@Override
				public void onCancel() {
					LogUtils.i(TAG, "动画被取消了！！");
				}
			});
		}
	}

	/**
	 * 根据GPS数据更新位置
	 * 
	 * @param location
	 */
	private void updateLocation(Location location) {
		LogUtils.i(TAG, "更新位置：" + location);
		LatLng tmp = MUtils.fromWgs84ToGoogle(location.getLatitude(), location.getLongitude());
		location.setLatitude(tmp.latitude);
		location.setLongitude(tmp.longitude);
		if (mLocationChangedListenerListener != null) {
			Location newLocation = new Location("LongPressLocationProvider");
			location.setLatitude(location.getLatitude());
			location.setLongitude(location.getLongitude());
			location.setAccuracy(location.getAccuracy());
			mLocationChangedListenerListener.onLocationChanged(location);
		}
		moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM);
	}

	/**
	 * 注册广播接收器
	 */
	private void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction("ACTION_NAME");
		mContext.registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	/**
	 * 广播接收器
	 */
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("ACTION_NAME")) {
				int eventType = intent.getIntExtra(Constants.WHAT_LOGIN_EVENT_TYPE, 0);//
				if (eventType == CALLBACKFLAG.DEVICE_EVENT_GPS_INFO) {// GPS基本
					String gpsDevID = intent.getStringExtra("gpsDevID");
					if (deviceInfo != null && deviceInfo.getGuId().equals(gpsDevID)) {
						double longitude = intent.getDoubleExtra("gpsBaseLongitude", 0);
						double latitude = intent.getDoubleExtra("gpsBaseLatitude", 0);
						int baseDirect = intent.getIntExtra("gpsBaseDirect", 0);
						curPoint = MUtils.fromWgs84ToGoogle(latitude, longitude);
						if (!curPoint.equals(prePoint)) {
							Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(),
									R.drawable.bus_image_map);
							Bitmap dstBitmap = MUtils.getRotatedBmp(bmp, baseDirect);
							mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(dstBitmap));

							mLatLngs.add(curPoint);
							mPolyline.setPoints(mLatLngs);

							mMarker.setPosition(curPoint);
							moveCamera(curPoint, 0);
							prePoint = curPoint;
						}
					}
				}
			}
		}

	};

	@Override
	public void onResum() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub

	}

}
