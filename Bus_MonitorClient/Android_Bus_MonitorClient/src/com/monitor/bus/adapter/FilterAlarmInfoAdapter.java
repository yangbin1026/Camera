package com.monitor.bus.adapter;

import java.util.HashMap;
import java.util.Map;

import com.monitor.bus.activity.R;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.control.LoginEventControl;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class FilterAlarmInfoAdapter extends BaseAdapter {
	public static final int[] str_alarm_info = { 
			R.string.overspeed,R.string.overspeed_cancel_alarm,R.string.input_trigger,
			R.string.motion_detection,R.string.video_lose,R.string.low_speed_alarm,
			R.string.low_speed_cancel_alarm,
			R.string.violation_parking_alarm,
			R.string.violation_parking_cancel_alarm,
			R.string.insufficient_GPS_disk_space, R.string.gps_save_ceased,
			R.string.overspeed_freeway,
			R.string.overspeed_freeway_cancel_alarm,
			R.string.lowspeed_freeway_alarm,
			R.string.lowspeed_freeway_cancel_alarm,
			R.string.overspeed_national_road,
			R.string.overspeed_nation_road_alarm_canceled,
			R.string.lowspeed_national_road,
			R.string.lowspeed_nation_road_alarm_canceled,
			R.string.gps_module_abnormal, R.string.gsensor_alarm,
			R.string.storage_medium_one_abnormity,
			R.string.storage_medium_two_abnormity, R.string.system_abnormity,
			R.string.device_online, R.string.device_offline,
			R.string.enter_into_wifi_region, R.string.leave_wifi_region,
			R.string.ignition, R.string.flameout, R.string.front_door_opens,
			R.string.front_door_closed, R.string.middle_door_opens,
			R.string.middle_door_closed, R.string.postern_opens,
			R.string.postern_closed, R.string.arrive_at_station_nostop,
			R.string.drive_with_door_open, R.string.arrive_at_station_noopen,
			R.string.high_temperature_on_carriage,
			R.string.low_temperature_on_carriage,
			R.string.alarm_when_delay_at_station,R.string.exigency_accelerate,
			R.string.exigency_brake, R.string.ballot_box_door_alarm,
			R.string.exigency_alarm,R.string.deviate_route,
			R.string.electronic_unlock, R.string.electronic_lock_closed,
			R.string.electronic_lock_abnormity, R.string.input_one_trigger,
			R.string.input_two_trigger, R.string.input_three_trigger,
			R.string.input_four_trigger, R.string.input_five_trigger,
			R.string.input_six_trigger, R.string.input_seven_trigger,
			R.string.input_eight_trigger, R.string.illegality_close,
			R.string.overload, R.string.beyond_the_mark,
			R.string.remove_slop_over, R.string.overtime_parking,
			R.string.remove_overtime_parking, R.string.fatigue_driving,
			R.string.remove_fatigue_driving };
	
	
	
	
	
	public Context context;
	public static Map<Integer, Boolean> isSelected;
	public SharedPreferences sp;
	public Editor editor;
	public final static String[] str = new String[str_alarm_info.length];
	public final static boolean[] prefs_str = new boolean[str_alarm_info.length];
	public String[] maps = new String[str_alarm_info.length];
	/* 构造函数 */
	public FilterAlarmInfoAdapter(Context context) {
		super();
		this.context = context;
		init();
	}

	private void init() {
		// TODO Auto-generated method stub
		isSelected = new HashMap<Integer, Boolean>();
		for (int i = 0; i < str_alarm_info.length; i++) {
			isSelected.put(i, false);
		}
	}

	/* 条目数目 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return str_alarm_info.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return str_alarm_info[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	/* oop思想，优化内存对象 */
	public final class ViewHolder {
		public TextView tv_filter;
		public CheckBox checkbox_filter;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHoler = null;
		if (convertView == null) {
			viewHoler = new ViewHolder();
			View view = LayoutInflater.from(context).inflate(R.layout.filter_alarm_info, null);
			viewHoler.tv_filter = (TextView) view
					.findViewById(R.id.tv_filter_info);
			viewHoler.checkbox_filter = (CheckBox) view
					.findViewById(R.id.checkbox_filter);
			convertView = view;
			convertView.setTag(viewHoler);
		} else {
			viewHoler = (ViewHolder) convertView.getTag();
		}
		viewHoler.tv_filter.setText(str_alarm_info[position]);
		
		viewHoler.tv_filter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		/*checkbox复选框的单击事件*/
		viewHoler.checkbox_filter.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					isSelected.put(position, true);
				}else{
					isSelected.put(position, false);
				}
				sp = context.getSharedPreferences("sp", Context.MODE_WORLD_WRITEABLE);
				editor = sp.edit();
				editor.putBoolean(str[position], isChecked);
				editor.commit();
				prefs_str[position] = sp.getBoolean(str[position], false);
			}
		});
		viewHoler.checkbox_filter.setChecked(isSelected.get(position));
		viewHoler.checkbox_filter.setChecked(prefs_str[position]);
		return convertView;
	}
}
