package com.monitor.bus.activity.fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.monitor.bus.Constants;
import com.monitor.bus.utils.LogUtils;
import com.monitor.bus.utils.MUtils;
import com.jniUtil.JNVPlayerUtil;
import com.monitor.bus.activity.R;
import com.monitor.bus.activity.VideoListActivity;
import com.monitor.bus.bean.DeviceInfo;
import com.monitor.bus.bean.RecordInfo;
import com.monitor.bus.bean.manager.DeviceManager;
import com.monitor.bus.view.dialog.DateUtil;
import com.monitor.bus.view.dialog.MyDataPickerDialog;
import com.monitor.bus.view.dialog.MyDatePickerDialog;
import com.monitor.bus.view.dialog.MyTimePickerDialog;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RePlayFragment extends BaseFragment implements View.OnClickListener {
	public static final String TAG = RePlayFragment.class.getSimpleName();
	View contentView;
	TextView tv_select_device, tv_file_local, tv_channel, tv_type, tv_select_time, tv_start_time, tv_end_time;
	RelativeLayout rl_1, rl_2, rl_3, rl_4, rl_5, rl_6, rl_7;
	Button bt_find;
	private Dialog dateDialog, endTimeDialog,startTimeDialog, chooseDeviceDialog,chooseLocalDialog,chooseChannelDialog,chooseTypeDialog;

	private List<DeviceInfo> deviceList;
	private RecordInfo recodInfo = new RecordInfo();
	private DeviceManager deviceManger;
	String dateString;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = inflater.inflate(R.layout.fragment_replay, container, false);
		setTitle();
		initView();
		initData();
		return contentView;
	}

	private void setTitle() {
		TextView title = (TextView) contentView.findViewById(R.id.tilte_name);
		title.setText(getContext().getString(R.string.find_record));
	}

	void initView() {
		tv_channel = (TextView) contentView.findViewById(R.id.tv_channel);
		tv_file_local = (TextView) contentView.findViewById(R.id.tv_find_local);
		tv_type = (TextView) contentView.findViewById(R.id.tv_type);
		tv_select_device = (TextView) contentView.findViewById(R.id.tv_select_device);
		tv_select_time = (TextView) contentView.findViewById(R.id.tv_select_time);
		tv_start_time = (TextView) contentView.findViewById(R.id.tv_start_time);
		tv_end_time = (TextView) contentView.findViewById(R.id.tv_end_time);

		rl_1 = (RelativeLayout) contentView.findViewById(R.id.rl_1);
		rl_2 = (RelativeLayout) contentView.findViewById(R.id.rl_2);
		rl_3 = (RelativeLayout) contentView.findViewById(R.id.rl_3);
		rl_4 = (RelativeLayout) contentView.findViewById(R.id.rl_4);
		rl_5 = (RelativeLayout) contentView.findViewById(R.id.rl_5);
		rl_6 = (RelativeLayout) contentView.findViewById(R.id.rl_6);
		rl_7 = (RelativeLayout) contentView.findViewById(R.id.rl_7);
		bt_find = (Button) contentView.findViewById(R.id.bt_find);
		bt_find.setOnClickListener(this);
		rl_1.setOnClickListener(this);
		rl_2.setOnClickListener(this);
		rl_3.setOnClickListener(this);
		rl_4.setOnClickListener(this);
		rl_5.setOnClickListener(this);
		rl_6.setOnClickListener(this);
		rl_7.setOnClickListener(this);

	}

	private void initData() {
		deviceManger = DeviceManager.getInstance();
		deviceList = deviceManger.getDeviceList();
		if(deviceList.size()>0){
			recodInfo.setDeviceId(deviceList.get(0).getDeviceId());
		}
		recodInfo.setStartTime(DateUtil.getTodayStart());
		recodInfo.setEndTime(DateUtil.getTodayEnd());
		
		tv_start_time.setText("00:00");
		tv_end_time.setText("23:59");
		tv_type.setText(getContext().getString(R.string.nomeravideo));
		tv_file_local.setText(getContext().getString(R.string.localvideo));
		tv_select_time.setText(DateUtil.getTodayDateString(DateUtil.REPLAY_SHOW_FORMAT));

		if(deviceList.size()>0){
			tv_select_device.setText(deviceList.get(0).getDeviceName());
			tv_channel.setText(""+deviceList.get(0).getCurrentChn());
		}
	}

	/**
	 * 录像查询
	 * 
	 * @param deviceId
	 *            设备GUID
	 * @param iCenter
	 *            中心或者设备录像，1，中心录像；0，设备录像
	 * @param iType
	 *            录像类型，1,普通录像；2,报警录像
	 * @param startTime
	 *            开始时间
	 * @param endTime
	 *            结束时间
	 * @param iChnFlag
	 *            通道号
	 * @param filePath
	 *            设备端录像文件路径
	 * @return
	 */
	private void queryRecord() {
		LogUtils.i(TAG, "query() RecordInfo:"+recodInfo.toString()); 
		if (!isUsefullTime(recodInfo.getStartTime(), recodInfo.getEndTime())) {
			MUtils.toast(getContext(), getContext().getString(R.string.time_validate));
			return;
		}
		if (!recodInfo.isLocalVideo()) {
			//远程视频
			JNVPlayerUtil.JNV_N_RecQuery(recodInfo.getDeviceId(), 0, recodInfo.getRecType(),dateString+" "+ recodInfo.getStartTime(),
					dateString+" "+recodInfo.getEndTime(), recodInfo.getChanneId(), Constants.DEVRECORD_PASTH);
		}
		Intent intent = new Intent(getContext(), VideoListActivity.class);
		intent.putExtra(VideoListActivity.EXTRA_RECODINFO, recodInfo);
		getContext().startActivity(intent);

	}

	/**
	 * 校验输入的时间是否合法
	 * 
	 * @param time
	 * @throws ParseException
	 */
	private boolean isUsefullTime(String start_time, String end_time) {
		try {
			SimpleDateFormat df = new SimpleDateFormat(DateUtil.DB_FORMAT);
			Date start_date = df.parse(start_time);
			Date end_date = df.parse(end_time);
			if (start_date.getTime() <= end_date.getTime()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * chooseDialog
	 */
	private void showDeviceDialog(List<String> mlist) {
		if(chooseDeviceDialog==null){
			
			MyDataPickerDialog.Builder builder = new MyDataPickerDialog.Builder(getContext());
			chooseDeviceDialog = builder.setData(mlist).setSelection(1).setTitle(getContext().getString(R.string.cancel))
					.setOnDataSelectedListener(new MyDataPickerDialog.OnDataSelectedListener() {
						@Override
						public void onDataSelected(String itemValue, int position) {
							Log.i(TAG, "selectDevice:" + itemValue + "  position:" + position);
							tv_select_device.setText(itemValue);
							recodInfo.setDeviceId("" + deviceList.get(position).getDeviceId());
						}
						
						@Override
						public void onCancel() {
							
						}
					}).create();
		}

		chooseDeviceDialog.show();
	}
	
	/**
	 * chooseDialog
	 */
	private void showLocalTypeDialog(List<String> mlist) {
		if(chooseLocalDialog==null){
			
			MyDataPickerDialog.Builder builder = new MyDataPickerDialog.Builder(getContext());
			chooseLocalDialog = builder.setData(mlist).setSelection(1).setTitle(getContext().getString(R.string.cancel))
					.setOnDataSelectedListener(new MyDataPickerDialog.OnDataSelectedListener() {
						@Override
						public void onDataSelected(String itemValue, int position) {
							Log.i(TAG, "selectisLocal:" + itemValue + "  position:" + position);
							tv_file_local.setText(itemValue);
							recodInfo.setRecType(position + 1);
						}
						
						@Override
						public void onCancel() {
							
						}
					}).create();
		}
	
		chooseLocalDialog.show();
	}

	private void showChannelDialog(List<String> mlist) {
		if(chooseChannelDialog==null){
			
			MyDataPickerDialog.Builder builder = new MyDataPickerDialog.Builder(getContext());
			chooseChannelDialog = builder.setData(mlist).setSelection(1).setTitle(getContext().getString(R.string.cancel))
					.setOnDataSelectedListener(new MyDataPickerDialog.OnDataSelectedListener() {
						@Override
						public void onDataSelected(String itemValue, int position) {
							Log.i(TAG, "selectchannel:" + itemValue + "  position:" + position);
							tv_channel.setText(itemValue);
							recodInfo.setChanneId(position+1);
						}
						
						@Override
						public void onCancel() {
							
						}
					}).create();
		}

		chooseChannelDialog.show();
	}

	/**
	 * chooseDialog
	 */
	private void showVideoTypeDialog(List<String> mlist) {
		if(chooseTypeDialog==null){
			MyDataPickerDialog.Builder builder = new MyDataPickerDialog.Builder(getContext());
			chooseTypeDialog = builder.setData(mlist).setSelection(1).setTitle(getContext().getString(R.string.cancel))
					.setOnDataSelectedListener(new MyDataPickerDialog.OnDataSelectedListener() {
						@Override
						public void onDataSelected(String itemValue, int position) {
							Log.i(TAG, "selectteype:" + itemValue + "  position:" + position);
							tv_type.setText(itemValue);
							recodInfo.setRecType(position + 1);
						}
						
						@Override
						public void onCancel() {
							
						}
					}).create();
		}

		chooseTypeDialog.show();
	}

	private void showDateDialog(List<Integer> date) {
		if(dateDialog==null){
			
			MyDatePickerDialog.Builder builder = new MyDatePickerDialog.Builder(getContext());
			builder.setOnDateSelectedListener(new MyDatePickerDialog.OnDateSelectedListener() {
				@Override
				public void onDateSelected(int[] dates) {
					dateString = dates[0] + "-" + (dates[1] > 9 ? dates[1] : ("0" + dates[1])) + "-"
							+ (dates[2] > 9 ? dates[2] : ("0" + dates[2]));
					tv_select_time.setText(dateString);
					Log.i(TAG, "selectdate:" +dateString);
				}
				
				@Override
				public void onCancel() {
					
				}
			}).setSelectYear(date.get(0)).setSelectMonth(date.get(1)).setSelectDay(date.get(2));
			
			builder.setMaxYear(DateUtil.getYear());
			builder.setMaxMonth(DateUtil.getDateForString(DateUtil.getToday()).get(1));
			builder.setMaxDay(DateUtil.getDateForString(DateUtil.getToday()).get(2));
			dateDialog = builder.create();
		}
		dateDialog.show();
	}

	private void showStartTimePick() {

		if (startTimeDialog == null) {
			MyTimePickerDialog.Builder builder = new MyTimePickerDialog.Builder(getContext());
			startTimeDialog = builder.setOnTimeSelectedListener(new MyTimePickerDialog.OnTimeSelectedListener() {
				@Override
				public void onTimeSelected(int[] times) {
					String startDate = (times[0]>9? times[0]: ("0" + times[0]))+":" + (times[1]>9? times[1]: ("0" + times[1]));
					tv_start_time.setText(startDate);
					recodInfo.setStartTime(startDate);
					Log.i(TAG, "selectdate:" +startDate);
				}
			}).create();
		}

		startTimeDialog.show();

	}

	private void showStopTimePick() {

		if (endTimeDialog == null) {

			MyTimePickerDialog.Builder builder = new MyTimePickerDialog.Builder(getContext());
			endTimeDialog = builder.setOnTimeSelectedListener(new MyTimePickerDialog.OnTimeSelectedListener() {
				@Override
				public void onTimeSelected(int[] times) {
					String endDate = times[0] + ":" + times[1];
					tv_end_time.setText(times[0] + ":" + times[1]);
					recodInfo.setEndTime(endDate);
					Log.i(TAG, "selectdate:" +endDate);
				}
			}).create();
		}

		endTimeDialog.show();

	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.rl_1:// 设备
			deviceList = deviceManger.getDeviceList();
			List<String> deviceNames = new ArrayList<String>();
			for (DeviceInfo info : deviceList) {
				if (info.getDeviceName() == null || info.getDeviceName().isEmpty()) {
					LogUtils.d(TAG, "deviceName："+info.toString());
					LogUtils.getInstance().localLog(TAG, "deviceName is NULL!!!");
				}
				deviceNames.add(info.getDeviceName());
			}
			showDeviceDialog(deviceNames);
			break;
		case R.id.rl_2:// 文件位置
			List<String> file = new ArrayList<String>();
			file.add(getContext().getString(R.string.localvideo));
			file.add(getContext().getString(R.string.remontvideo));
			showLocalTypeDialog(file);
			break;
		case R.id.rl_3:// 通道
			List<String> channel = new ArrayList<String>();
			for(int i=1; i<6;i++){
				channel.add(getContext().getString(R.string.channel)+i);
			}
			showChannelDialog(channel);
			break;
		case R.id.rl_4:// 录像类型
			List<String> test = new ArrayList<String>();
			test.add(getContext().getString(R.string.nomeravideo));
			test.add(getContext().getString(R.string.alarmvideo));
			showVideoTypeDialog(test);
			break;
		case R.id.rl_5:// 日期
			List<Integer> time = DateUtil.getCurrentTimeList();
			showDateDialog(time);
			break;
		case R.id.rl_6:// 开始时间
			showStartTimePick();
			break;
		case R.id.rl_7:// 结束时间
			showStopTimePick();
			break;
		case R.id.bt_find:
			queryRecord();
			break;

		default:
			break;
		}

	}
}
