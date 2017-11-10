package com.monitor.bus.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jniUtil.MyUtil;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.service.CurrentVersionInfo;
import com.monitor.bus.service.GetUpdateJsonInfo;


public class SettingActivity extends BaseActivity{

	private static final String TAG = "SettingActivity";
	private EditText storeEditText, serviceEditText;
	private TextView defMapTextView;
	private CheckBox cbIsGpsCorrection = null;
	private boolean IsGpsCorrection = false;

	private boolean isCurDefBaiduMap;
	private SharedPreferences spf = null; 
	private String newVerName, newAppName;// 新版本名称,新应用程序名称
	private int newVerCode;// 新版本号
	private int currentCode = 0;//旧版本号
	private ProgressDialog pbar;// 进度条对话框
	private Handler handler = new Handler();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyUtil.initTitleName(this,R.layout.setting,R.string.setting);
		findView();
		initPreferences();
	}

	/**
	 * 初始化共享参数
	 */
	private void initPreferences() {
		spf 
		= getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		
		String serviceUrl 
		= spf.getString(Constants.SERVICE_URL_KEY, Constants.SERVICE_URL);
		
		String storeUrl 
		= spf.getString(Constants.STORE_URL_KEY, Constants.STORE_URL);

		serviceEditText.setText(serviceUrl);
		storeEditText.setText(storeUrl);

		isCurDefBaiduMap = spf.getBoolean(Constants.DEFAULT_MAP_KEY, 
				Constants.IS_DEFAULT_BAIDU_MAP);

		defMapTextView.setText(isCurDefBaiduMap 
				? R.string.baiduMap 
						: R.string.googleMap);
		
		IsGpsCorrection = spf.getBoolean(Constants.GOOGLE_GPS_CORRRECTION, Constants.IS_GOOGLE_GPS_CORRRECTION);
		if(cbIsGpsCorrection != null){
			cbIsGpsCorrection.setChecked(IsGpsCorrection);
			cbIsGpsCorrection.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Auto-generated method stub
					IsGpsCorrection = isChecked;
					saveBooleanPreferences(Constants.GOOGLE_GPS_CORRRECTION, isChecked);
				}
			});
		}
	}

	/**
	 * 保存字符串共享参数
	 */
	public void saveStringPreferences(String key, String value){
		spf.edit().putString(key, value).commit();
	}
	
	/**
	 * 保存布尔型共享参数
	 */
	public void saveBooleanPreferences(String key, boolean value){
		spf.edit().putBoolean(key, value).commit();
	}
	
	/**
	 * 获取组件
	 */
	private void findView() {
		storeEditText = (EditText) findViewById(R.id.storeUrl);
		serviceEditText = (EditText) findViewById(R.id.serviceUrl);
		defMapTextView = (TextView) findViewById(R.id.defMapText);
		cbIsGpsCorrection = (CheckBox) findViewById(R.id.checkBox_googlegpsCorrection);
	}

	/**
	 * 保存
	 * */
	public void saveUrl(View view){
		
		Log.i(TAG, "保存"+view);
		String url;
		
		switch (view.getId()) {
		case R.id.serviceButton:
			url = serviceEditText.getText().toString();
			if(!"".equals(url.trim().toString())){
				saveStringPreferences(Constants.SERVICE_URL_KEY, url);
				Toast.makeText(this, R.string.save_succeed, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.storeButton:
			url = storeEditText.getText().toString();
			if(!"".equals(url.trim().toString())){
				saveStringPreferences(Constants.STORE_URL_KEY, url);
				Toast.makeText(this, R.string.save_succeed, Toast.LENGTH_SHORT).show();
			}
			break;

		default:
			break;
		}
	}

	/**
	 * 改变
	 * */
	public void change(View view){
		Log.i(TAG, "改变"+view);
		isCurDefBaiduMap = !isCurDefBaiduMap;
		saveBooleanPreferences(Constants.DEFAULT_MAP_KEY, isCurDefBaiduMap);
		if(isCurDefBaiduMap){
			defMapTextView.setText(R.string.baiduMap);
		}else{
			defMapTextView.setText(R.string.googleMap);
		}
	}
	/*检查新版本*/
	public void checkVersion(View view){
		Log.i(TAG, "检查"+view);
		checkToUpdate();
	}
		
	
	/**
	 * 进行版本的比较，提示是否更新当前的应用
	 */
	private void checkToUpdate() {
		Log.i(".....................", "checkToUpdate()" + "....");
		if (getServerVersion()) {//发现新版本
			
			try {
				currentCode = CurrentVersionInfo.getVerCode(this);
				if (newVerCode > currentCode) {
					// 弹出更新提示对话框
					showUpdateDialog();
				}else{//没有发现新版本
					Toast.makeText(getApplicationContext(),R.string.this_latest_version , Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	/**
	 * 显示更新提示框
	 */
	private void showUpdateDialog() throws Exception {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer();
		sb.append("当前版本：");
		sb.append(CurrentVersionInfo.getVerName(this));
		sb.append("VerCode:");
		sb.append(CurrentVersionInfo.getVerCode(this));
		sb.append("\n");
		sb.append("发现新版本：");
		sb.append(newVerName);
		sb.append("NewVerCode:");
		sb.append(newVerCode);
		sb.append("\n");
		sb.append("是否更新?");
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setCancelable(false);
		dialog.setTitle("软件更新")
				.setMessage(sb.toString())
				.setPositiveButton("更新", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						showProgressBar();// 更新当前版本
					}
				})
				.setNegativeButton("暂时不更新",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub

							}
						});
		dialog.create().show();
	}

	/**
	 * 下载最新版本进度条
	 */
	private void showProgressBar() {
		// TODO Auto-generated method stub
		pbar = new ProgressDialog(this);
		pbar.setTitle("正在下载");
		pbar.setMessage("请稍候...");
		pbar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pbar.setCanceledOnTouchOutside(false);
		downAppFile(Constants.SERVER_IP + newAppName);
	}

	/**
	 * 下载更新的APK文件
	 */
	private void downAppFile(final String url) {
		// TODO Auto-generated method stub
		pbar.show();
		new Thread() {
			public void run() {
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(url);
				HttpResponse response;
				try {
					response = client.execute(get);
					HttpEntity entity = response.getEntity();
					long length = entity.getContentLength();
					Log.i("------------", "length : " + (int) length);
					InputStream is = entity.getContent();
					FileOutputStream fileOutputStream = null;
					if(is == null){
						throw new RuntimeException("inputStream is null");
					}
					File file = new File(
							Environment.getExternalStorageDirectory() + "/",
							newAppName);
					Log.i("==============", "file : "+file.getAbsolutePath().toString());
					fileOutputStream = new FileOutputStream(file);
					/*fileOutputStream = LoginActivity.this.openFileOutput(file.toString(), Context.MODE_WORLD_READABLE);*/
					byte[] buf = new byte[1024];
					int ch = -1;
					do {
						ch = is.read(buf);
						if (ch <= 0) {
							break;
						}
						fileOutputStream.write(buf, 0, ch);
					} while (true);
					is.close();
					fileOutputStream.close();
					haveDownLoad();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * 下载完成时要将进度条对话框取消并进行是否安装新应用的提示
	 */
	protected void haveDownLoad() {

		handler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				pbar.cancel();
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						SettingActivity.this);
				dialog.setCancelable(false);
				dialog.setTitle("下载完成")
						.setMessage("是否安装新的应用")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										installNewApk();
										
										finish();
									}
								})
						.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										/* finish(); */
									}
								}).create().show();
			}
		});
	}

	/**
	 * 安装新的应用程序
	 */
	private void installNewApk() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(
				Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/", newAppName)),
				"application/vnd.android.package-archive");
		startActivity(intent);
	}

	/**
	 * 将从服务器version.json获得的字符串解析出我们需要的版本信息
	 */
	private boolean getServerVersion() {
		// TODO Auto-generated method stub
		try {
			Log.i("--------------", "ip:" + Constants.SERVER_IP);
			String newVerJSON = GetUpdateJsonInfo
					.getUpdateVerJSON(Constants.SERVER_IP + "version.json");
			Toast.makeText(this, "检查更新中.."+newVerJSON, 0).show();
			Log.i("--------------", "newVerJSON:" + newVerJSON);
			JSONArray jsonArray = new JSONArray(newVerJSON);
			if (jsonArray.length() > 0) {
				JSONObject obj = jsonArray.getJSONObject(0);
				Log.i("--------------", "obj:" + obj.toString());
				try {
					newVerCode = Integer.parseInt(obj.getString("verCode"));
					newVerName = obj.getString("verName");
					newAppName = obj.getString("apkName");
				} catch (Exception e) {
					newVerCode = -1;
					newVerName = "";
					return false;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * 设置中添加退出程序按钮功能
	 */
	public void exitProgram(View view){
		isCompleteExit = true;
		finish();
	}
}
