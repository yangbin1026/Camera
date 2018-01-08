package com.monitor.bus.bean;

/**
 * 报警信息
 */
public class AlarmInfo {
	private String deviceId;//设备ID
	private int channelId;//当前通道号
	private String alarmString;//报警描述
	private int alarmType;
	private String deviceName;
	
	
	public String getDeviceId() {
		return deviceId;
	}
	public int getAlarmType() {
		return alarmType;
	}
	public void setAlarmType(int alarmType) {
		this.alarmType = alarmType;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getdeviceId() {
		return deviceId;
	}
	public void setDeviceId(String guId) {
		this.deviceId = guId;
	}
	public int getChannelId() {
		return channelId;
	}
	public void setChannelId(int currentChn) {
		this.channelId = currentChn;
	}
	public String getAlarmString() {
		return alarmString;
	}
	public void setAlarmString(String expresion) {
		this.alarmString = expresion;
	}
	@Override
	public String toString() {
		return "AlarmInfo [deviceId=" + deviceId + ", channelId=" + channelId + ", alarmString=" + alarmString
				+ ", alarmType=" + alarmType + ", deviceName=" + deviceName + "]";
	}
	
}
