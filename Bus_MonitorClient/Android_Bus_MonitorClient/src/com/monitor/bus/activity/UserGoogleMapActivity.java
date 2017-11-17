package com.monitor.bus.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jniUtil.GpsCorrection;
import com.jniUtil.JNVPlayerUtil;
import com.jniUtil.MyUtil;
import com.jniUtil.SaveUtil;
import com.monitor.bus.adapter.SpinnerBusAdapter;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.consts.Constants.CALLBACKFLAG;
import com.monitor.bus.model.BusDeviceInfo;


public class UserGoogleMapActivity extends FragmentActivity{ 

	private static final String TAG = "MainActivity";

	private GoogleMap mMap;
	private Marker mMarker;
	private LatLng prePoint;
	private LatLng curPoint;
	private Polyline mPolyline;
	private LinkedList<LatLng> mLatLngs = new LinkedList<LatLng>();
	private boolean isAnimationEnd = false;

	private LocationManager locationManager;
	private Location location;
	private MyLocationSource mLocationSource;

	static final String Apptag = "Google";
	final String g_GpsFixFileName = "commondata.gft";

	private List<BusDeviceInfo> listBus;
	private BusDeviceInfo curCtlDevInfo = null; // 当前可操作的设备
	private String guid = null;

	private LinearLayout myLayout;
	private Spinner queryDevList;
	
	protected boolean IsAsyncLoadGpsCorrectionFile = false;//异步加载gps校验数据

	private boolean busListEntrance = false;
	private boolean isToOtherMap = false;
	private boolean isBroadcastRegister = false;
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 1:
				Log.i(TAG, "请求GPS信息参数："+guid); 
				JNVPlayerUtil.JNV_N_GetGPSStart(guid);//请求下发GPS数据
				break;
			case 2:
				//刷新位置
				setUpMapIfNeeded();
				mMap.setOnMapLongClickListener(new OnMapLongClickListener() {
					public void onMapLongClick(LatLng arg0) {
						openOptionsMenu();
					}
				});
				
				registerBoradcastReceiver();//注册广播接收器
				mLocationSource.onResume();
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
		curCtlDevInfo = (BusDeviceInfo) intent.getSerializableExtra(UserMapActivity.KEY_DEVICE_INFO);// 获取当前设备坐标

		mLocationSource = new MyLocationSource();

		//		SD卡中		
		//		File targetFile = null;
		//		try {
		//			targetFile = new File(Environment.getExternalStorageDirectory()
		//					.getCanonicalPath() +"/"+ g_GpsFixFileName);
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		} 
		
		String f = SaveUtil.saveIfNeed(this, g_GpsFixFileName, R.raw.commondata);
		File targetFile = new File(f);
		if(targetFile.exists())
		{
			SharedPreferences spf = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
			boolean IsGpsCorrection = spf.getBoolean(Constants.GOOGLE_GPS_CORRRECTION, Constants.IS_GOOGLE_GPS_CORRRECTION);
			if(IsGpsCorrection){
				if(!GpsCorrection.getInstance().IsInitialize()){
					//GpsCorrection.getInstance().initialize(targetFile.getPath());
					IsAsyncLoadGpsCorrectionFile = true;
					new AsyncLoadGpsCorrection(this,handler,targetFile.getPath()).execute();
				}
			}
		}

		if(!IsAsyncLoadGpsCorrectionFile){
			setUpMapIfNeeded();
			mMap.setOnMapLongClickListener(new OnMapLongClickListener() {
				public void onMapLongClick(LatLng arg0) {
					openOptionsMenu();
				}
			});
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(!IsAsyncLoadGpsCorrectionFile){
			setUpMapIfNeeded();
			registerBoradcastReceiver();//注册广播接收器
			mLocationSource.onResume();
		}
	}

	@Override
	protected void onPause(){
		super.onPause();
		mLocationSource.onPause();
	}

	@Override
	protected void onStop() {
		unregisterReceiver(mBroadcastReceiver);
		isBroadcastRegister = false;
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if(!isToOtherMap){
			JNVPlayerUtil.JNV_N_GetGPSStop(guid);
			guid = "";
			}
		super.onDestroy();
		//CommonJni.JNV_GPSFix_UnInit();
	}

	/**
	 * 如果需要的话，设置地图
	 */
	public void setUpMapIfNeeded() {
		if (mMap == null) {
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	/**
	 * 设置地图
	 */
	private void setUpMap() {

		mMap.getUiSettings().setRotateGesturesEnabled(false);//禁用旋转手势
		mMap.setMyLocationEnabled(true);//开启本机位置图层
		mMap.getUiSettings().setMyLocationButtonEnabled(false);
		mMap.setLocationSource(mLocationSource);
		isAnimationEnd = false;
		
		setMarkerListener();

		if(curCtlDevInfo != null){//设备列表入口
			busListEntrance = true;
			myLayout = (LinearLayout) findViewById(R.id.devSelect);//设备下拉列表布局隐藏
			myLayout.setVisibility(View.GONE);
			initDevLocationGPS();

		}else{//主菜单电子地图入口
			busListEntrance = false;
			getBusDevices();//获取数据
			if(0 == listBus.size()){
				myLayout = (LinearLayout) findViewById(R.id.devSelect);//设备下拉列表布局隐藏
				myLayout.setVisibility(View.GONE);
				//initDevLocationGPS();
				getCurrentLocation();//当前位置
			}else{
				queryDevList = (Spinner)findViewById(R.id.queryDevList);
				SpinnerBusAdapter queryDevListAdapter = new SpinnerBusAdapter(this, R.layout.spinner_item, listBus);
				queryDevListAdapter.setDropDownViewResource(R.layout.spinner_checkview);
				queryDevList.setAdapter(queryDevListAdapter);
				queryDevList.setOnItemSelectedListener(new DevListItemSelectListener());
				//getCurrentLocation();
				//				queryDevList.setSelection(0);
			}
		}
	}

	private void setMarkerListener() {
		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(Marker marker) {
				if(marker.equals(mMarker)){
					Toast.makeText(UserGoogleMapActivity.this,
							"设备名称："+curCtlDevInfo.getDeviceName(), Toast.LENGTH_SHORT).show();
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * 移动摄像头
	 * @param zoom   不进行缩放时，指定为0
	 */
	private void moveCamera(LatLng latLng,int zoom) {
		//		Log.i(TAG, "移动摄像头："+latLng);
		if(zoom==0){
			if(isAnimationEnd) mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng)); 
		}else{
			mMap.animateCamera( CameraUpdateFactory.newLatLngZoom(latLng, zoom), callback);  
			isAnimationEnd = false;
		}
	}

	/**
	 * 摄像头相关回调
	 */
	private CancelableCallback callback = new CancelableCallback() {

		@Override
		public void onFinish() {
			Log.i(TAG, "动画结束了！！");
			isAnimationEnd = true;
		}

		@Override
		public void onCancel() {
			Log.i(TAG, "动画被取消了！！");
		}
	};

	/**
	 * 获取所有的bus设备
	 * @return
	 */
	public void getBusDevices(){
		listBus = new ArrayList<BusDeviceInfo>();
		for (BusDeviceInfo busInfo : Constants.BUSDEVICEDATA) {
			if ("0".equals( busInfo.getIsDeviceGroup())) {
				listBus.add(busInfo);
			}
		}
	}

	/**
	 * 初始化对应设备的GPS信息
	 * @param curCtlDevInfo
	 */
	public void initDevLocationGPS(){
		Log.i(TAG, "运行到initDevLocationGPS");
		if( validateGps()){
			//			double longitude = MyUtil.convertDoubleType6(curCtlDevInfo.getLongitude());
			//			double latitude = MyUtil.convertDoubleType6(curCtlDevInfo.getLatitude());
			double longitude = curCtlDevInfo.getLongitude();
			double latitude = curCtlDevInfo.getLatitude();
			Log.i(TAG, "表中获得：lon="+longitude+",lat="+latitude);
			//			GeoPoint myPoint =CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(new GeoPoint((int)(latitude*1e6), (int)(longitude*1e6))));//GPS转换为百度地图坐标 
			//			LatLng myPoint = new LatLng(latitude, longitude);
			LatLng myPoint =MyUtil.fromWgs84ToGoogle(latitude, longitude);

			mMap.clear();
			mLatLngs.clear();

			mMarker = mMap.addMarker(new MarkerOptions()
			.anchor(0.5f, 0.5f)
			.position(myPoint)
			.title("设备名称："+curCtlDevInfo.getDeviceName())
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_image_map)));

			mPolyline = mMap.addPolyline(new PolylineOptions()
			.color(Color.GREEN)
			.width(4));

			moveCamera(myPoint, 16);
		}else{
			getCurrentLocation();
		}
		//		guid = curCtlDevInfo.getGuId();
		guid = curCtlDevInfo.getNewGuId();  
		//		Log.i(TAG, "请求GPS信息参数："+guid); 
		//		JNVPlayerUtil.JNV_N_GetGPSStart(guid);//请求下发GPS数据
//		handler.sendEmptyMessageDelayed(1, 1000);//延迟1秒执行获取GPS，防止前面界面的停止获取延迟
		handler.sendEmptyMessage(1);
	}



	/**
	 * 获取旋转后的位图
	 */
	protected Bitmap getRotatedBmp(Bitmap bmp, int angle) {
		Matrix myMatrix = new Matrix();
		myMatrix.reset();
		myMatrix.postRotate(angle);
		Bitmap dstBitmap = Bitmap.createBitmap(bmp, 0, 0, 
				bmp.getWidth(),bmp.getHeight(), myMatrix, true);
		return dstBitmap;
	} 


	/**
	 * 验证是否具有有效的GPS数据
	 * @param curCtlDevInfo
	 * @return
	 */
	public boolean validateGps(){
		if(curCtlDevInfo!=null && 0d != curCtlDevInfo.getLatitude() && 0d != curCtlDevInfo.getLongitude()){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			Intent intent = new Intent();
			curCtlDevInfo.setCurrentChn(1);
			intent.putExtra("videoData", curCtlDevInfo);
			intent.setClass(this, VideoActivity.class);
			startActivity(intent);
			break;
		case 1:

			break;
		case 2:
			isToOtherMap = true;
			JNVPlayerUtil.JNV_N_GetGPSStop(guid);
			guid = "";
			Intent intent2 = new Intent();
			if(busListEntrance){
				intent2.putExtra(UserMapActivity.KEY_DEVICE_INFO, curCtlDevInfo);
			}
			intent2.setClass(this, UserMapActivity.class);
			startActivity(intent2);
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, R.string.video_stream);
		menu.add(0, 1, 1, R.string.cancel);
		int versionLevel = android.os.Build.VERSION.SDK_INT;
		menu.add(0, 2, 2, R.string.baiduMap);
		return super.onCreateOptionsMenu(menu);
	}



	/**
	 * 设备列表选择监听器
	 */
	public class DevListItemSelectListener implements OnItemSelectedListener{
		@Override 
		public void onItemSelected(AdapterView<?> arg0, View arg1,
				int location, long arg3) {
			Log.i(TAG, "选择了"+location);
			if(guid != null){
				JNVPlayerUtil.JNV_N_GetGPSStop(guid);
				guid = "";
			}
			changeFocusBus(location);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			//getCurrentLocation();
		}

	} 

	/**
	 * 注册广播接收器
	 */
	public void registerBoradcastReceiver(){ 
		IntentFilter myIntentFilter = new IntentFilter(); 
		myIntentFilter.addAction("ACTION_NAME"); 
		//注册广播       
		registerReceiver(mBroadcastReceiver, myIntentFilter); 
		isBroadcastRegister = true;
	} 

	/**
	 * 修改焦点车辆
	 * @param location
	 */
	public void changeFocusBus(int location) {
		curCtlDevInfo = listBus.get(location);
		initDevLocationGPS();
	}

	/**
	 * 获取我的位置
	 */
	private void getCurrentLocation() {

		Criteria criteria = new Criteria();
		// ACCURACY_FINE 较高精确度
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		String provider = locationManager.getBestProvider(criteria, true);

		Log.i(TAG,"位置提供者"+provider);

		location = locationManager
				.getLastKnownLocation(provider);//locationManager.GPS_PROVIDER
		if(location!=null) updateLocation(location);


		locationManager.requestLocationUpdates(provider, 3000, (float) 10.0,
				new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				updateLocation(location);
			}

			@Override
			public void onProviderDisabled(String provider) {
				Log.i("onProviderDisabled", "come in");
			}

			@Override
			public void onProviderEnabled(String provider) {
				Log.i("onProviderEnabled", "come in");
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}
		});
	}

	/**
	 * 更新位置
	 * @param location
	 */
	private void updateLocation(Location location) {
		Log.i(TAG, "更新位置："+location);

		LatLng tmp = MyUtil.fromWgs84ToGoogle(
				location.getLatitude(), location.getLongitude());
		location.setLatitude(tmp.latitude);
		location.setLongitude(tmp.longitude);

		mLocationSource.updataMyLocation(location);
		moveCamera(new LatLng(location.getLatitude(), 
				location.getLongitude()), 16);
	}


	/**
	 * 我的位置源
	 */
	private static class MyLocationSource implements LocationSource{
		private OnLocationChangedListener mListener;

		private boolean mPaused;

		@Override
		public void activate(OnLocationChangedListener listener) {
			mListener = listener;
		}

		@Override
		public void deactivate() {
			mListener = null;
		}

		public void updataMyLocation(Location point){
			if (mListener != null && !mPaused) {
				Location location = new Location("LongPressLocationProvider");
				location.setLatitude(point.getLatitude());
				location.setLongitude(point.getLongitude());
				location.setAccuracy(point.getAccuracy());
				mListener.onLocationChanged(location);
			}
		}

		public void onPause() {
			mPaused = true;
		}

		public void onResume() {
			mPaused = false;
		}

	}

	/**
	 * 广播接收器
	 */
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){ 
		@Override 
		public void onReceive(Context context, Intent intent) { 
			String action = intent.getAction(); 
			if(action.equals("ACTION_NAME")){ 
				int eventType = intent.getIntExtra("eventType", 0);// 
				if(isBroadcastRegister && eventType == CALLBACKFLAG.DEVICE_EVENT_GPS_INFO){//GPS基本

//					if(!validateGps()){
//						//禁用自动定位
//												app.mBMapMan.getLocationManager().removeUpdates(mLocationListener);
//												mLocationOverlay.disableMyLocation();
//					}
					String gpsDevID = intent.getStringExtra("gpsDevID");
//					Log.i(TAG, "当前设备："+curCtlDevInfo);
					if(curCtlDevInfo!=null && curCtlDevInfo.getGuId().equals(gpsDevID)){
						//						Log.e(TAG, "====有木有接收到广播");
						double longitude = intent.getDoubleExtra("gpsBaseLongitude", 0);
						double latitude = intent.getDoubleExtra("gpsBaseLatitude", 0);
						int baseDirect = intent.getIntExtra("gpsBaseDirect", 0);
						//						Log.e(TAG, "======gpsBaseDirect的度数"+baseDirect );
						//						curPoint = new LatLng(latitude, longitude);
						curPoint = MyUtil.fromWgs84ToGoogle(latitude, longitude);
						if(!curPoint.equals(prePoint)){
							Bitmap bmp = BitmapFactory.decodeResource(getResources(),
									R.drawable.bus_image_map);
							Bitmap dstBitmap = getRotatedBmp(bmp, baseDirect);
							mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(dstBitmap));

							mLatLngs.add(curPoint);
							mPolyline.setPoints(mLatLngs);

							if(isAnimationEnd){
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

	//触摸打开菜单按钮
	 @Override
	public boolean onTouchEvent(MotionEvent event) {
		 this.openOptionsMenu();
		return super.onTouchEvent(event);
	}
	
	 //异步加载gps校正文件
	 class AsyncLoadGpsCorrection extends AsyncTask<Void,Integer,Integer>{
		protected Context context = null;
		protected String  gpsCorrectionFileName = "";
		protected ProgressDialog  dialog = null;
		protected Handler mHandler = null;
		
		public AsyncLoadGpsCorrection(Context context,Handler h,String strFileName){
			this.context = context;
			mHandler = h;
			gpsCorrectionFileName = strFileName;
			dialog = new ProgressDialog(this.context);
			dialog.setMessage(context.getResources().getString(R.string.googleGpsCorrectionFileLoad));
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return GpsCorrection.getInstance().initialize(gpsCorrectionFileName);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			
			if(dialog != null){
				dialog.show();
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			if(dialog != null){
				dialog.dismiss();
			}
			
			if(mHandler != null){
				Message msg = new Message();
				msg.what = 2;
				mHandler.sendMessage(msg);
			}
			
			IsAsyncLoadGpsCorrectionFile = false;
		}
		 
	 };
}
