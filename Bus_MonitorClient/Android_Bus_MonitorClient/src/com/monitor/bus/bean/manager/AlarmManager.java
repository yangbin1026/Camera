package com.monitor.bus.bean.manager;

import java.util.ArrayList;
import java.util.List;

import com.monitor.bus.activity.R;
import com.monitor.bus.bean.AlarmInfo;
import com.monitor.bus.bean.DeviceInfo;
import com.monitor.bus.utils.LogUtils;

import android.content.Context;

public class AlarmManager {
	private static final String TAG=AlarmManager.class.getSimpleName();
	private List<AlarmInfo> alarmList = new ArrayList<AlarmInfo>();// 报警信息
	static AlarmManager manager;
	private Context mContext;

	private AlarmManager(Context context) {
		if(context!=null){
			mContext=context;
		}
	}
	private AlarmManager(){}

	public synchronized static AlarmManager getInstance(Context context) {
		if(context == null){
			LogUtils.getInstance().localLog(TAG, "Context IS NULL!!!", LogUtils.LOG_NAME_ALARM);
		}
		if (manager == null) {
			manager = new AlarmManager(context);
		}
		return manager;
	}

	public void addAlarmInfo(AlarmInfo info) {
		if (alarmList.size() >= 100) {// 获取的报警信息超过100条
			alarmList.remove(0);
		}
		LogUtils.getInstance().localLog(TAG, "addAlarmInfo:"+info.toString(), LogUtils.LOG_NAME_ALARM);
		info.setAlarmString(getAlarmMessage(info.getDeviceId(), info.getChannelId(), info.getAlarmType()));
		alarmList.add(info);
	}


	public List<AlarmInfo> getAlarmList() {
		return alarmList;
	}

	public List<AlarmInfo> getAlarmListbyType(int type) {
		List<AlarmInfo> list=new ArrayList<AlarmInfo>();
		for(AlarmInfo info: alarmList){
			if(info.getAlarmType()==type){
				list.add(info);
			}
		}
		return list;
	}
	
	
	/**
	 * 返回报警信息
	 * 
	 * @param devId
	 * @param chn
	 * @param alarmType
	 * @return
	 */
	public String getAlarmMessage(String devId, int chn, int alarmType) {
		DeviceInfo deviceInfo = DeviceManager.getInstance().getDeviceInfoById(devId);
		if(deviceInfo==null){
			LogUtils.getInstance().localLog(TAG, "getAlarmMessage() device is NULL deviceid:"+devId,LogUtils.LOG_NAME_ALARM);
			return null;
		}
		String alarmInfo = null;
		String devList = mContext.getString(R.string.devList);
		String exists = mContext.getString(R.string.exists);
		String overspeed = mContext.getString(R.string.overspeed);
		String overspeed_cancel_alarm = mContext.getString(R.string.overspeed_cancel_alarm);
		String deviceNmae = deviceInfo.getDeviceName();
		
		if (1 == alarmType) {// 超速
			alarmInfo = deviceNmae + devList + exists
					+ overspeed;
		} else if (2 == alarmType) {// 超速消警
			alarmInfo = deviceNmae + devList + exists
					+ overspeed_cancel_alarm;
		} else if (4 == alarmType) {// 输入触发
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.input_trigger);
		} else if (8 == alarmType) {// 移动侦测
			alarmInfo = deviceNmae + devList + (chn - 1) + exists
					+ mContext.getString(R.string.motion_detection);
		} else if (16 == alarmType) {// 视频丢失
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.video_lose);
		} else if (17 == alarmType) {// 低速报警
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.low_speed_alarm);
		} else if (18 == alarmType) {// 低速消音
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.low_speed_cancel_alarm);
		} else if (19 == alarmType) {// 违规停车报警
			alarmInfo = deviceNmae
					+ devList
					+ exists
					+ mContext
							.getString(R.string.violation_parking_alarm);
		} else if (20 == alarmType) {// 违规停车消警
			alarmInfo = deviceNmae
					+ devList
					+ exists
					+ mContext
							.getString(R.string.violation_parking_cancel_alarm);
		} else if (21 == alarmType) {// GPS存储磁盘空间不足
			alarmInfo = deviceNmae
					+ devList
					+ exists
					+ mContext
							.getString(R.string.insufficient_GPS_disk_space);
		} else if (22 == alarmType) {// GPS存储停止存储
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.gps_save_ceased);
		} else if (23 == alarmType) {// 高速超速
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.overspeed_freeway);
		} else if (24 == alarmType) {// 高速超速消警
			alarmInfo = deviceNmae
					+ devList
					+ exists
					+ mContext
							.getString(R.string.overspeed_freeway_cancel_alarm);
		} else if (25 == alarmType) {// 高速低速报警
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.lowspeed_freeway_alarm);
		} else if (26 == alarmType) {// 高速低速消警
			alarmInfo = deviceNmae
					+ devList
					+ exists
					+ mContext
							.getString(R.string.lowspeed_freeway_cancel_alarm);
		} else if (27 == alarmType) {// 国道超速
			alarmInfo = deviceNmae
					+ devList
					+ exists
					+ mContext
							.getString(R.string.overspeed_national_road);
		} else if (28 == alarmType) {// 国道超速消警
			alarmInfo = deviceNmae
					+ devList
					+ exists
					+ mContext
							.getString(R.string.overspeed_nation_road_alarm_canceled);
		} else if (29 == alarmType) {// 国道低速
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.lowspeed_national_road);
		} else if (30 == alarmType) {// 国道低速消警
			alarmInfo = deviceNmae
					+ devList
					+ exists
					+ mContext
							.getString(R.string.lowspeed_nation_road_alarm_canceled);
		} else if (32 == alarmType) {// GPS模块异常
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.gps_module_abnormal);
		} else if (64 == alarmType) {// GSensor报警
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.gsensor_alarm);
		} else if (128 == alarmType) {// 存储介质1异常
			alarmInfo = deviceNmae
					+ devList
					+ exists
					+ mContext
							.getString(R.string.storage_medium_one_abnormity);
		} else if (256 == alarmType) {// 存储介质2异常
			alarmInfo = deviceNmae
					+ devList
					+ exists
					+ mContext
							.getString(R.string.storage_medium_two_abnormity);
		} else if (512 == alarmType) {// 系统异常
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.system_abnormity);
		} else if (4096 == alarmType) {// 设备上线
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.device_online);
			// BusDeviceInfo busInfo = new BusDeviceInfo();

			for (DeviceInfo busInfo : DeviceManager.getInstance().getAll()) {
				if (devId.equals(busInfo.getGuId())) {
					busInfo.setOnLine(1);
				}
			}

		} else if (8192 == alarmType) {// 设备离线

			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.device_offline);
			for (DeviceInfo busInfo : DeviceManager.getInstance().getAll()) {
				if (devId.equals(busInfo.getGuId())) {
					busInfo.setOnLine(0);
				}
			}
		} else if (16384 == alarmType) {// 设备进入WIFI区域
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.enter_into_wifi_region);
		} else if (32768 == alarmType) {// 设备离开WIFI区域
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.leave_wifi_region);
		} else if (65536 == alarmType) {// 点火
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.ignition);
		} else if (131072 == alarmType) {// 熄火
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.flameout);
		} else if (131074 == alarmType) {// 前门打开
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.front_door_opens);
		} else if (131076 == alarmType) {// 前门关闭
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.front_door_closed);
		} else if (131080 == alarmType) {// 中门打开
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.middle_door_opens);
		} else if (131088 == alarmType) {// 中门关闭
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.middle_door_closed);
		} else if (131104 == alarmType) {// 后门打开
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.postern_opens);
		} else if (131136 == alarmType) {// 后门关闭
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.postern_closed);
		} else if (131200 == alarmType) {// 到站未停车
			alarmInfo = deviceNmae
					+ devList
					+ exists
					+ mContext
							.getString(R.string.arrive_at_station_nostop);
		} else if (131328 == alarmType) {// 开门行车
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.drive_with_door_open);
		} else if (131584 == alarmType) {// 到站未开车门
			alarmInfo = deviceNmae
					+ devList
					+ exists
					+ mContext
							.getString(R.string.arrive_at_station_noopen);
		} else if (132096 == alarmType) {// 车厢温度高
			alarmInfo = deviceNmae
					+ devList
					+ exists
					+ mContext
							.getString(R.string.high_temperature_on_carriage);
		} else if (133120 == alarmType) {// 车厢温度低
			alarmInfo = deviceNmae
					+ devList
					+ exists
					+ mContext
							.getString(R.string.low_temperature_on_carriage);
		} else if (135168 == alarmType) {// 滞站告警
			alarmInfo = deviceNmae
					+ devList
					+ exists
					+ mContext
							.getString(R.string.alarm_when_delay_at_station);
		} else if (139264 == alarmType) {// 紧急加速
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.exigency_accelerate);
		} else if (147456 == alarmType) {// 紧急刹车
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.exigency_brake);
		} else if (163840 == alarmType) {// 票箱门报警
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.ballot_box_door_alarm);
		} else if (196609 == alarmType) {// 紧急报警
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.exigency_alarm);
		} else if (196610 == alarmType) {// 偏离路线
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.deviate_route);
		} else if (196612 == alarmType) {// 电子锁打开
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.electronic_unlock);
		} else if (196616 == alarmType) {// 电子锁关闭
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.electronic_lock_closed);
		} else if (196624 == alarmType) {// 电子锁异常
			alarmInfo = deviceNmae
					+ devList
					+ exists
					+ mContext
							.getString(R.string.electronic_lock_abnormity);
		} else if (196640 == alarmType) {// 输入1触发
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.input_one_trigger);
		} else if (196672 == alarmType) {// 输入2触发
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.input_two_trigger);
		} else if (196736 == alarmType) {// 输入3触发
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.input_three_trigger);
		} else if (196864 == alarmType) {// 输入4触发
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.input_four_trigger);
		} else if (197120 == alarmType) {// 输入5触发
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.input_five_trigger);
		} else if (197632 == alarmType) {// 输入6触发
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.input_six_trigger);
		} else if (198656 == alarmType) {// 输入7触发
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.input_seven_trigger);
		} else if (200704 == alarmType) {// 输入8触发
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.input_eight_trigger);
		} else if (204800 == alarmType) {// 非法开关门
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.illegality_close);
		} else if (212992 == alarmType) {// 车辆载客超载
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.overload);
		} else if (262144 == alarmType) {// 越界
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.beyond_the_mark);
		} else if (524288 == alarmType) {// 越界消除
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.remove_slop_over);
		} else if (1048576 == alarmType) {// 超时停车
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.overtime_parking);
		} else if (2097152 == alarmType) {// 超时停车消除
			alarmInfo = deviceNmae
					+ devList
					+ exists
					+ mContext
							.getString(R.string.remove_overtime_parking);
		} else if (4194304 == alarmType) {// 疲劳驾驶
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.fatigue_driving);
		} else if (8388608 == alarmType) {// 疲劳驾驶消除
			alarmInfo = deviceNmae + devList + exists
					+ mContext.getString(R.string.remove_fatigue_driving);
		}
		return alarmInfo;
	}

	/**
	 * 返回报警类型String
	 * @return
	 */
	public String[] getAlarmTypeString() {
		String[] alarmTypeString = { "超速", " 超速消警", " 输入触发", " 移动侦测", " 视频丢失", " 低速报警", " 低速消音", " 违规停车报警", " 违规停车消警",
				" GPS存储磁盘空间不足", " GPS存储停止存储", " 高速超速", " 高速超速消警", " 高速低速报警", " 高速低速消警 ", " 国道超速 ", " 国道超速消警 ", " 国道低速 ",
				" 国道低速消警 ", " GPS模块异常 ", " GSensor报警", " 存储介质1异常 ", " 存储介质2异常 ", " 系统异常 ", " 设备上线 ", " 设备离线 ",
				" 设备进入WIFI区域 ", " 设备离开WIFI区域 ", " 点火 ", " 熄火 ", " 前门打开 ", " 前门关闭 ", " 中门打开 ", " 后门打开 ", " 后门关闭 ",
				" 到站未停车 ", " 开门行车 ", " 到站未开车门 ", " 车厢温度高 ", " 车厢温度低 ", " 滞站告警", " 紧急加速 ", " 紧急刹车 ", " 票箱门报警 ", " 紧急报警 ",
				" 偏离路线 ", " 电子锁打开 ", " 电子锁关闭 ", " 电子锁异常 ", " 输入1触发 ", " 输入2触发 ", " 输入3触发 ", " 输入4触发 ", " 输入5触发 ",
				" 输入6触发 ", " 输入7触发 ", " 输入8触发 ", " 非法开关门 ", " 车辆载客超载 ", " 越界 ", " 越界消除 ", " 超时停车 ", " 超时停车消除 ",
				" 疲劳驾驶 ", " 疲劳驾驶消除" };
		return alarmTypeString;
	}
	
	public int[] getAlarmType(){
		 int[] alarmType1 = { 1, // 超速
				2, // 超速消警
				4, // 输入触发
				8, // 移动侦测
				16, // 视频丢失
				17, // 低速报警
				18, // 低速消音
				19, // 违规停车报警
				20, // 违规停车消警
				21, // GPS存储磁盘空间不足
				22, // GPS存储停止存储
				23, // 高速超速
				24, // 高速超速消警
				25, // 高速低速报警
				26, // 高速低速消警
				27, // 国道超速
				28, // 国道超速消警
				29, // 国道低速
				30, // 国道低速消警
				32, // GPS模块异常
				64, // GSensor报警
				128, // 存储介质1异常
				256, // 存储介质2异常
				512, // 系统异常
				4096, // 设备上线
				8192, // 设备离线
				16384, // 设备进入WIFI区域
				32768, // 设备离开WIFI区域
				65536, // 点火
				131072, // 熄火
				131074, // 前门打开
				131076, // 前门关闭
				131080, // 中门打开
				131104, // 后门打开
				131136, // 后门关闭
				131200, // 到站未停车
				131328, // 开门行车
				131584, // 到站未开车门
				132096, // 车厢温度高
				133120, // 车厢温度低
				135168, // 滞站告警
				139264, // 紧急加速
				147456, // 紧急刹车
				163840, // 票箱门报警
				196609, // 紧急报警
				196610, // 偏离路线
				196612, // 电子锁打开
				196616, // 电子锁关闭
				196624, // 电子锁异常
				196640, // 输入1触发
				196672, // 输入2触发
				196736, // 输入3触发
				196864, // 输入4触发
				197120, // 输入5触发
				197632, // 输入6触发
				198656, // 输入7触发
				200704, // 输入8触发
				204800, // 非法开关门
				212992, // 车辆载客超载
				262144, // 越界
				524288, // 越界消除
				1048576, // 超时停车
				2097152, // 超时停车消除
				4194304, // 疲劳驾驶
				8388608,// 疲劳驾驶消除
		};
		 return alarmType1;
	}
	
	

}
