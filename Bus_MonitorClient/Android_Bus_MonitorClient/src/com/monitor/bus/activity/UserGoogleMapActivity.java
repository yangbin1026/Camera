package com.monitor.bus.activity;

import java.io.File;
import java.util.LinkedList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jniUtil.GpsCorrection;
import com.jniUtil.JNVPlayerUtil;
import com.monitor.bus.utils.MUtils;
import com.monitor.bus.utils.SPUtils;
import com.monitor.bus.bdmap.GoogleCheckGPSAsyncTask;
import com.monitor.bus.bean.DeviceInfo;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.consts.Constants.CALLBACKFLAG;
import com.monitor.bus.utils.LogUtils;

public class UserGoogleMapActivity extends FragmentActivity {

	private static final String TAG = "UserGoogleMapActivity";
	static final String Apptag = "Google";
	final String g_GpsFixFileName = "commondata.gft";
	public static final int MSG_WHAT_GET_GPS_START = 1;
	public static final int MSG_WHAT_NEW_LOCATION = 2;
	public static final int ZOOM=16;

	private boolean IsAsynCheckGPS = true;// 异步加载gps校验数据
	private boolean isBroadcastRegister = false;
	private boolean isAnimationEnd = false;
	public boolean isResume=false;

	private GoogleMap mapView;
	private Marker mMarker;
	private LatLng prePoint;
	private LatLng curPoint;
	private Polyline mPolyline;

	private LinkedList<LatLng> mLatLngs = new LinkedList<LatLng>();
	private Location location;
	private LocationSource mLocationSource;

	private DeviceInfo curCtlDevInfo = null; // 当前可操作的设备
	private String deviceId = null;
	
	public OnLocationChangedListener mListener;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_WHAT_GET_GPS_START:
				//开始获取gps信息
				LogUtils.i(TAG, "请求GPS信息参数：" + deviceId);
				JNVPlayerUtil.JNV_N_GetGPSStart(deviceId);// 请求下发GPS数据
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_google_mapview);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 屏幕保持常亮

		Intent intent = getIntent();
		curCtlDevInfo = (DeviceInfo) intent.getSerializableExtra(RealTimeVideoActivity.KEY_DEVICE_INFO);// 获取当前设备坐标
		initView();
		
		mLocationSource = new LocationSource(){

			@Override
			public void activate(OnLocationChangedListener arg0) {
				mListener = arg0;
			}

			@Override
			public void deactivate() {
				mListener = null;
				
			}
			
		};


		String f = MUtils.saveIfNeed(this, g_GpsFixFileName, R.raw.commondata);
		File targetFile = new File(f);
		
		if (targetFile.exists()) {
			SharedPreferences spf = getSharedPreferences(SPUtils.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
			boolean IsGpsCorrection = spf.getBoolean(Constants.GOOGLE_GPS_CORRRECTION,
					Constants.IS_GOOGLE_GPS_CORRRECTION);
			if (IsGpsCorrection) {
				if (!GpsCorrection.getInstance().IsInitialize()) {
					IsAsynCheckGPS = true;
					new GoogleCheckGPSAsyncTask(this, handler, targetFile.getPath()).execute();
				}
			}
		}

		if (!IsAsynCheckGPS) {
			mapView.setOnMapLongClickListener(new OnMapLongClickListener() {
				public void onMapLongClick(LatLng arg0) {
					openOptionsMenu();
				}
			});
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!IsAsynCheckGPS) {
			registerBoradcastReceiver();// 注册广播接收器
			isResume=true;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		isResume=false;
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mBroadcastReceiver);
		isBroadcastRegister = false;
		JNVPlayerUtil.JNV_N_GetGPSStop(deviceId);
		super.onDestroy();
	}
	
	private void initView(){
		isAnimationEnd = false;
			mapView = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
			mapView.getUiSettings().setRotateGesturesEnabled(false);// 禁用旋转手势
			mapView.setMyLocationEnabled(true);// 开启本机位置图层
			mapView.getUiSettings().setMyLocationButtonEnabled(false);
			mapView.setLocationSource(mLocationSource);
			mapView.setOnMarkerClickListener(new OnMarkerClickListener() {

				@Override
				public boolean onMarkerClick(Marker marker) {
					if (marker.equals(mMarker)) {
						Toast.makeText(UserGoogleMapActivity.this, "设备名称：" + curCtlDevInfo.getDeviceName(),
								Toast.LENGTH_SHORT).show();
						return true;
					}
					return false;
				}
			});

			if (curCtlDevInfo != null) {// 设备列表入口
				initDevLocationGPS();

			} 
	}
	private void initData(){
		
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
			mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom), callback);
			isAnimationEnd = false;
		}
	}

	/**
	 * 摄像头相关回调
	 */
	private CancelableCallback callback = new CancelableCallback() {

		@Override
		public void onFinish() {
			LogUtils.i(TAG, "动画结束了！！");
			isAnimationEnd = true;
		}

		@Override
		public void onCancel() {
			LogUtils.i(TAG, "动画被取消了！！");
		}
	};

	/**
	 * 初始化对应设备的GPS信息
	 */
	public void initDevLocationGPS() {
		LogUtils.i(TAG, "运行到initDevLocationGPS");
		if (validateGps()) {
			double longitude = curCtlDevInfo.getLongitude();
			double latitude = curCtlDevInfo.getLatitude();
			LogUtils.i(TAG, "表中获得：lon=" + longitude + ",lat=" + latitude);
			LatLng myPoint = MUtils.fromWgs84ToGoogle(latitude, longitude);

			mapView.clear();
			mLatLngs.clear();

			mMarker = mapView.addMarker(new MarkerOptions().anchor(0.5f, 0.5f).position(myPoint)
					.title("设备名称：" + curCtlDevInfo.getDeviceName())
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_image_map)));

			mPolyline = mapView.addPolyline(new PolylineOptions().color(Color.GREEN).width(4));

			moveCamera(myPoint, ZOOM);
		} else {
			getCurrentLocation();
		}
		deviceId = curCtlDevInfo.getNewGuId();
		handler.sendEmptyMessage(MSG_WHAT_GET_GPS_START);
	}

	/**
	 * 验证是否具有有效的GPS数据
	 * 
	 * @return
	 */
	private boolean validateGps() {
		if (curCtlDevInfo != null && 0d != curCtlDevInfo.getLatitude() && 0d != curCtlDevInfo.getLongitude()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取我的位置
	 */
	private void getCurrentLocation() {
		LocationManager locationManager= (LocationManager) getSystemService(LOCATION_SERVICE);
		
		Criteria criteria = new Criteria();
		// ACCURACY_FINE 较高精确度
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);


		String provider = locationManager.getBestProvider(criteria, true);

		LogUtils.i(TAG, "位置提供者" + provider);

		location = locationManager.getLastKnownLocation(provider);// locationManager.GPS_PROVIDER
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
	 * 更新位置
	 * 
	 * @param location
	 */
	private void updateLocation(Location location) {
		LogUtils.i(TAG, "更新位置：" + location);
		LatLng tmp = MUtils.fromWgs84ToGoogle(location.getLatitude(), location.getLongitude());
		location.setLatitude(tmp.latitude);
		location.setLongitude(tmp.longitude);
		if (mListener != null && isResume) {
			Location newLocation = new Location("LongPressLocationProvider");
			location.setLatitude(location.getLatitude());
			location.setLongitude(location.getLongitude());
			location.setAccuracy(location.getAccuracy());
			mListener.onLocationChanged(location);
		}
		moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM);
	}

	/**
	 * 注册广播接收器
	 */
	private void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction("ACTION_NAME");
		registerReceiver(mBroadcastReceiver, myIntentFilter);
		isBroadcastRegister = true;
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
				if (isBroadcastRegister && eventType == CALLBACKFLAG.DEVICE_EVENT_GPS_INFO) {// GPS基本

					String gpsDevID = intent.getStringExtra("gpsDevID");
					if (curCtlDevInfo != null && curCtlDevInfo.getGuId().equals(gpsDevID)) {
						double longitude = intent.getDoubleExtra("gpsBaseLongitude", 0);
						double latitude = intent.getDoubleExtra("gpsBaseLatitude", 0);
						int baseDirect = intent.getIntExtra("gpsBaseDirect", 0);
						curPoint =MUtils.fromWgs84ToGoogle(latitude, longitude);
						if (!curPoint.equals(prePoint)) {
							Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.bus_image_map);
							Bitmap dstBitmap = MUtils.getRotatedBmp(bmp, baseDirect);
							mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(dstBitmap));

							mLatLngs.add(curPoint);
							mPolyline.setPoints(mLatLngs);

							if (isAnimationEnd) {
								mMarker.setPosition(curPoint);
								moveCamera(curPoint, 0);
							}
							prePoint = curPoint;
						}
					}
				}
			}
		}

	};
}
