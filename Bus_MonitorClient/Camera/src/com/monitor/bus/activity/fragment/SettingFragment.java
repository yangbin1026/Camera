package com.monitor.bus.activity.fragment;

import java.util.ArrayList;
import java.util.List;

import com.monitor.bus.Constants;
import com.monitor.bus.activity.LoginActivity;
import com.monitor.bus.activity.R;
import com.monitor.bus.bean.LoginInfo;
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

public class SettingFragment extends BaseFragment implements View.OnClickListener, OnCheckedChangeListener {
	private static final String TAG = "SettingActivity";
	View view;
	Button bt_change, bt_exit, bt_saveurl;
	Button bt_logout;
	SwitchButton sb_autologin, sb_local, sb_gps;
	TextView tv_version, tv_showmode, tv_username, tv_map;
	RelativeLayout rl_mode, rl_map;
	Dialog modeDialog,mapDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_setting, container, false);
		setTitle();
		initView();
		initData();
		return view;
	}

	private void setTitle() {
		TextView title = (TextView) view.findViewById(R.id.tilte_name);
		title.setText(getContext().getString(R.string.setting));
	}

	private void initView() {
		bt_logout = (Button) view.findViewById(R.id.bt_logout);
		bt_logout.setOnClickListener(this);
		tv_version = (TextView) view.findViewById(R.id.tv_version);
		tv_username = (TextView) view.findViewById(R.id.tv_username);
		tv_map = (TextView) view.findViewById(R.id.tv_map);
		tv_showmode = (TextView) view.findViewById(R.id.tv_showmode);
		rl_mode = (RelativeLayout) view.findViewById(R.id.rl_mode);
		rl_map = (RelativeLayout) view.findViewById(R.id.rl_map);

		sb_autologin = (SwitchButton) view.findViewById(R.id.sb_autologin);
		sb_gps = (SwitchButton) view.findViewById(R.id.sb_gps);
		sb_local = (SwitchButton) view.findViewById(R.id.sb_localpaser);
		sb_autologin.setOnCheckedChangeListener(this);
		sb_gps.setOnCheckedChangeListener(this);
		sb_local.setOnCheckedChangeListener(this);
		rl_mode.setOnClickListener(this);
		rl_map.setOnClickListener(this);
	}

	private void initData() {
		try {
			String version = MUtils.getVerName(getContext());
			tv_version.setText(version);
		} catch (Exception e) {
			e.printStackTrace();
		}

		LoginInfo loginInfo = SPUtils.getLoginInfo(getContext());
		tv_username.setText(loginInfo.getUserName());

		boolean autoLogin = SPUtils.getBoolean(getContext(), SPUtils.KEY_AUTO_LOGIN, false);
		boolean gps = SPUtils.getBoolean(getContext(), SPUtils.KEY_GSP_CHECK, false);
		boolean local = SPUtils.getBoolean(getContext(), SPUtils.KEY_LOCAL, false);
		sb_autologin.setChecked(autoLogin);
		sb_gps.setChecked(gps);
		sb_local.setChecked(local);
		int mode = SPUtils.getInt(getContext(), SPUtils.KEY_REMEMBER_SHOWMODE, 2);
		switch (mode) {
		case 0:
			tv_showmode.setText(getContext().getString(R.string.show_video));
			break;
		case 1:
			tv_showmode.setText(getContext().getString(R.string.show_map));
			break;
		case 2:
			tv_showmode.setText(getContext().getString(R.string.show_mapvideo));
			break;

		default:
			break;
		}

	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.bt_logout:
			// 登出
			SPUtils.saveBoolean(getContext(), SPUtils.KEY_AUTO_LOGIN, false);
			Intent intent = new Intent(getContext(), LoginActivity.class);
			getContext().startActivity(intent);
			getActivity().finish();
			break;
		case R.id.rl_mode:
			List<String> modes = new ArrayList<String>();
			String[] modeStrings = getResources().getStringArray(R.array.list_setting_showmode);
			for (int i = 0; i < modeStrings.length; i++) {
				modes.add(modeStrings[i]);
			}
			showModeDialog(modes);
			break;
		case R.id.rl_map:
			List<String> maps = new ArrayList<String>();
			String[] mapstring = getResources().getStringArray(R.array.maps);
			for (int i = 0; i < mapstring.length; i++) {
				maps.add(mapstring[i]);
			}
			showMapsDialog(maps);
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
	private void showModeDialog(List<String> mlist) {
		if (modeDialog == null) {

			MyDataPickerDialog.Builder builder = new MyDataPickerDialog.Builder(getContext());
			modeDialog = builder.setData(mlist).setSelection(1).setTitle(getContext().getString(R.string.cancel))
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

	/**
	 * chooseDialog
	 */
	private void showMapsDialog(List<String> mlist) {
		if (mapDialog == null) {
			MyDataPickerDialog.Builder builder = new MyDataPickerDialog.Builder(getContext());
			mapDialog = builder.setData(mlist).setSelection(1).setTitle(getContext().getString(R.string.cancel))
					.setOnDataSelectedListener(new MyDataPickerDialog.OnDataSelectedListener() {
						@Override
						public void onDataSelected(String itemValue, int position) {
							tv_map.setText(itemValue);
							SPUtils.saveBoolean(getContext(), SPUtils.KEY_REMEMBER_ISGOOGLEMAP, position == 1);
						}

						@Override
						public void onCancel() {

						}
					}).create();
		}
		mapDialog.show();
	}

}
