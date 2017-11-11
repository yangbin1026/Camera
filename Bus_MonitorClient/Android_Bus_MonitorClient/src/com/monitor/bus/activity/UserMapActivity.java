package com.monitor.bus.activity;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import Decoder.BASE64Decoder;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.Geometry;
import com.baidu.mapapi.map.Graphic;
import com.baidu.mapapi.map.GraphicsOverlay;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.mapapi.map.Symbol;
import com.baidu.mapapi.utils.CoordinateConvert;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.baidu.platform.comapi.map.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.jniUtil.GpsToBaiduUtil;
import com.jniUtil.JNVPlayerUtil;
import com.jniUtil.MyUtil;
import com.monitor.bus.adapter.SpinnerBusAdapter;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.consts.Constants.CALLBACKFLAG;
import com.monitor.bus.model.BusDeviceInfo;
import com.monitor.bus.utils.LogUtils;

/**
 * 地图
 */
public class UserMapActivity extends Activity {
	public static final String TAG ="TAGUserMapAct";
	BMapManager bManager;
	private MyLocationMapView mapView;
	private MapController mapController;
	private BusDeviceInfo curBusDeviceInfo;// 当前可操作的设备
	private Projection projection;
	private List<Overlay> overlays;
	private String guid;
	private boolean isToOtherMap = false;
	private boolean busListEntrance = false;// 设备列表入口
	private LinearLayout myLayout;
	private double longitude, latitude;
	private GeoPoint myGeoPoint;
	private List<BusDeviceInfo> listBus;
	private Spinner queryDevList;
	private List<GeoPoint> beginGeoPointList;// 存储坐标点
	private GeoPoint endGeoPoint;
	private GeoPoint currentPoint;
	private MyOverlay mOverlay = null;
	private OverlayItem mCurItem = null;
	private GeoPoint beginGeoPoint;
	private boolean isBroadcastRegister = false;
	private OverlayItem item;
	private int baseDirect;
	private boolean isFirstGetGps = true;
	private int num = 2, i = 1;
	private GeoPoint pt1 = null, pt2 = null,ptLast = null;
	protected boolean IsExit = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bManager = new BMapManager(this);
		// NWbcmXVqc4pTsPpQZ9l81VeB
		// XyMmRTO8VVhFVd0laDfkCxWH
		// YO2IhEA6cBGz8pnIx0hgBHAo		nUKAWtCiINQEmL6gzpQiA6UG	[chen zheng yi]
		//yangbin ：9aFWU4B5xsHpnUDeGGnMbFcNVZaxu70Q

		//bManager.init("nUKAWtCiINQEmL6gzpQiA6UG", new MyGeneralListener());
		bManager.init("9aFWU4B5xsHpnUDeGGnMbFcNVZaxu70Q", new MyGeneralListener());
		bManager.start();
		setContentView(R.layout.user_mapview);
		initView();
		Intent intent = getIntent();
		curBusDeviceInfo = (BusDeviceInfo) intent
				.getSerializableExtra("DevObj");
		beginGeoPointList = new ArrayList<GeoPoint>();
		if (curBusDeviceInfo != null) {// 设备列表入口
			busListEntrance = true;
			myLayout = (LinearLayout) findViewById(R.id.devSelect);// 设备下拉列表布局隐藏
			myLayout.setVisibility(View.GONE);
			initDevLocationGPS();
		} else {// 主菜单电子地图入口
			busListEntrance = false;
			getBusDevices();// 获取数据
			if (listBus.size() == 0) {
				myLayout = (LinearLayout) findViewById(R.id.devSelect);// 设备下拉列表布局隐藏
				myLayout.setVisibility(View.GONE);
				getCurrentLocation();// 定位手机的位置
			} else {
				curBusDeviceInfo = listBus.get(0);
				queryDevList = (Spinner) findViewById(R.id.queryDevList);
				SpinnerBusAdapter queryDevListAdapter = new SpinnerBusAdapter(
						this, R.layout.spinner_item, listBus);
				queryDevListAdapter
						.setDropDownViewResource(R.layout.spinner_checkview);
				queryDevList.setAdapter(queryDevListAdapter);
				queryDevList
						.setOnItemSelectedListener(new DevListItemSelectListener());
			}
		}
		//initDevLocationGPS();
		// initUserMapInfo();
		// initOverlay();
	}

	// 设备列表选择监听事件
	public class DevListItemSelectListener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1,
				int location, long arg3) {
			if (guid != null) {
				JNVPlayerUtil.JNV_N_GetGPSStop(guid);
				guid = "";
			}
			curBusDeviceInfo = listBus.get(location);
			initDevLocationGPS();
			/*
			if(mapView != null){
				mapView.getOverlays().clear();	// add johnchen 40302240
				isFirstGetGps = true;
			}
			initGPS();
			*/
			
			/*
			 * isBroadcastRegister = true; registerBoradcastReceiver();
			 */
			 //initDevLocationGPS();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// getCurrentLocation();
		}
		
	}

	private Handler handler3 = new Handler() {
		public void handleMessage(Message msg) {
			String guid_Tmp = "";
			if(msg.obj != null){
				guid_Tmp = (String)msg.obj;
				if(!guid.equals(guid_Tmp)){
					return ;
				}
			}
			myGeoPoint = new GeoPoint((int) (latitude * 1E6),
					(int) (longitude * 1E6));
			mapController.setCenter(myGeoPoint);
			initOverlay();
			Log.i("handler2", "经度:" + longitude + " 纬度:" + latitude);
		};
	};

	// 初始化对应设备的GPS信息
	private void initGPS() {
		if (validateGps()) {
			longitude = curBusDeviceInfo.getLongitude();
			latitude = curBusDeviceInfo.getLatitude();
			Log.i("FFFFFFFFFFF", "经度:" + longitude + " 纬度:" + latitude);
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						String guid_tmp = guid;
						Map<String, String> maps = GpsToBaiduUtil
								.ConvertGpsToBaidu(guid_tmp,latitude, longitude);
						BASE64Decoder decoder = new BASE64Decoder();
						if(maps ==null || maps.size() < 3)
							return;
						
						guid_tmp = maps.get("guid");
						if(!guid_tmp.equals(guid))
							return;
						
						if(IsExit)
							return;
						
						String x = maps.get("x");
						String y = maps.get("y");
						byte[] byte_x = decoder.decodeBuffer(x);
						byte[] byte_y = decoder.decodeBuffer(y);
						longitude = Double.parseDouble(new String(byte_x));
						latitude = Double.parseDouble(new String(byte_y));
						Log.i("run()", "经度:" + longitude + " 纬度:" + latitude);
						Message msg = new Message();
						handler3.sendMessage(msg);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();

		} else {

		}
		guid = curBusDeviceInfo.getNewGuId();
		handler.sendEmptyMessage(1);
	}

	private Handler handler2 = new Handler() {
		@SuppressWarnings("deprecation")
		public void handleMessage(Message msg) {
			Bundle bundle = new Bundle();
			bundle = msg.getData();
			currentPoint = new GeoPoint((int) (bundle.getDouble("lat") * 1e6),
					(int) (bundle.getDouble("lon") * 1e6));
			/*
			 * // 创建自定义overlay mOverlay = new
			 * MyOverlay(getResources().getDrawable( R.drawable.bus_image_map),
			 * mapView); // 准备overlay 数据 item = new OverlayItem(currentPoint,
			 * "设备位置", ""); // 设置overlay图标，如不设置，则使用创建ItemizedOverlay时的默认图标
			 * Bitmap bmp = BitmapFactory.decodeResource(getResources(),
			 * R.drawable.bus_image_map); Bitmap dstBitmap = getRotatedBmp(bmp,
			 * baseDirect); item.setMarker(new BitmapDrawable(dstBitmap));
			 * mOverlay.addItem(item); // 将overlay 添加至MapView中
			 * mapView.getOverlays().add(mOverlay);
			 */
			/*
			 * mOverlay = new MyOverlay(getResources().getDrawable(
			 * R.drawable.bus_image_map), mapView);
			 */
			if (isFirstGetGps) {// 首次获取设备GPS
				mapView.getOverlays().clear();
				pt1 = new GeoPoint((int) (bundle.getDouble("lat") * 1e6),
						(int) (bundle.getDouble("lon") * 1e6));
				isFirstGetGps = false;
			} else {
				//
				pt2 = new GeoPoint((int) (bundle.getDouble("lat") * 1e6),
						(int) (bundle.getDouble("lon") * 1e6));
				// 画轨迹
				GraphicsOverlay graphicsOverlay = new GraphicsOverlay(mapView);
				Geometry lineGeometry = new Geometry();
				// 设定折线点坐标
				GeoPoint[] linePoints = new GeoPoint[2];
				linePoints[0] = pt1;
				linePoints[1] = pt2;
				lineGeometry.setPolyLine(linePoints);
				Symbol lineSymbol = new Symbol();
				Symbol.Color lineColor = lineSymbol.new Color();
				lineColor.red = 0;
				lineColor.green = 255;
				lineColor.blue = 0;
				lineColor.alpha = 255;
				lineSymbol.setLineSymbol(lineColor, 6);
				// 生成Graphic对象
				Graphic lineGraphic = new Graphic(lineGeometry, lineSymbol);
				graphicsOverlay.setData(lineGraphic);
				// 轨迹放入mapView中
				List<Overlay> list = mapView.getOverlays();
				if(list == null || list.size() == 0){
					mOverlay = new MyOverlay(getResources().getDrawable(
							R.drawable.bus_image_map), mapView);
					
					// 准备overlay 数据
					item = new OverlayItem(currentPoint, "设备位置", "");
					// 设置overlay图标，如不设置，则使用创建ItemizedOverlay时的默认图标
					Bitmap bmp = BitmapFactory.decodeResource(getResources(),
							R.drawable.bus_image_map);
					Bitmap dstBitmap = getRotatedBmp(bmp, baseDirect);
					item.setMarker(new BitmapDrawable(dstBitmap));
					mOverlay.addItem(item);
					// 将overlay 添加至MapView中
					mapView.getOverlays().add(mOverlay);
				}else{
					mOverlay = (MyOverlay)list.get(0);
					mOverlay.removeAll();
					if(ptLast != null){
						// 画旧新交接轨迹
						GraphicsOverlay graphicsOverlay_tmp = new GraphicsOverlay(mapView);
						Geometry lineGeometry_tmp = new Geometry();
						// 设定折线点坐标
						GeoPoint[] linePoints_tmp = new GeoPoint[2];
						linePoints_tmp[0] = ptLast;
						linePoints_tmp[1] = pt1;
						lineGeometry_tmp.setPolyLine(linePoints_tmp);
						Symbol lineSymbol_tmp = new Symbol();
						Symbol.Color lineColor_tmp = lineSymbol_tmp.new Color();
						lineColor_tmp.red = 0;
						lineColor_tmp.green = 255;
						lineColor_tmp.blue = 0;
						lineColor_tmp.alpha = 255;
						lineSymbol_tmp.setLineSymbol(lineColor_tmp, 6);
						// 生成Graphic对象
						Graphic lineGraphic_tmp = new Graphic(lineGeometry_tmp, lineSymbol_tmp);
						graphicsOverlay_tmp.setData(lineGraphic_tmp);
						mapView.getOverlays().add(graphicsOverlay_tmp);
						
						ptLast = null;
					}
					
					// 准备overlay 数据
					item = new OverlayItem(currentPoint, "设备位置", "");
					// 设置overlay图标，如不设置，则使用创建ItemizedOverlay时的默认图标
					Bitmap bmp = BitmapFactory.decodeResource(getResources(),
							R.drawable.bus_image_map);
					Bitmap dstBitmap = getRotatedBmp(bmp, baseDirect);
					item.setMarker(new BitmapDrawable(dstBitmap));
					mOverlay.addItem(item);
				}
				
				mapView.getOverlays().add(graphicsOverlay);
				mapController.setCenter(currentPoint);
				// 执行地图刷新使生效
				mapView.refresh();
				pt1 = pt2;
				/*
				// 轨迹放入mapView中
				mapView.getOverlays().add(graphicsOverlay);
				mapView.getOverlays().remove(mOverlay);
				
				mOverlay = new MyOverlay(getResources().getDrawable(
						R.drawable.bus_image_map), mapView);
				// 准备overlay 数据
				item = new OverlayItem(currentPoint, "设备位置", "");
				// 设置overlay图标，如不设置，则使用创建ItemizedOverlay时的默认图标
				Bitmap bmp = BitmapFactory.decodeResource(getResources(),
						R.drawable.bus_image_map);
				Bitmap dstBitmap = getRotatedBmp(bmp, baseDirect);
				item.setMarker(new BitmapDrawable(dstBitmap));
				mOverlay.addItem(item);
				// 将overlay 添加至MapView中
				mapView.getOverlays().add(mOverlay);
				mapController.setCenter(currentPoint);
				// 执行地图刷新使生效
				mapView.refresh();
				pt1 = pt2;
				*/
			} 
			/* mapView.getOverlays().add(mOverlay); */
			/*
			 * // 刷新地图 mapController.setCenter(currentPoint); mapView.refresh();
			 */
			// Log.i("handler2", "经度:"+longitude+" 纬度:"+latitude);
		};
	};

	/**
	 * 绘制折线，该折线状态随地图状态变化
	 * 
	 * @return 折线对象
	 */
	public Graphic drawLine(Bundle bundle) {
		GeoPoint pt1 = new GeoPoint((int) (bundle.getDouble("lat") * 1e6),
				(int) (bundle.getDouble("lon") * 1e6));
		GeoPoint pt2 = new GeoPoint((int) (39.916076 * 1e6),
				(int) (116.401313 * 1e6));
		GeoPoint pt3 = new GeoPoint((int) (39.916187 * 1e6),
				(int) (116.402831 * 1e6));
		// 构建线
		Geometry lineGeometry = new Geometry();
		// 设定折线点坐标
		GeoPoint[] linePoints = new GeoPoint[3];
		linePoints[0] = pt1;
		linePoints[1] = pt2;
		linePoints[2] = pt3;
		lineGeometry.setPolyLine(linePoints);
		Symbol lineSymbol = new Symbol();
		Symbol.Color lineColor = lineSymbol.new Color();
		lineColor.red = 0;
		lineColor.green = 0;
		lineColor.blue = 255;
		lineColor.alpha = 255;
		lineSymbol.setLineSymbol(lineColor, 8);
		// 生成Graphic对象
		Graphic lineGraphic = new Graphic(lineGeometry, lineSymbol);
		return lineGraphic;
	}

	// 定位手机的位置
	private void getCurrentLocation() {

	}

	// 初始化对应设备的GPS信息
	private void initDevLocationGPS() {
		guid = curBusDeviceInfo.getNewGuId();
		if (validateGps()) {
			longitude = curBusDeviceInfo.getLongitude();
			latitude = curBusDeviceInfo.getLatitude();
			// 刷新地图
			mapView.getOverlays().clear();
			mapView.refresh();
			isFirstGetGps = true;
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						if(IsExit){
							return;
						}
						String guid_Tmp = guid;
						LogUtils.d(TAG,"initDevLocationGPS" + latitude + longitude);
						Map<String, String> maps = GpsToBaiduUtil
								.ConvertGpsToBaidu(guid_Tmp,latitude, longitude);
						BASE64Decoder decoder = new BASE64Decoder();
						if (maps == null || maps.size() < 3)
							return;
						
						guid_Tmp = maps.get("guid");
						if(!guid_Tmp.equals(guid))
							return;
						
						if(IsExit)
							return;
						
						String x = maps.get("x");
						String y = maps.get("y");
						//invalid double:""
						byte[] byte_x = decoder.decodeBuffer(x);
						byte[] byte_y = decoder.decodeBuffer(y);
						longitude = Double.parseDouble(new String(byte_x));
						latitude = Double.parseDouble(new String(byte_y));
						Log.i("run()", "经度:" + longitude + " 纬度:" + latitude);
						Message msg = new Message();
						msg.obj = guid_Tmp;
						handler3.sendMessage(msg);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
		} else {

		}
		handler.sendEmptyMessage(1);
	}

	// 广播接收者
	public void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction("ACTION_NAME");
		// 注册广播
		registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	// 广播接收对象
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {		
			String action = intent.getAction();
			if (action.equals("ACTION_NAME")) {
				int eventType = intent.getIntExtra("eventType", 0);//
				if (isBroadcastRegister
						&& eventType == CALLBACKFLAG.DEVICE_EVENT_GPS_INFO) {// GPS基本
					String gpsDevID = intent.getStringExtra("gpsDevID");
					if (curBusDeviceInfo != null
							&& curBusDeviceInfo.getGuId().equals(gpsDevID)) {
						// Log.e(TAG, "====有木有接收到广播");
						longitude = intent
								.getDoubleExtra("gpsBaseLongitude", 0);
						latitude = intent.getDoubleExtra("gpsBaseLatitude", 0);
						baseDirect = intent.getIntExtra("gpsBaseDirect", 0);
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									
									if(IsExit)
										return;

									Log.i("ii", "获取的gps:经度:" + longitude + " 纬度:"
											+ latitude);
									String guid_tmp = guid;
									Map<String, String> maps = new HashMap<String, String>();
									maps = GpsToBaiduUtil.ConvertGpsToBaidu(
											guid_tmp,latitude, longitude);
									if (maps != null && maps.size() > 0) {
										guid_tmp = maps.get("guid");
										if(!guid_tmp.equals(guid))
											return;
										
										if(IsExit)
											return;
										
										Log.i("ii", "百度api解析数据:"+maps.get("x")
												+ "  " + maps.get("y"));
										BASE64Decoder decoder = new BASE64Decoder();
										String x = null, y = null;
											x = maps.get("x");
											y = maps.get("y");
											byte[] byte_x = decoder
													.decodeBuffer(x);
											byte[] byte_y = decoder
													.decodeBuffer(y);
											/*longitude = Double
													.parseDouble(new String(
															byte_x));
											latitude = Double
													.parseDouble(new String(
															byte_y));*/
											if (Double
													.parseDouble(new String(
															byte_x)) == 0.0
													|| Double
													.parseDouble(new String(
															byte_y)) == 0.0) {
												onReceive(context, intent);
											}
											Log.i("ii", "run()经度:" + Double
													.parseDouble(new String(
															byte_x))
													+ " 纬度:" + Double
													.parseDouble(new String(
															byte_y)));
											Message msg = new Message();
											Bundle bundle = new Bundle();
											bundle.putDouble("lat", Double
													.parseDouble(new String(
															byte_y)));
											bundle.putDouble("lon", Double
													.parseDouble(new String(
															byte_x)));
											bundle.putInt("num", num++);
											msg.setData(bundle);
											handler2.sendMessage(msg);
										}
								} catch (IOException e) {
									context.unregisterReceiver(mBroadcastReceiver);
									e.printStackTrace();
								}
							}
						}).start();
					}
				}
			}
		}

	};

	/**
	 * 获取旋转后的位图
	 */
	protected Bitmap getRotatedBmp(Bitmap bmp, int angle) {
		Matrix myMatrix = new Matrix();
		myMatrix.reset();
		myMatrix.postRotate(angle);
		Bitmap dstBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
				bmp.getHeight(), myMatrix, true);
		return dstBitmap;
	}

	// 异步处理百度地图坐标
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				JNVPlayerUtil.JNV_N_GetGPSStart(guid);// 请求下发GPS数据
				break;
			}
		};
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

	// 该类的对象的作用是在地图上绘制图标
	class PointOverlay extends Overlay implements OnClickListener {
		private GeoPoint geoPoint;
		private float myRotation;
		Matrix myMatrix;

		public PointOverlay(GeoPoint geoPoint, float myRotation) {
			this.geoPoint = geoPoint;
			this.myRotation = myRotation;
			myMatrix = new Matrix();
		}

		@Override
		public void onClick(View v) {

		}
	}

	// double类型保留6位小数
	public double convertDoubleType6(double d) {
		BigDecimal b = new BigDecimal(d);
		return b.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	// 验证是否具有有效的GPS数据
	private boolean validateGps() {
		if (curBusDeviceInfo != null && 0d != curBusDeviceInfo.getLatitude()
				&& 0d != curBusDeviceInfo.getLongitude()) {
			return true;
		} else {
			return false;
		}
	}

	// 初始化地图信息
	private void initUserMapInfo() {
		projection = mapView.getProjection();
		overlays = mapView.getOverlays();// 获取所有的图层
	}

	// 初始化UI控件
	private void initView() {
		mapView = (MyLocationMapView) findViewById(R.id.bmapView);
		// 获取地图控制器
		mapController = mapView.getController();
		// 设置地图是否响应点击事件
		mapController.enableClick(true);
		// 设置缩放级别
		mapController.setZoom(16);
		mapView.setBuiltInZoomControls(true);
		LocationClient client = new LocationClient(this);
		LocationClientOption option = new LocationClientOption();
		// 打开GPS
		option.setOpenGps(true);
		option.setCoorType("bd09ll");
		option.setScanSpan(5000);
		client.setLocOption(option);
		mapView.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				openOptionsMenu();
				return false;
			}
		});
	}

	// 常用事件监听，用来处理通常的网络错误，授权验证错误等
	class MyGeneralListener implements MKGeneralListener {

		@Override
		public void onGetNetworkState(int iError) {
			if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
				Toast.makeText(UserMapActivity.this, "您的网络出错啦！",
						Toast.LENGTH_LONG).show();
			} else if (iError == MKEvent.ERROR_NETWORK_DATA) {
				Toast.makeText(UserMapActivity.this, "输入正确的检索条件！",
						Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void onGetPermissionState(int iError) {
			if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
				// 授权Key错误：
				Toast.makeText(UserMapActivity.this, "请输入正确的授权Key！", 0).show();
			}
		}

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
			if (MyUtil.checkGoogleMapModule(this)) {
				isToOtherMap = true;
				IsExit = true;
				JNVPlayerUtil.JNV_N_GetGPSStop(guid);
				guid = "";
				// 刷新地图
				mapView.getOverlays().clear();
				Intent intent2 = new Intent();
				if (busListEntrance) {
					intent2.putExtra("DevObj", curBusDeviceInfo);
				}
				intent2.setClass(this, UserGoogleMapActivity.class);
				startActivity(intent2);
				finish();
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void initOverlay() {
		List<Overlay> list = mapView.getOverlays();
		if(list == null || list.size() == 0){
			// 创建自定义overlay
			mOverlay = new MyOverlay(getResources().getDrawable(
					R.drawable.bus_image_map), mapView);
			// 准备overlay 数据
			item = new OverlayItem(myGeoPoint, "设备位置", "");
			// 设置overlay图标，如不设置，则使用创建ItemizedOverlay时的默认图标
			item.setMarker(getResources().getDrawable(R.drawable.bus_image_map));
			mOverlay.addItem(item);
			// 将overlay 添加至MapView中
			mapView.getOverlays().add(mOverlay);
		}else{
			mOverlay = (MyOverlay)list.get(0);
			mOverlay.removeAll();
			if(ptLast != null){
				// 画旧新交接轨迹
				GraphicsOverlay graphicsOverlay_tmp = new GraphicsOverlay(mapView);
				Geometry lineGeometry_tmp = new Geometry();
				// 设定折线点坐标
				GeoPoint[] linePoints_tmp = new GeoPoint[2];
				linePoints_tmp[0] = ptLast;
				linePoints_tmp[1] = myGeoPoint;
				lineGeometry_tmp.setPolyLine(linePoints_tmp);
				Symbol lineSymbol_tmp = new Symbol();
				Symbol.Color lineColor_tmp = lineSymbol_tmp.new Color();
				lineColor_tmp.red = 0;
				lineColor_tmp.green = 255;
				lineColor_tmp.blue = 0;
				lineColor_tmp.alpha = 255;
				lineSymbol_tmp.setLineSymbol(lineColor_tmp, 6);
				// 生成Graphic对象
				Graphic lineGraphic_tmp = new Graphic(lineGeometry_tmp, lineSymbol_tmp);
				graphicsOverlay_tmp.setData(lineGraphic_tmp);
				mapView.getOverlays().add(graphicsOverlay_tmp);
				
				ptLast = null;
			}
			// 准备overlay 数据
			item = new OverlayItem(myGeoPoint, "设备位置", "");
			// 设置overlay图标，如不设置，则使用创建ItemizedOverlay时的默认图标
			item.setMarker(getResources().getDrawable(R.drawable.bus_image_map));
			mOverlay.addItem(item);
		}
		// 刷新地图
		mapView.refresh();
		
		/*
		// 创建自定义overlay
		mOverlay = new MyOverlay(getResources().getDrawable(
				R.drawable.bus_image_map), mapView);
		// 准备overlay 数据
		item = new OverlayItem(myGeoPoint, "设备位置", "");
		// 设置overlay图标，如不设置，则使用创建ItemizedOverlay时的默认图标
		item.setMarker(getResources().getDrawable(R.drawable.bus_image_map));
		mOverlay.addItem(item);
		// 将overlay 添加至MapView中
		mapView.getOverlays().add(mOverlay);
		// 刷新地图
		mapView.refresh();
		*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, R.string.video_stream);
		menu.add(0, 1, 1, R.string.cancel);
		menu.add(0, 2, 2, R.string.googleMap);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onPause() {
		mapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mapView.onResume();
		registerBoradcastReceiver();
		isBroadcastRegister = true;
		super.onResume();
	}

	@Override
	protected void onStop() {
		unregisterReceiver(mBroadcastReceiver);
		isBroadcastRegister = false;
		if(pt2 != null){
			ptLast = pt2;
		}else{
			ptLast = pt1;
		}

		super.onStop();
	}

	@Override
	protected void onDestroy() {
		IsExit = true;
		JNVPlayerUtil.JNV_N_GetGPSStop(guid);
		guid = "";
		mapView.destroy();
		bManager.stop();
		super.onDestroy();
	}

	public class MyOverlay extends ItemizedOverlay<OverlayItem> {

		public MyOverlay(Drawable arg0, MapView arg1) {
			super(arg0, arg1);
		}

		@Override
		protected boolean onTap(int arg0) {
			/*
			 * OverlayItem item = getItem(arg0); mCurItem = item;
			 */
			return super.onTap(arg0);
		}

		@Override
		public boolean onTap(GeoPoint arg0, MapView arg1) {
			return super.onTap(arg0, arg1);
		}
	}
}

/**
 * 继承MapView重写onTouchEvent实现泡泡处理操作
 * 
 */
class MyLocationMapView extends MapView {
	static PopupOverlay pop = null;// 弹出泡泡图层，点击图标使用

	public MyLocationMapView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MyLocationMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyLocationMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!super.onTouchEvent(event)) {
			// 消隐泡泡
			if (pop != null && event.getAction() == MotionEvent.ACTION_UP)
				pop.hidePop();
		}
		return true;
	}
}