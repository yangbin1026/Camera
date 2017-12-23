package com.monitor.bus.activity.fragment;

import java.util.ArrayList;
import java.util.List;

import com.monitor.bus.activity.LoginActivity;
import com.monitor.bus.activity.R;
import com.monitor.bus.bean.LoginInfo;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.utils.MUtils;
import com.monitor.bus.utils.SPUtils;
import com.monitor.bus.view.SwitchButton;
import com.monitor.bus.view.dialog.MyDataPickerDialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRouter.UserRouteInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SettingFragment extends BaseFragment implements View.OnClickListener,OnCheckedChangeListener{
	private static final String TAG = "SettingActivity";
	View view;
	Button bt_change, bt_exit, bt_saveurl;
	Button bt_logout;
	SwitchButton sb_autologin,sb_local,sb_gps;
	TextView tv_version,tv_showmode,tv_username;
	RelativeLayout rl_mode;

	Dialog modeDialog;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_setting, container, false);
		setTitle();
		initView();
		initData();
		return view;
	}

	private void setTitle() {
		TextView title= (TextView) view.findViewById(R.id.tilte_name);
		title.setText(getContext().getString(R.string.setting));
	}
	private void initData() {
		try {
			String version=MUtils.getVerName(getContext());
			tv_version.setText(version);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LoginInfo loginInfo=SPUtils.getLoginInfo(getContext());
		tv_username.setText(loginInfo.getUserName());
		
		boolean autoLogin=SPUtils.getBoolean(getContext(), SPUtils.KEY_AUTO_LOGIN, false);
		boolean gps=SPUtils.getBoolean(getContext(), SPUtils.KEY_GSP_CHECK, false);
		boolean local=SPUtils.getBoolean(getContext(), SPUtils.KEY_LOCAL, false);
		LoginInfo info= SPUtils.getLoginInfo(getContext());
		sb_autologin.setChecked(autoLogin);
		sb_gps.setChecked(gps);
		sb_local.setChecked(local);
		tv_username.setText(info.getUserName());
	}

	private void initView() {
		bt_logout=(Button) view.findViewById(R.id.bt_logout);
		bt_logout.setOnClickListener(this);
		tv_version=(TextView) view.findViewById(R.id.tv_version);
		tv_username=(TextView) view.findViewById(R.id.tv_username);
		tv_showmode=(TextView) view.findViewById(R.id.tv_showmode);
		rl_mode=(RelativeLayout) view.findViewById(R.id.rl_mode);
		
		sb_autologin=(SwitchButton) view.findViewById(R.id.sb_autologin);
		sb_gps=(SwitchButton) view.findViewById(R.id.sb_gps);
		sb_local=(SwitchButton) view.findViewById(R.id.sb_localpaser);
		sb_autologin.setOnCheckedChangeListener(this);
		sb_gps.setOnCheckedChangeListener(this);
		sb_local.setOnCheckedChangeListener(this);
		rl_mode.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.bt_logout:
			//登出
			SPUtils.saveBoolean(getContext(), SPUtils.KEY_AUTO_LOGIN, false);
			Intent intent=new Intent(getContext(),LoginActivity.class);
			getContext().startActivity(intent);
			getActivity().finish();
			break;
		case R.id.rl_mode:
			List<String> list=new ArrayList<String>();
			list.add("显示视频");
			list.add("显示地图");
			list.add("显示视频和地图");
			showModeDialog(list);
			break;
		default:
			break;
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		switch (arg0.getId()) {
		case R.id.sb_autologin:
			SPUtils.saveBoolean(getContext(), SPUtils.KEY_AUTO_LOGIN, arg1);
			break;
		case R.id.sb_gps:
			SPUtils.saveBoolean(getContext(), SPUtils.KEY_GSP_CHECK, arg1);
			break;
		case R.id.sb_localpaser:
			SPUtils.saveBoolean(getContext(), SPUtils.KEY_LOCAL, arg1);
			break;

		default:
			break;
		}
	}
	
	/**
	 * chooseDialog
	 */
	/**
	 * chooseDialog
	 */
	private void showModeDialog(List<String> mlist) {
		if(modeDialog==null){
			
			MyDataPickerDialog.Builder builder = new MyDataPickerDialog.Builder(getContext());
			modeDialog = builder.setData(mlist).setSelection(1).setTitle("取消")
					.setOnDataSelectedListener(new MyDataPickerDialog.OnDataSelectedListener() {
						@Override
						public void onDataSelected(String itemValue, int position) {
							tv_showmode.setText(itemValue);
							SPUtils.saveInt(getContext(), SPUtils.KEY_REMEMBER_SHOWMODE, position);
						}
						
						@Override
						public void onCancel() {
							
						}
					}).create();
		}
	
		modeDialog.show();
	}

}
