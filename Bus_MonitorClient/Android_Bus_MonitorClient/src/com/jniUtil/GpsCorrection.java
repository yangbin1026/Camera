package com.jniUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

//gps校正类
public class GpsCorrection {
	protected int mTableWidth = 660;
	protected int mTableHeihgt = 450;
	protected int mTableSize = 0;
	protected static float [] mTableLongitude = null;
	protected static float [] mTableLatitude = null;
	protected boolean mIsInitialize = false;
	protected static GpsCorrection g_GpsCorrection = null;
	
	//构造函数
	public GpsCorrection(){
		mTableSize = mTableWidth * mTableHeihgt;
		mTableLongitude = new float [mTableSize];
		mTableLatitude = new float [mTableSize];
		for(int m=0; m<mTableSize; m++){
			mTableLongitude[m] = 0;
			mTableLatitude[m] = 0;
		}
		
		g_GpsCorrection = this;
	}
	
	//获取实例
	public static GpsCorrection getInstance(){
		if(g_GpsCorrection == null){
			g_GpsCorrection = new GpsCorrection();
		}
		
		return g_GpsCorrection;
	}
	
	//获取位置
	protected int getPosition(long i,long j){
		return (int)(i + mTableWidth*j);
	}

	/*
	 * 功能：初始化Google地图校正表
	 * 参数：strFileName,校正表文件名
	 */
	public int initialize(String strFileName){
		if(mIsInitialize){
			return 0;
		}
		
		if(strFileName == null || "".equals(strFileName)){
			return -1;
		}
		
		File f = null;
		InputStream is = null;
		try{
			f = new File(strFileName);
			is = new FileInputStream(f);
			byte []data = new byte[50];
			int byteread = 0;
			String strData = null;
			String strlon1 = null;
			String strlat1 = null;
			String strlon2 = null;
			String strlat2 = null;
			int nIndex = 0;
			while(true){
				byteread = is.read(data);
				if(byteread == -1){
					break;
				}
				
				strData = new String(data,0,byteread);
				//去掉组后的回车换行
				strData = strData.substring(0, strData.length() - 2);
				
				strlon1 = strData.substring(0,12);
				strlon1 = strlon1.trim();
				strlat1 = strData.substring(12,24);
				strlat1 = strlat1.trim();
				Integer intlon1 = Integer.valueOf(strlon1);
				Integer intlat1 = Integer.valueOf(strlat1);
				mTableLongitude[nIndex] = (float) ((intlon1.floatValue()) / 100000.0);
				mTableLatitude[nIndex] = (float) ((intlat1.floatValue()) / 100000.0);
				nIndex++;
				
				strlon2 = strData.substring(24,36);
				strlon2 = strlon2.trim();
				strlat2 = strData.substring(36,48);
				strlat2 = strlat2.trim();
				Integer intlon2 = Integer.valueOf(strlon2);
				Integer intlat2 = Integer.valueOf(strlat2);
				mTableLongitude[nIndex] = (float) ((intlon2.floatValue()) / 100000.0);
				mTableLatitude[nIndex] = (float) ((intlat2.floatValue()) / 100000.0);
				nIndex++;
			}

			mIsInitialize = true;
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
		}
		
		return 0;
	}
	
	//反初始化
	public int unInitialize(){
		mIsInitialize = false;
		return 0;
	}
	
	//是否初始化
	public boolean IsInitialize(){
		return mIsInitialize;
	}
	
	class LongitudeLatitude{
		protected float mLongitude = 0;
		protected float mLatitude = 0;
		
		public float getLongitude() {
			return mLongitude;
		}
		public float getLatitude() {
			return mLatitude;
		}
		public void setLongitude(float Longitude) {
			mLongitude = Longitude;
		}
		public void setLatitude(float Latitude) {
			mLatitude = Latitude;
		}
		
	};
	
	/*
	 * 功能：校正gps
	 * 参数：dbLongitude，经度；dbLatitude，纬度
	 */
	public LongitudeLatitude fixGPS(float dbLongitude, float dbLatitude){
		float dbLongitude_out = 0;
		float dbLatitude_out = 0;
		
		LongitudeLatitude llObject = new LongitudeLatitude();
		//台湾范围不校正
		//北纬21.9度到26.4度
		//东经117.6度到121.6度
		if(dbLongitude >= 117.6 && dbLongitude <= 121.6 &&
			dbLatitude >= 21.9 && dbLatitude <= 26.4)
		{
			dbLongitude_out = dbLongitude;
			dbLatitude_out = dbLatitude;
			
			llObject.setLatitude(dbLatitude_out);
			llObject.setLongitude(dbLongitude_out);
			return llObject;
		}
		
		//最东端 东经135度2分30秒 黑龙江和乌苏里江交汇处 
		//最西端 东经73度40分 帕米尔高原乌兹别里山口（乌恰县） 
		//最南端 北纬3度52分 南沙群岛曾母暗沙 
		//最北端 北纬53度33分 漠河以北黑龙江主航道（漠河县）2日本朝鲜韩国
		//	2分 0.0349
		//	40分 0.6981
		//	52分  0.9076
		
		if(dbLongitude > 135 || dbLongitude < 73 ||
			dbLatitude > 54 || dbLatitude < 3)
		{
			dbLongitude_out = dbLongitude;
			dbLatitude_out = dbLatitude;
			
			llObject.setLatitude(dbLatitude_out);
			llObject.setLongitude(dbLongitude_out);
			return llObject;
		}
		
		long i, j, k;
		float x1, y1, x2, y2, x3, y3, x4, y4, xtry, ytry, dx, dy;
		float t, u;
				
		xtry = dbLongitude;
		ytry = dbLatitude;
		
		for(k=0; k<10; ++k)
		{
			if( xtry < 72 || xtry > 137.9 || ytry < 10 || ytry > 54.9)
			{
				break;
			}
			
			i = (long) ((xtry - 72.0) * 10.0);
			j = (long) ((ytry - 10.0) * 10.0);
			
			
			x1 = mTableLongitude[getPosition(i, j)];
			y1 = mTableLatitude[getPosition(i, j)];
			x2 = mTableLongitude[getPosition(i+1, j)];
			y2 = mTableLatitude[getPosition(i+1, j)];
			x3 = mTableLongitude[getPosition(i+1, j+1)];
			y3 = mTableLatitude[getPosition(i+1, j+1)];
			x4 = mTableLongitude[getPosition(i, j+1)];
			y4 = mTableLatitude[getPosition(i, j+1)];
			
			t = (float)((xtry - 72.0 - 0.1 * i) * 10.0);
			u = (float)((ytry - 10.0 - 0.1 * j) * 10.0);
			
			dx = (float)((1.0-t)*(1.0-u)*x1 + t*(1.0-u)*x2 + t*u*x3 + (1.0-t)*u*x4 - xtry);
			dy = (float)((1.0-t)*(1.0-u)*y1 + t*(1.0-u)*y2 + t*u*y3 + (1.0-t)*u*y4 - ytry);
			
			xtry = (float)((xtry + dbLongitude + dx)/2.0);
			ytry = (float)((ytry + dbLatitude + dy)/2.0);
		}
		
		dbLongitude_out = xtry;
		dbLatitude_out = ytry;
		
		llObject.setLatitude(dbLatitude_out);
		llObject.setLongitude(dbLongitude_out);
		return llObject;
	}
}
