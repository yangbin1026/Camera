package com.monitor.bus.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.jniUtil.JNVPlayerUtil;
import com.jniUtil.MyUtil;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.consts.DBUser.User;
import com.monitor.bus.control.LoginEventControl;
import com.monitor.bus.database.DBHelper;
import com.monitor.bus.model.LoginInfo;
import com.monitor.bus.service.CurrentVersionInfo;
import com.monitor.bus.service.GetUpdateJsonInfo;
import com.monitor.bus.utils.LogUtils;
import com.monitor.bus.utils.MyUtils;
import com.monitor.bus.utils.SPUtils;
import com.monitor.bus.view.MyEditText;

/**
 * 用户登陆
 * 
 */
public class LoginActivity extends Activity implements android.view.View.OnClickListener, OnCheckedChangeListener {
	private static String TAG = "LoginActivity";

	private MyEditText et_userName;// 用户名
	private MyEditText et_password;// 密码
	private MyEditText et_login_port;// 端口
	private MyEditText et_login_address;// 地址
	private ProgressDialog pbar;// 进度条对话框
	private EditText mUserName;
	private EditText mPassword;
	private EditText mIP;
	private EditText mPort;
	private Button btn_login;
	private CheckBox cb_remenber, cb_autoLogin;

	private String newVerName, newAppName;// 新版本名称,新应用程序名称
	private int newVerCode;// 新版本号
	private int currentCode = 0;// 旧版本号
	private InetAddress iAdd;
	private Handler handler = new Handler();
	private LoginEventControl loginControl;// 登陆回调类

	static {
		try {
			System.loadLibrary("ffmpeg");
			System.loadLibrary("JNVCommon_jni");
			System.loadLibrary("JNVPlayer_jni");
		} catch (UnsatisfiedLinkError e) {
			Log.e(TAG, "load库文件异常:" + e.getMessage());
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		loginControl = new LoginEventControl(this);
		JNVPlayerUtil.JNV_Init(Constants.SCREEN_COUNT);// 初始化so
		initView();
	}

	private void initView() {
		et_userName = (MyEditText) findViewById(R.id.userName);
		et_password = (MyEditText) findViewById(R.id.login_password);
		et_login_port = (MyEditText) findViewById(R.id.login_port);
		et_login_address = (MyEditText) findViewById(R.id.login_address);
		et_userName.setImage(R.drawable.account);
		et_password.setImage(R.drawable.password);
		et_login_port.setImage(R.drawable.port);
		et_login_address.setImage(R.drawable.ip);
		cb_remenber = (CheckBox) findViewById(R.id.remember);
		cb_autoLogin = (CheckBox) findViewById(R.id.cb_auto_login);
		cb_remenber.setOnCheckedChangeListener(this);
		cb_autoLogin.setOnCheckedChangeListener(this);
		mUserName = (EditText) et_userName.findViewById(R.id.edit_text_input);
		mPassword = (EditText) et_password.findViewById(R.id.edit_text_input);
		mIP = (EditText) et_login_address.findViewById(R.id.edit_text_input);
		mPort = (EditText) et_login_port.findViewById(R.id.edit_text_input);
		btn_login = (Button) findViewById(R.id.login);
		btn_login.setOnClickListener(this);

		SharedPreferences userInfo = getSharedPreferences("user_info", 0);
		String u = userInfo.getString("userName", "");
		String p = userInfo.getString("password", "");
		String ip = userInfo.getString("login_address", "");
		String port = userInfo.getString("login_port", "");

		et_password.setTextViewText( p);
		et_login_address.setTextViewText( ip);
		et_login_port.setTextViewText( port + "");
		et_userName.setTextViewText( u);
		
		et_password.setEditPasswordType();
		et_login_port.setEditNumberType();
		et_login_address.setIpConfigType();
		et_userName.setEditFocus();

		if (LogUtils.Debug) {
			mUserName.setText("123");
			mPassword.setText("123");
			mIP.setText("183.61.171.28");
			mPort.setText("6008");
		}
	}

	@Override
	public void onBackPressed() {
		if (LoginEventControl.myProgress.isShowing()) {
			super.onBackPressed();
		} else {
			JNVPlayerUtil.JNV_UnInit();
			Process.killProcess(Process.myPid());
		}
	}

	/* 登陆按钮 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login:
			String userName = mUserName.getText().toString();
			String password = mPassword.getText().toString();
			String ip = mIP.getText().toString();
			String port=mPort.getText().toString();
			LoginInfo info=new LoginInfo(userName, port, password, ip);
			if(MyUtils.hasUselessString(userName,password,ip,port)){
				MyUtils.toast(this, "请填写完整信息！");
				break;
			}
			if (cb_remenber.isChecked()) {
				SPUtils.saveLoginInfo(this, info);
			} else {
				/* dbHelper.insertOrUpdate(userName, "", 0); */

			}
			login(userName,password,ip,port);
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		switch (arg0.getId()) {
		case R.id.remember:
			SPUtils.saveBoolean(this, SPUtils.KEY_REMEMBER_USERINFO, arg1);
			break;
		case R.id.cb_auto_login:
			SPUtils.saveBoolean(this, SPUtils.KEY_AUTO_LOGIN, arg1);
			break;

		default:
			break;
		}

	}

	/**
	 * 进行版本的比较，提示是否更新当前的应用
	 */
	private void checkToUpdate() {
		Log.i(".....................", "checkToUpdate()" + "....");
		if (getServerVersion()) {

			try {
				currentCode = CurrentVersionInfo.getVerCode(this);
				if (newVerCode > currentCode) {
					// 弹出更新提示对话框
					showUpdateDialog();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * 显示更新提示框
	 */
	private void showUpdateDialog() throws Exception {
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
		dialog.setTitle("软件更新").setMessage(sb.toString())
				.setPositiveButton("更新", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						showProgressBar();// 更新当前版本
					}
				}).setNegativeButton("暂时不更新", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		dialog.create().show();
	}

	/**
	 * 下载最新版本进度条
	 */
	private void showProgressBar() {
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
					if (is == null) {
						throw new RuntimeException("inputStream is null");
					}
					File file = new File(Environment.getExternalStorageDirectory() + "/", newAppName);
					Log.i("==============", "file : " + file.getAbsolutePath().toString());
					fileOutputStream = new FileOutputStream(file);
					/*
					 * fileOutputStream =
					 * LoginActivity.this.openFileOutput(file.toString(),
					 * Context.MODE_WORLD_READABLE);
					 */
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
					e.printStackTrace();
				} catch (IOException e) {
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
				pbar.cancel();
				AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
				dialog.setCancelable(false);
				dialog.setTitle("下载完成").setMessage("是否安装新的应用")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						installNewApk();

						finish();
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
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
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/", newAppName)),
				"application/vnd.android.package-archive");
		startActivity(intent);
	}

	/**
	 * 将从服务器version.json获得的字符串解析出我们需要的版本信息
	 */
	private boolean getServerVersion() {
		try {
			Log.i("--------------", "ip:" + Constants.SERVER_IP);
			String newVerJSON = GetUpdateJsonInfo.getUpdateVerJSON(Constants.SERVER_IP + "version_jilian.json");
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
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * 登录
	 */
	public void login(String u,String p,String ip,String portStr) {
		if (!MyUtil.isConnect(this)) {
			Log.e(TAG, "网络没有连接");
			Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
			return;
		}
		if (isValidity()) {
			if ("".equals(u)) {
				return;
			} else if ("".equals(p)) {
				return;
			} else if ("".equals(ip)) {
				return;
			} else if ("".equals(portStr)) {
				return;
			}
			try {
				// 解析域名的IP地址
				iAdd = InetAddress.getByName(ip);
				// 得到字符串形式的IP地址
				ip = iAdd.getHostAddress();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (Exception e) {
				ip = "";
			} finally {

			}
			Pattern pattern = Pattern.compile(
					"^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$");
			Matcher matcher = pattern.matcher(ip);
			if (!matcher.matches()) {
				MyUtil.commonToast(this, R.string.ipFailed);
				return;
			}
			int port = Integer.parseInt(portStr.equals("") ? "0" : portStr);
			//登录
			JNVPlayerUtil.JNV_N_Login(ip, port, u, p, 30, loginControl, "callbackLonginEvent", 0);
		} else {
			AlertDialog.Builder builder = new Builder(this);
			builder.setMessage(R.string.lose_efficacy);
			builder.setTitle(R.string.prompt);
			builder.setPositiveButton(R.string.confirm, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					LoginActivity.this.finish();
				}
			});
			builder.create().show();

		}
	}

	/**
	 * 验证有效性
	 */
	private boolean isValidity() {
		if (Constants.IS_TEST_VERSION) {
			long cur = System.currentTimeMillis();
			Date date = MyUtil.stringToDate(Constants.EFFECTIVE_DATE, Constants.DATE_FORMAT);
			long flag = date.getTime();
			if (cur > flag) {
				return false;
			}
		}
		return true;
	}

}
