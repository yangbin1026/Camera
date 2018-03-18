package com.monitor.bus.bean;

import java.io.Serializable;

import com.monitor.bus.Constants;
/**
 * 设备信息
 */
public class DeviceInfo implements Serializable{


	private static final long serialVersionUID = 1L;
	private String	groupId;//组ID
	private	String	groupName;//组名称

	private	String	parentId;//父ID
	private	boolean isDeviceGroup;//是否为设备的组
	private String	deviceId;//设备ID
	private String	deviceName;//设备名称
	private String	guId;//唯一标示id
	private String	sn;
	private	int		onLine;//是否在线    0:在线
	private	String	maxSpeed;//最大速度
	private	String	minSpeed;//最小速度
	private int 	currentChn;//当前通道
	private double	longitude =0.0d;
	private	double	latitude =0.0d;
	
	
	private	int		encoderNumber;
	private	String	deviceType;
	private	String	maxSpeedNation;
	private	String	minSpeedNation;
	private String	maxSpeedRapid;
	private String	minSpeedRapid;
	private	String	model;
	private	String	plateNum;
	private	String 	lineId;
	private	String	driver;//司机

	private String	centerServerID; //中心服务器
	private String	cmdServerID;	//信令服务器
	private String	mediaServerID;	//媒体服务器

	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public boolean issDeviceGroup() {
		return isDeviceGroup;
	}
	public void setIsDeviceGroup(boolean isDeviceGroup) {
		this.isDeviceGroup = isDeviceGroup;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getGuId() {
		return guId;
	}
	
	/**
	 * 新的Guid，支持级联
	 * @return
	 */
	public String getNewGuId() {
		if(Constants.IS_CASCADE_SERVER){
			
			String str = guId+"#"+ this.getCenterServerID()
					+ "#" + this.getCmdServerID() + "#" + this.getMediaServerID();
//			LogUtils.i("BuSDeviceInfo", str);
			return str;
		}
		return guId;
	}
	
	public void setGuId(String guId) {
		this.guId = guId;
	}
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	public int getOnLine() {
		return onLine;
	}
	public void setOnLine(int onLine) {
		this.onLine = onLine;
	}
	public String getMaxSpeed() {
		return maxSpeed;
	}
	public void setMaxSpeed(String maxSpeed) {
		this.maxSpeed = maxSpeed;
	}
	public String getMinSpeed() {
		return minSpeed;
	}
	public void setMinSpeed(String minSpeed) {
		this.minSpeed = minSpeed;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public int getEncoderNumber() {
		return encoderNumber;
	}
	public void setEncoderNumber(int encoderNumber) {
		this.encoderNumber = encoderNumber;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public String getMaxSpeedNation() {
		return maxSpeedNation;
	}
	public void setMaxSpeedNation(String maxSpeedNation) {
		this.maxSpeedNation = maxSpeedNation;
	}
	public String getMinSpeedNation() {
		return minSpeedNation;
	}
	public void setMinSpeedNation(String minSpeedNation) {
		this.minSpeedNation = minSpeedNation;
	}
	public String getMaxSpeedRapid() {
		return maxSpeedRapid;
	}
	public void setMaxSpeedRapid(String maxSpeedRapid) {
		this.maxSpeedRapid = maxSpeedRapid;
	}
	public String getMinSpeedRapid() {
		return minSpeedRapid;
	}
	public void setMinSpeedRapid(String minSpeedRapid) {
		this.minSpeedRapid = minSpeedRapid;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getPlateNum() {
		return plateNum;
	}
	public void setPlateNum(String plateNum) {
		this.plateNum = plateNum;
	}
	public String getLineId() {
		return lineId;
	}
	public void setLineId(String lineId) {
		this.lineId = lineId;
	}
	public String getDriver() {
		return driver;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	}

	public int getCurrentChn() {
		return currentChn;
	}
	public void setCurrentChn(int currentChn) {
		this.currentChn = currentChn;
	}


	@Override
	public String toString() {
		return "DeviceInfo [groupId=" + groupId + ", parentId=" + parentId
				+ ", isDeviceGroup=" + isDeviceGroup + ", groupName=" + groupName + ", deviceId="
				+ deviceId + ", deviceName=" + deviceName + ", guId=" + guId
				+ ", sn=" + sn + ", onLine=" + onLine
				+ ", maxSpeed=" + maxSpeed + ", minSpeed=" + minSpeed+ ", longitude=" + longitude  
				+ ", latitude=" + latitude + ", encoderNumber=" + encoderNumber+ ", deviceType=" + deviceType 
				+ ", maxSpeedNation=" + maxSpeedNation + ", minSpeedNation=" + minSpeedNation+ ", maxSpeedRapid=" + maxSpeedRapid 
				+ ", minSpeedRapid=" + minSpeedRapid + ", model=" + model+ ", plateNum=" + plateNum 
				+ ", lineId=" + lineId + ", driver=" + driver+"+,centerServerID="+centerServerID
				+ ",mediaServerID="+mediaServerID+"cmdServerID="+cmdServerID+"]";
	}
	public String getCenterServerID() {
		return centerServerID;
	}
	public void setCenterServerID(String centerServerID) {
		this.centerServerID = centerServerID;
	}
	public String getCmdServerID() {
		return cmdServerID;
	}
	public void setCmdServerID(String cmdServerID) {
		this.cmdServerID = cmdServerID;
	}
	public String getMediaServerID() {
		return mediaServerID;
	}
	public void setMediaServerID(String mediaServerID) {
		this.mediaServerID = mediaServerID;
	}





}
