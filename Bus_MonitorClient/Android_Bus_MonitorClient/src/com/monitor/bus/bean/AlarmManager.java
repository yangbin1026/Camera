package com.monitor.bus.bean;

import java.util.ArrayList;
import java.util.List;

import com.monitor.bus.activity.R;
import com.monitor.bus.consts.Constants;
import com.monitor.bus.model.AlarmInfo;
import com.monitor.bus.model.DeviceInfo;

public class AlarmManager {
	private List<AlarmInfo> alarmList = new ArrayList<AlarmInfo>();// 报警信息
	static AlarmManager manager;

	private AlarmManager() {

	}

	public synchronized static AlarmManager getInstance() {
		if (manager == null) {
			manager = new AlarmManager();
		}
		return manager;
	}

	public void addAlarmInfo(AlarmInfo info) {
		alarmList.add(info);
	}

	public boolean removeFirst() {
		if (alarmList.size() > 0) {
			alarmList.remove(0);
			return true;
		}
		return false;
	}

	public List<AlarmInfo> getAlarmList() {
		return alarmList;
	}

	public int getSize() {
		return alarmList.size();
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
