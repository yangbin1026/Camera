package com.monitor.bus.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.monitor.bus.activity.R;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class MUtils {
	
	private static final int BUFFER_SIZE = 400000;//30000000;
	private static final String DB_PATH = 
			Environment.getExternalStorageDirectory().getAbsolutePath()+ "/BusMonitorClient";  //在手机里存放数据库的位置 

	/**
	 * 获取旋转后的位图
	 */
	public static Bitmap getRotatedBmp(Bitmap bmp, int angle) {
		Matrix myMatrix = new Matrix();
		myMatrix.reset();
		myMatrix.postRotate(angle);
		Bitmap dstBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
				bmp.getHeight(), myMatrix, true);
		return dstBitmap;
	}
	public static boolean hasUselessString(String ...strings){
		for(String s:strings){
			if(s==null||s.isEmpty()){
				return true;
			}
		}
		return false;
	}

	public static void debugToast(Context context,String msg){
		if(LogUtils.Debug){
			return;
		}
		Toast.makeText(context, ""+msg, Toast.LENGTH_LONG).show();
	}
	public static void toast(Context context,String msg){
		Toast.makeText(context, ""+msg, Toast.LENGTH_LONG).show();
	}
	// double类型保留6位小数
		public static double convertDoubleType6(double d) {
			BigDecimal b = new BigDecimal(d);
			return b.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
		}
		
		
		/**
		 * 保存到指定路径，如果需要的话
		 * @throws IOException 
		 */
	public static String saveIfNeed(Context context, String fileName, int id) { 
			String file = getDataPath(context, fileName);//文件路径
			/*if (!(new File(file).exists())) {//判断文件是否存在，若不存在则执行导入，否则直接打开数据库 
				
				try {
					InputStream is = context.getResources().openRawResource(id); 
					FileOutputStream fos;
					fos = new FileOutputStream(file);

					byte[] buffer = new byte[BUFFER_SIZE]; 
					int count = 0; 
					while ((count = is.read(buffer)) > 0) { 
						fos.write(buffer, 0, count); 
					} 
					fos.close(); 
					is.close(); 
					
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}*/
			File fl = null;
			FileOutputStream fos = null;
			try {
				File dir = new File(DB_PATH);//目录路径
				if(!dir.exists()){//如果不存在，则创建路径名
					Log.i("+++++++++++", "要存储的目录不存在");
					if(dir.mkdirs()){//创建该路径名，返回true则表示创建成功  
						Log.i("++++++++", "已经创建文件存储目录");
					}else{
						Log.i("++++++++", "创建目录失败");
					}
				}
				// 目录存在，则将apk中raw中的需要的文档复制到该目录下  
				fl = new File(file);
				boolean bFileNotExist = false;
				if(!fl.exists()){
					bFileNotExist = true;
				}else{
					if(fl.length() <= 0){
						fl.delete();
						bFileNotExist = true;
					}
				}
				if(bFileNotExist){// 文件不存在  
					Log.i("++++++++", "要打开的文件不存在");
					InputStream ins = context.getResources().openRawResource(id);
					fos = new FileOutputStream(fl);
					byte[] buffer = new byte[1024];
					int count = 0;
					while((count = ins.read(buffer)) > 0){
						fos.write(buffer, 0, count);
					}
					fos.close();
					ins.close();
				}
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch(IOException e){
				e.printStackTrace();
				
				if(fl != null){
					fl.delete();
				}
			}
			return file;
		}

		/**
		 * 获取数据完整路径
		 */
		private static String getDataPath(Context context, String fileName){
			String path = DB_PATH + "/" + fileName;
			return path;
		}
		
		public static void Vibrate(final Activity activity, long milliseconds){
			Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
			vib.vibrate(milliseconds);
		}
		public static void Vibrate(final Activity activity,long[] pattern,boolean isRepeat){
			Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
			vib.vibrate(pattern, isRepeat? 1: -1);
		}
		
		public static String getUpdateVerJSON(String serverPath) throws Exception {
			StringBuilder newVerJSON = new StringBuilder();
			HttpClient client = new DefaultHttpClient();// 新建http客户端
			HttpParams httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 3000);// 设置连接超时的范围
			// serverPath是服务器端version.json文件的路径
			HttpResponse response = client.execute(new HttpGet(serverPath));
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						entity.getContent(), "utf-8"), 8192);
				String line = null;
				while ((line = reader.readLine()) != null) {
					newVerJSON.append(line + "\n");// 按行读取放入StringBuilder中
				}
				reader.close();
			}
			return newVerJSON.toString();
		}
		public static int getVerCode(Context context) throws Exception{
			int verCode = -1;
			verCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
			return verCode;
		}
		//获取旧应用程序名称版本
		public static String getVerName(Context context) throws Exception{
			String verName = "";
			verName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
			return verName;
		}
		//获取旧应用程序的名字
		public static String getAppName(Context context){
			String appName = context.getResources().getText(R.string.app_name).toString();
			return appName;
		}
		

}
