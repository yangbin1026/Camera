package com.monitor.bus.bean;

import java.util.ArrayList;
import java.util.List;

import com.monitor.bus.activity.R;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.model.AlarmInfo;
import com.monitor.bus.model.DeviceInfo;

public class AlarmManager {
	private List<AlarmInfo> alarmList = new ArrayList<AlarmInfo>();//报警信息
	static AlarmManager manager;
	private AlarmManager(){
		
	}
	
	public synchronized static AlarmManager getInstance(){
		if(manager==null){
			manager=new AlarmManager();
		}
		return manager;
	}
	public void addAlarmInfo(AlarmInfo info){
		alarmList.add(info);
	}
	public boolean removeFirst(){
		if(alarmList.size()>0){
			alarmList.remove(0);
			return true;
		}
		return false;
	}
	public List<AlarmInfo> getAlarmList(){
		return alarmList;
	}
	public int getSize(){
		return alarmList.size();
	}
	
	
	/**
	 * 返回报警信息
	 * 
	 * @param devId
	 * @param chn
	 * @param alarmType
	 * @return
	 */
//	public String getAlarmInfo(String devId, int chn, int alarmType) {
//		String alarmInfo = null;
//		String devList = currentContext.getString(R.string.devList);
//		String exists = currentContext.getString(R.string.exists);
//		String overspeed = currentContext.getString(R.string.overspeed);
//		String overspeed_cancel_alarm = currentContext.getString(R.string.overspeed_cancel_alarm);
//		/*AlarmInfo devRecordInfo = alarms.get(0);*/
//		/*AlarmInfo devRecordInfo = null;*/
//		currentDeviceInfo = getBusInfo(devId);
//		/*Log.i("+++++++++++", devRecordInfo.getGuId()+ "+" + devId);*/
//		String dev_name = currentDeviceInfo.getDeviceName();
//		if (1 == alarmType) {// 超速
//			alarmInfo = dev_name + devList + exists
//					+ overspeed;
//		} else if (2 == alarmType) {// 超速消警
//			alarmInfo = dev_name + devList + exists
//					+ overspeed_cancel_alarm;
//		} else if (4 == alarmType) {// 输入触发
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.input_trigger);
//		} else if (8 == alarmType) {// 移动侦测
//			alarmInfo = dev_name + devList + (chn - 1) + exists
//					+ currentContext.getString(R.string.motion_detection);
//		} else if (16 == alarmType) {// 视频丢失
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.video_lose);
//		} else if (17 == alarmType) {// 低速报警
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.low_speed_alarm);
//		} else if (18 == alarmType) {// 低速消音
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.low_speed_cancel_alarm);
//		} else if (19 == alarmType) {// 违规停车报警
//			alarmInfo = dev_name
//					+ devList
//					+ exists
//					+ currentContext
//							.getString(R.string.violation_parking_alarm);
//		} else if (20 == alarmType) {// 违规停车消警
//			alarmInfo = dev_name
//					+ devList
//					+ exists
//					+ currentContext
//							.getString(R.string.violation_parking_cancel_alarm);
//		} else if (21 == alarmType) {// GPS存储磁盘空间不足
//			alarmInfo = dev_name
//					+ devList
//					+ exists
//					+ currentContext
//							.getString(R.string.insufficient_GPS_disk_space);
//		} else if (22 == alarmType) {// GPS存储停止存储
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.gps_save_ceased);
//		} else if (23 == alarmType) {// 高速超速
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.overspeed_freeway);
//		} else if (24 == alarmType) {// 高速超速消警
//			alarmInfo = dev_name
//					+ devList
//					+ exists
//					+ currentContext
//							.getString(R.string.overspeed_freeway_cancel_alarm);
//		} else if (25 == alarmType) {// 高速低速报警
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.lowspeed_freeway_alarm);
//		} else if (26 == alarmType) {// 高速低速消警
//			alarmInfo = dev_name
//					+ devList
//					+ exists
//					+ currentContext
//							.getString(R.string.lowspeed_freeway_cancel_alarm);
//		} else if (27 == alarmType) {// 国道超速
//			alarmInfo = dev_name
//					+ devList
//					+ exists
//					+ currentContext
//							.getString(R.string.overspeed_national_road);
//		} else if (28 == alarmType) {// 国道超速消警
//			alarmInfo = dev_name
//					+ devList
//					+ exists
//					+ currentContext
//							.getString(R.string.overspeed_nation_road_alarm_canceled);
//		} else if (29 == alarmType) {// 国道低速
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.lowspeed_national_road);
//		} else if (30 == alarmType) {// 国道低速消警
//			alarmInfo = dev_name
//					+ devList
//					+ exists
//					+ currentContext
//							.getString(R.string.lowspeed_nation_road_alarm_canceled);
//		} else if (32 == alarmType) {// GPS模块异常
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.gps_module_abnormal);
//		} else if (64 == alarmType) {// GSensor报警
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.gsensor_alarm);
//		} else if (128 == alarmType) {// 存储介质1异常
//			alarmInfo = dev_name
//					+ devList
//					+ exists
//					+ currentContext
//							.getString(R.string.storage_medium_one_abnormity);
//		} else if (256 == alarmType) {// 存储介质2异常
//			alarmInfo = dev_name
//					+ devList
//					+ exists
//					+ currentContext
//							.getString(R.string.storage_medium_two_abnormity);
//		} else if (512 == alarmType) {// 系统异常
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.system_abnormity);
//		} else if (4096 == alarmType) {// 设备上线
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.device_online);
//			// BusDeviceInfo busInfo = new BusDeviceInfo();
//
//			for (DeviceInfo busInfo : Constants.DEVICE_LIST) {
//				if (devId.equals(busInfo.getGuId())) {
//					busInfo.setOnLine(1);
//				}
//			}
//
//		} else if (8192 == alarmType) {// 设备离线
//
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.device_offline);
//			for (DeviceInfo busInfo : Constants.DEVICE_LIST) {
//				if (devId.equals(busInfo.getGuId())) {
//					busInfo.setOnLine(0);
//				}
//			}
//		} else if (16384 == alarmType) {// 设备进入WIFI区域
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.enter_into_wifi_region);
//		} else if (32768 == alarmType) {// 设备离开WIFI区域
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.leave_wifi_region);
//		} else if (65536 == alarmType) {// 点火
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.ignition);
//		} else if (131072 == alarmType) {// 熄火
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.flameout);
//		} else if (131074 == alarmType) {// 前门打开
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.front_door_opens);
//		} else if (131076 == alarmType) {// 前门关闭
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.front_door_closed);
//		} else if (131080 == alarmType) {// 中门打开
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.middle_door_opens);
//		} else if (131088 == alarmType) {// 中门关闭
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.middle_door_closed);
//		} else if (131104 == alarmType) {// 后门打开
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.postern_opens);
//		} else if (131136 == alarmType) {// 后门关闭
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.postern_closed);
//		} else if (131200 == alarmType) {// 到站未停车
//			alarmInfo = dev_name
//					+ devList
//					+ exists
//					+ currentContext
//							.getString(R.string.arrive_at_station_nostop);
//		} else if (131328 == alarmType) {// 开门行车
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.drive_with_door_open);
//		} else if (131584 == alarmType) {// 到站未开车门
//			alarmInfo = dev_name
//					+ devList
//					+ exists
//					+ currentContext
//							.getString(R.string.arrive_at_station_noopen);
//		} else if (132096 == alarmType) {// 车厢温度高
//			alarmInfo = dev_name
//					+ devList
//					+ exists
//					+ currentContext
//							.getString(R.string.high_temperature_on_carriage);
//		} else if (133120 == alarmType) {// 车厢温度低
//			alarmInfo = dev_name
//					+ devList
//					+ exists
//					+ currentContext
//							.getString(R.string.low_temperature_on_carriage);
//		} else if (135168 == alarmType) {// 滞站告警
//			alarmInfo = dev_name
//					+ devList
//					+ exists
//					+ currentContext
//							.getString(R.string.alarm_when_delay_at_station);
//		} else if (139264 == alarmType) {// 紧急加速
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.exigency_accelerate);
//		} else if (147456 == alarmType) {// 紧急刹车
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.exigency_brake);
//		} else if (163840 == alarmType) {// 票箱门报警
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.ballot_box_door_alarm);
//		} else if (196609 == alarmType) {// 紧急报警
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.exigency_alarm);
//		} else if (196610 == alarmType) {// 偏离路线
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.deviate_route);
//		} else if (196612 == alarmType) {// 电子锁打开
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.electronic_unlock);
//		} else if (196616 == alarmType) {// 电子锁关闭
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.electronic_lock_closed);
//		} else if (196624 == alarmType) {// 电子锁异常
//			alarmInfo = dev_name
//					+ devList
//					+ exists
//					+ currentContext
//							.getString(R.string.electronic_lock_abnormity);
//		} else if (196640 == alarmType) {// 输入1触发
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.input_one_trigger);
//		} else if (196672 == alarmType) {// 输入2触发
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.input_two_trigger);
//		} else if (196736 == alarmType) {// 输入3触发
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.input_three_trigger);
//		} else if (196864 == alarmType) {// 输入4触发
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.input_four_trigger);
//		} else if (197120 == alarmType) {// 输入5触发
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.input_five_trigger);
//		} else if (197632 == alarmType) {// 输入6触发
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.input_six_trigger);
//		} else if (198656 == alarmType) {// 输入7触发
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.input_seven_trigger);
//		} else if (200704 == alarmType) {// 输入8触发
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.input_eight_trigger);
//		} else if (204800 == alarmType) {// 非法开关门
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.illegality_close);
//		} else if (212992 == alarmType) {// 车辆载客超载
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.overload);
//		} else if (262144 == alarmType) {// 越界
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.beyond_the_mark);
//		} else if (524288 == alarmType) {// 越界消除
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.remove_slop_over);
//		} else if (1048576 == alarmType) {// 超时停车
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.overtime_parking);
//		} else if (2097152 == alarmType) {// 超时停车消除
//			alarmInfo = dev_name
//					+ devList
//					+ exists
//					+ currentContext
//							.getString(R.string.remove_overtime_parking);
//		} else if (4194304 == alarmType) {// 疲劳驾驶
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.fatigue_driving);
//		} else if (8388608 == alarmType) {// 疲劳驾驶消除
//			alarmInfo = dev_name + devList + exists
//					+ currentContext.getString(R.string.remove_fatigue_driving);
//		}
//		return alarmInfo;
//	}
//	

}
