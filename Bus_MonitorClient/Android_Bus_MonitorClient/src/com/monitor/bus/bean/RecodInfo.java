package com.monitor.bus.bean;

import java.io.Serializable;

import com.monitor.bus.consts.Constants;
/**
 * 远程录像请求信息
 */
public class RecodInfo implements Serializable{
	
	/**
	 * 录像查询
	 * @param deviceId		设备ID
	 * @param iCenter		1，中心录像；0，设备录像
	 * @param iType			1,普通录像；2,报警录像
	 * @param startTime		开始时间
	 * @param endTime		结束时间
	 * @param iChnFlag		通道号
	 * @param filePath		设备端文件路径
	 * @return
	 */
	private boolean isLocalVideo =true;
	private String deviceId = "";
	private int center;
	private int type;
	private String startTime;
	private String endTime;
	private int channelId;
	private String path = "";
	
	
	public boolean isLocalVideo() {
		return isLocalVideo;
	}
	public void setLocalVideo(boolean isLocalVideo) {
		this.isLocalVideo = isLocalVideo;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public int getCenter() {
		return center;
	}
	public int getType() {
		return type;
	}
	public int getChannelId() {
		return channelId;
	}
	public String getPath() {
		return path;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public void setCenter(int center) {
		this.center = center;
	}
	public void setType(int type) {
		this.type = type;
	}
	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getStartTime() {
		return startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	



}
