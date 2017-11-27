package com.jniUtil;


import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.jniUtil.GpsCorrection.LongitudeLatitude;
import com.monitor.bus.activity.R;
import com.monitor.bus.activity.UserGoogleMapActivity;
import com.monitor.bus.activity.UserMapActivity;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.model.DeviceInfo;

public class MyUtil{
	private static String TAG = "MyUtil";
	public static TextView tilte_name;//标题栏名称
  /**
   * 判断文件MimeType的方法
   * @param f
   * @param isOpen 目的打开方式为true
   * @return
   */
  @SuppressLint("DefaultLocale")
public static String getMIMEType(File f,boolean isOpen){
    String type="";
    String fName=f.getName();
    /* 取得扩展名 */
    String end=fName.substring(fName.lastIndexOf(".")+1,fName.length()).toLowerCase(); 
    if(isOpen){
           if(end.equals("jpg")||end.equals("gif")||end.equals("png")||end.equals("jpeg")||end.equals("bmp")){
              type = "image";
            }
            else{
              /* 如果无法直接打开，就跳出软件列表给用户选择 */
              type="*";
            }
            type += "/*"; 
    }else{
    	 if(end.equals("jpg")||end.equals("gif")||end.equals("png")||end.equals("jpeg")||end.equals("bmp")){
            type = "image";
          }
    }
    return type; 
  }
  
  /**
   * 缩放图片的方法
   * @param bitMap
   * @param x
   * @param y
   * @param newWidth
   * @param newHeight
   * @param matrix
   * @param isScale
   * @return
   */
  public static Bitmap fitSizePic(File f){ 
    Bitmap resizeBmp = null;
    BitmapFactory.Options opts = new BitmapFactory.Options(); 
    //数字越大读出的图片占用的heap越小 不然总是溢出
    if(f.length()<20480){         //0-20k
      opts.inSampleSize = 1;
    }else if(f.length()<51200){   //20-50k
      opts.inSampleSize = 2;
    }else if(f.length()<307200){  //50-300k
      opts.inSampleSize = 4;
    }else if(f.length()<819200){  //300-800k
      opts.inSampleSize = 6;
    }else if(f.length()<1048576){ //800-1024k
      opts.inSampleSize = 8;
    }else{
      opts.inSampleSize = 10;
    }
    resizeBmp = BitmapFactory.decodeFile(f.getPath(),opts);
    return resizeBmp; 
  }

  /**
   * 文件大小描述
   * @param f
   * @return
   */
  public static String  fileSizeMsg(File f){ 
    int sub_index = 0;
    String  show = "";
    if(f.isFile()){
          long length = f.length();
          if(length>=1073741824){
            sub_index = (String.valueOf((float)length/1073741824)).indexOf(".");
            show = ((float)length/1073741824+"000").substring(0,sub_index+3)+"GB";
          }else if(length>=1048576){
            sub_index = (String.valueOf((float)length/1048576)).indexOf(".");
            show =((float)length/1048576+"000").substring(0,sub_index+3)+"MB";
          }else if(length>=1024){
            sub_index = (String.valueOf((float)length/1024)).indexOf(".");
            show = ((float)length/1024+"000").substring(0,sub_index+3)+"KB";
          }else if(length<1024){
            show = String.valueOf(length)+"B";
          }
    }
    return show; 
  }
  
  /** 
   * 初始化当前标题栏
   * @param currentContext 当前activity
   * @param currentLayout 当前加载的布局
   * @param currentTitle 当前标题内容
   */
  public static void initTitleName(Activity currentContext,int currentLayout, int currentTitle){
	  Log.i(TAG, "++++++++++++初始化当前的布局和标题名称");
	  currentContext.setContentView(currentLayout);
	  // 设置标题栏内容
	  tilte_name = (TextView)currentContext.findViewById(R.id.tilte_name);
	  if(currentTitle != 0){
		  tilte_name.setText(currentTitle);
	  }else{
		  currentContext.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 屏幕保持常亮
	  }
  }
  
  
  /**
   * 获取存储录像及照片的路径 由当前日期+设备IP+当前通道号
   * @param path
   * @param currentDeviceInfo
   * @return
   */
  public static String getCurrentFilePath(String path,DeviceInfo currentDeviceInfo){
	  String currentDate = getCurrentDateTime(Constants.YMD_FORMAT);
	  String currentPath = path + currentDate + "/"+ currentDeviceInfo.getDeviceName()+"/" + currentDeviceInfo.getCurrentChn()+"/";
	  return currentPath;
  }
  /**
   * 	获取当前时间,并制定返回格式 如 yyyyMMddHHmmss , yyyyMMdd
   * @param fomat
   * @return
   */
  @SuppressLint("SimpleDateFormat")
public static String getCurrentDateTime(String fomat){
		Date date = new Date(); 
		//指定的时间格式
		SimpleDateFormat from = new SimpleDateFormat(fomat); 
		//格式化时间
		String times = from.format(date); 
	  return times;
  }
  /**
   * 给出相应的提示信息
   * @param currentContext
   * 				当前的上下文
   * @param message
   * 				提示信息
   */
  public static void commonToast(Context currentContext ,int stringValue){
	  Toast.makeText(currentContext, stringValue, Toast.LENGTH_SHORT).show();
  }
  
	/**
	 * 字符串转换到时间格式
	 * @param dateStr 需要转换的字符串
	 * @param formatStr 需要格式的目标字符串  举例 yyyy-MM-dd
	 * @return Date 返回转换后的时间
	 * @throws ParseException 转换异常
	 */
	@SuppressLint("SimpleDateFormat")
	public static Date stringToDate(String dateStr,String formatStr){
		DateFormat sdf=new SimpleDateFormat(formatStr);
		Date date=null;
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	/**
	 * 检测当前网络是否连接
	 */
	public static boolean isConnect(Context context) { 
		// 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理） 
		try { 
			ConnectivityManager connectivity = (ConnectivityManager) context 
					.getSystemService(Context.CONNECTIVITY_SERVICE); 
			if (connectivity != null) { 
				// 获取网络连接管理的对象 
				NetworkInfo info = connectivity.getActiveNetworkInfo(); 
				if (info != null&& info.isConnected()) { 
					// 判断当前网络是否已经连接 
					if (info.getState() == NetworkInfo.State.CONNECTED) { 
						return true; 
					} 
				} 
			} 
		} catch (Exception e) { 
			Log.v("error",e.toString()); 
		} 
		return false; 
	} 
	
	/**
	 * double类型保留6位小数
	 */
	public static double convertDoubleType6(double d){
		BigDecimal   b   =   new   BigDecimal(d);  
		 return  b.setScale(6,   BigDecimal.ROUND_HALF_UP).doubleValue();  
	}
	
	/**
	 * GPS位置转google地图位置
	 * @return
	 */
	public static LatLng fromWgs84ToGoogle(double latitude, double longitude){
		
		Log.i(TAG, "换算前的经纬：lat=" + latitude + ",lon=" + longitude);
		LatLng ret = new LatLng(latitude, longitude);
		//对位置进行运算
		if(GpsCorrection.getInstance().IsInitialize())
		{
			LongitudeLatitude dstLongitudeLatitude = GpsCorrection.getInstance().fixGPS((float)longitude,(float)latitude);
			LatLng tmp = new LatLng(dstLongitudeLatitude.mLatitude, dstLongitudeLatitude.mLongitude);
			
			if(tmp!=null){
				ret = tmp;	
			}
		}

		return ret;
	}
	
	private static final String CHINA = "中国";
	/**
	 * 验证是否是中文语言环境
	 */
	public static boolean isChina(Context context) {
		String country = context.getString(R.string.country);
		return CHINA.equals(country);
	}
	
	/**
	 * 开启地图
	 * @return 开启成功返回true,否则相反
	 */
	public static boolean startMapActivity(Activity context, Intent intent) {
		
//		if(MyUtil.isChina(MainListActivity.this)){
		if(MyUtil.getDefMapIsBaiduMap(context)){
			intent.setClass(context, UserMapActivity.class);
			context.startActivity(intent);
		}else{
			if( MyUtil.checkGoogleMapModule(context) ){
				intent.setClass(context, UserGoogleMapActivity.class);
				context.startActivity(intent);
			}else{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 验证谷歌地图模块完整性
	 */
	public static boolean checkGoogleMapModule(Activity activity){
		boolean ret = true;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		if( !apkExists(activity, Constants.SERVICE_APK_NAME) ){
			Log.e(TAG, "未安装google服务");
			intent.setData(getGoogleServiceUri(activity));
			activity.startActivity(intent);
			ret = false;
		}
		if( !apkExists(activity, Constants.STORE_APK_NAME) ){
			Log.e(TAG, "未安装google市场");
			intent.setData(getGoogleStoreUri(activity));
			activity.startActivity(intent);
			ret = false;
		}
		if(!ret){
			Toast.makeText(activity, R.string.googleMapSupport, Toast.LENGTH_LONG).show();
		}
		return ret;
	}
	
	/**
	 * 根据包名判断该应用是否已经安装
	 */
	public static boolean apkExists(Context context, String packageName) {
		PackageManager pManager = context.getPackageManager();
		List<PackageInfo> packageInfoList = pManager.getInstalledPackages(0);
		for (int i = 0; i < packageInfoList.size(); i++) {
			PackageInfo pkg = packageInfoList.get(i);
//			Log.d(TAG, "pkg name " + pkg.packageName);
			if (pkg.packageName.equals(packageName))
				return true;
		}
		return false;
	}
	
	/**
	 * 获取谷歌市场的下载地址
	 */
	public static Uri getGoogleStoreUri(Activity activity){
		SharedPreferences spf = activity.getSharedPreferences(
				Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		
		String storeUrl = spf.getString(Constants.STORE_URL_KEY, Constants.STORE_URL);
		Log.i(TAG, "谷歌市场下载地址："+storeUrl);
		return Uri.parse(storeUrl);
	}
	
	/**
	 * 获取谷歌服务的下载地址
	 */
	public static Uri getGoogleServiceUri(Activity activity){
		SharedPreferences spf = activity.getSharedPreferences(
				Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		
		String serviceUrl 
		= spf.getString(Constants.SERVICE_URL_KEY, Constants.SERVICE_URL);
		Log.i(TAG, "谷歌服务下载地址："+serviceUrl);
		return Uri.parse(serviceUrl);
	}
	
	/**
	 * 当前默认地图是否是百度地图
	 */
	public static boolean getDefMapIsBaiduMap(Activity activity){
		
		SharedPreferences spf = activity.getSharedPreferences(
				Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		
		boolean isCurDefBaiduMap = spf.getBoolean(Constants.DEFAULT_MAP_KEY, 
				Constants.IS_DEFAULT_BAIDU_MAP);
		return isCurDefBaiduMap;
	}

	
}
