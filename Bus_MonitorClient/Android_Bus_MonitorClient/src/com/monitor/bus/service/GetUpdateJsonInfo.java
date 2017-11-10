package com.monitor.bus.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * 从服务器读取版本信息文件(Json格式文件)
 * 
 */
public class GetUpdateJsonInfo {
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
}
