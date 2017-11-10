package com.jniUtil;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class JsonParse {
	
	private static final String TAG = "JsonParse";

	/**
	 * 解析获取的经纬信息
	 * @return 出错时返回null
	 */
	public static LatLng getLatLng(String str){
		LatLng ret = null;
		
		try {
			
			JSONObject jObject = new JSONObject(str);
			double lon = jObject.getDouble("Longitude");
			double lat = jObject.getDouble("Latitude");
//			Log.i(TAG, "解析获得：lon="+lon+",lat="+lat);
			ret = new LatLng(lat, lon);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return ret;
	}
}
