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
import com.monitor.bus.service.CurrentVersionInfo;
import com.monitor.bus.service.GetUpdateJsonInfo;
import com.monitor.bus.view.MyEditText;

/**
 * 用户登陆
 * 
 */
public class LoginActivity extends Activity implements android.view.View.OnClickListener {
	private static String TAG = "LoginActivity";

	private MyEditText userName;// 用户名
	private MyEditText password;// 密码
	private MyEditText login_port;// 端口
	private MyEditText login_address;// 地址
	private ProgressDialog pbar;// 进度条对话框
	private EditText mUserName;
	private EditText mPassword;
	private EditText mIP;
	private EditText mPort;
	private ImageButton mDropDown;
	private Button btn_login;
	private CheckBox mCheckBox;
	private PopupWindow popView;
	private MyAdapter dropDownAdapter;
	
	private String newVerName, newAppName;// 新版本名称,新应用程序名称
	private int newVerCode;// 新版本号
	private int currentCode = 0;//旧版本号
	private InetAddress iAdd;
	
	
	
	private Handler handler = new Handler();
	private DBHelper dbHelper;
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
		Log.i(".....................", "login");
		setContentView(R.layout.login);
		try {
			Log.i(".....................", "目前版本:"+CurrentVersionInfo.getVerCode(this));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//checkToUpdate();
		loginControl = new LoginEventControl(this);
		JNVPlayerUtil.JNV_Init(Constants.SCREEN_COUNT);// 初始化so

		userName = (MyEditText) findViewById(R.id.userName);
		password = (MyEditText) findViewById(R.id.login_password);
		login_port = (MyEditText) findViewById(R.id.login_port);
		login_address = (MyEditText) findViewById(R.id.login_address);
		/**
		 * 输入框提示 userName.setEditHint(R.string.init_user);
		 * password.setEditHint(R.string.init_psw);
		 * login_address.setEditHint(R.string.init_ip);
		 * login_port.setEditHint(R.string.init_port);
		 */
		initLoginView();
		initWidget();
	}
	/*初始化控件与数据库*/
	private void initWidget() {
		// TODO Auto-generated method stub
		dbHelper = new DBHelper(this);
		mDropDown = (ImageButton) findViewById(R.id.dropdown_button);
		mCheckBox = (CheckBox) findViewById(R.id.remember);
		mUserName =(EditText) userName.findViewById(R.id.edit_text_input);
		mPassword = (EditText) password.findViewById(R.id.edit_text_input);
		mPassword.setText("");
		mIP = (EditText) login_address.findViewById(R.id.edit_text_input);
		mPort = (EditText) login_port.findViewById(R.id.edit_text_input);
		mUserName.setText("123");
		mPassword.setText("123");
		mIP.setText("183.61.171.28");
		mPort.setText("6008");
		
		btn_login = (Button) findViewById(R.id.login);
		mDropDown.setOnClickListener(this);
		btn_login.setOnClickListener(this);
		initLoginUserName();
	}
	/*初始化登录的用户名信息*/
	private void initLoginUserName() {
		// TODO Auto-generated method stub
		String[] usernames = dbHelper.queryAllUserName();
		if (usernames.length > 0) {
			String tempName = usernames[usernames.length - 1];
			mUserName.setText(tempName);
			mUserName.setSelection(tempName.length());
			//查询用户信息
			ContentValues userinfo = dbHelper.queryUserInfoByName(tempName);
			String tempIp = userinfo.getAsString(User.IPADDRESS);
		    String tempPort = userinfo.getAsString(User.PORT);	
			String tempPassword = userinfo.getAsString(User.PASSWORD);
			//int tempIsDomain = userinfo.getAsInteger(User.ISDOMAIN).intValue();
			int tempIsSave = userinfo.getAsInteger(User.ISSAVED).intValue();
			if (tempIsSave == 0) {
				mCheckBox.setChecked(false);
			} else if (tempIsSave == 1) {
				mCheckBox.setChecked(true);
			}
			mPassword.setText(tempPassword);
			mIP.setText(tempIp);
			mPort.setText(tempPort);
		}
		mUserName.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mPassword.setText("");
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
	}
	/*登陆按钮*/
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		/*case R.id.remember:
			
			break;*/
		case R.id.dropdown_button:
			if (popView != null) {
				if (!popView.isShowing()) {
					popView.showAsDropDown(mUserName);
				} else {
					popView.dismiss();
				}
			} else {
				// 如果有已经登录过账号
				if (dbHelper.queryAllUserName().length > 0) {
					initPopView(dbHelper.queryAllUserName());
					if (!popView.isShowing()) {
						popView.showAsDropDown(mUserName);
					} else {
						popView.dismiss();
					}
				} else {
					Log.i("++++++++++", "无记录");
				}

			}
			break;
		case R.id.login:
			String userName = mUserName.getText().toString();
			String password = mPassword.getText().toString();
			String ip = mIP.getText().toString();
			int nPort = Integer.parseInt(mPort.getText().toString());
			if (mCheckBox.isChecked()) {
				dbHelper.insertOrUpdate(userName, password, ip,nPort,0, 1);
			} else {
				/*dbHelper.insertOrUpdate(userName, "", 0);*/
				
			}
			Log.i("++++++++++", "记录已经保存");
			loginClient(v);
			break;
		}
	}
	/*初始化下拉按钮，多账号记住*/
	private void initPopView(String[] usernames) {
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < usernames.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("name", usernames[i]);
			map.put("drawable", R.drawable.xicon);
			list.add(map);
		}
		dropDownAdapter = new MyAdapter(this, list, R.layout.dropdown_item,
				new String[] { "name", "drawable" }, new int[] { R.id.textview,
						R.id.delete });
		ListView listView = new ListView(this);
		listView.setAdapter(dropDownAdapter);

		popView = new PopupWindow(listView, mUserName.getWidth(),
				ViewGroup.LayoutParams.WRAP_CONTENT, true);
		popView.setFocusable(true);
		popView.setOutsideTouchable(true);
		popView.setBackgroundDrawable(getResources().getDrawable(R.drawable.white));
		//popView.showAsDropDown(mUserName);
	}

	class MyAdapter extends SimpleAdapter {

		private List<HashMap<String, Object>> data;

		public MyAdapter(Context context, List<HashMap<String, Object>> data,
				int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);
			this.data = data;
		}

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			System.out.println(position);
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(LoginActivity.this).inflate(
						R.layout.dropdown_item, null);
				holder.btn = (ImageButton) convertView
						.findViewById(R.id.delete);
				holder.tv = (TextView) convertView.findViewById(R.id.textview);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv.setText(data.get(position).get("name").toString());
			holder.tv.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					String[] usernames = dbHelper.queryAllUserName();
					mUserName.setText(usernames[position]);
					//查询用户信息,填充内容
					ContentValues userinfo = dbHelper.queryUserInfoByName(usernames[position]);
					mIP.setText(userinfo.getAsString(User.IPADDRESS));
					mPort.setText(userinfo.getAsString(User.PORT));
					mPassword.setText(userinfo.getAsString(User.PASSWORD));		
					popView.dismiss();
				}
			});
			holder.btn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					String[] usernames = dbHelper.queryAllUserName();
					if (usernames.length > 0) {
						dbHelper.delete(usernames[position]);
					}
					String[] newusernames = dbHelper.queryAllUserName();
					if (newusernames.length > 0) {
						initPopView(newusernames);
						popView.showAsDropDown(mUserName);
					} else {
						popView.dismiss();
						popView = null;
					}
				}
			});
			return convertView;
		}
	}

	class ViewHolder {
		private TextView tv;
		private ImageButton btn;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		dbHelper.cleanup();
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
						LoginActivity.this);
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
					.getUpdateVerJSON(Constants.SERVER_IP + "version_jilian.json");
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
	 * 登录
	 * */
	public void loginClient(View view) {
		if (!MyUtil.isConnect(this)) {
			Log.e(TAG, "网络没有连接");
			Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		if (isValidity()) {

			String u = userName.getEditInputValue();
			String p = password.getEditInputValue();
			String ip = login_address.getEditInputValue();
			String portStr = login_port.getEditInputValue();
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
				//解析域名的IP地址
				iAdd = InetAddress.getByName(ip);
				//得到字符串形式的IP地址
				ip = iAdd.getHostAddress();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch(Exception e){
				ip = "";
			}finally{
				
			}
			Pattern pattern = Pattern
					.compile("^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$");
			Matcher matcher = pattern.matcher(ip);
			if (!matcher.matches()) {
				MyUtil.commonToast(this, R.string.ipFailed);
				return;
			}
			int port = Integer.parseInt(portStr.equals("") ? "0" : portStr);

			SharedPreferences userInfo = getSharedPreferences("user_info", 0);
			userInfo.edit().putString("userName", u).commit();
			userInfo.edit().putString("password", p).commit();
			userInfo.edit().putString("login_address", ip).commit();
			userInfo.edit().putString("login_port", port + "").commit();

			// ip="192.168.4.160";
			// port=5700;
			// u="aa";
			// p="aa";
			// String userId = dbHelper.checkLoginUser(u,"p");
			
			JNVPlayerUtil.JNV_N_Login(ip, port, u, p, 30, loginControl,
					"callbackLonginEvent", 0);
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
			Date date = MyUtil.stringToDate(Constants.EFFECTIVE_DATE,
					Constants.DATE_FORMAT);
			long flag = date.getTime();
			if (cur > flag) {
				return false;
			}
		}
		return true;
	}

	public void initLoginView() {
		SharedPreferences userInfo = getSharedPreferences("user_info", 0);
		String u = userInfo.getString("userName", "");
		String p = userInfo.getString("password", "");
		String ip = userInfo.getString("login_address", "");
		String port = userInfo.getString("login_port", "");

		password.setTextViewText(R.string.password, p);
		login_address.setTextViewText(R.string.login_address, ip);
		login_port.setTextViewText(R.string.login_port, port + "");
		password.setEditPasswordType();
		login_port.setEditNumberType();
		login_address.setIpConfigType();
		userName.setTextViewText(R.string.userName, u);
		userName.setEditFocus();
	}

	/**
	 * 退出
	 * */
	public void exitClient(View view) {
		Log.i(TAG, "+++++++++退出");
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e(TAG, "-------------登陆Activity销毁!");
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
}
