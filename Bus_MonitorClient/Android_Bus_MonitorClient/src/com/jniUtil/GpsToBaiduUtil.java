package com.jniUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class GpsToBaiduUtil {
	// 百度地图经纬度接口转换
	public static Map<String, String> ConvertGpsToBaidu(String strGuid,double latitude,
			double longitude){
		String interface_url = "http://api.map.baidu.com/ag/coord/convert?from=0&to=4&x="
				+ longitude + "&y=" + latitude;
		Map<String, String> map = null;
		try {
			String strResult = "";
			URL url = new URL(interface_url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);

			HttpGet httpGet = new HttpGet(interface_url);
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = httpClient.execute(httpGet);
			if (conn.getResponseCode() == 200) {
				strResult = EntityUtils.toString(response.getEntity());
//				LogUtils.i("Baidu Map api:", strResult);
				JSONObject jsonObject = new JSONObject(strResult);
				String x = jsonObject.getString("x");
				String y = jsonObject.getString("y");
				String error = jsonObject.getString("error");
				int error_in = Integer.parseInt(error);
				if(error_in == 0){
				map = new HashMap<String, String>();
				map.put("guid", strGuid);
				map.put("x", x);//经度
				map.put("y", y);//纬度
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
}
