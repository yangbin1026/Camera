package com.monitor.bus.bean;

import java.io.Serializable;
/**
* 设备端录像信息
*/
public class RecordInfo implements Serializable{

	private static final long serialVersionUID = 1L;
		private String fileName;//文件名称    
		private String fileSize;//文件大小
		private String timeLen;//文件长度
		private String path;
		
		private String startTime;//开始时间       1
		private String endTime;//结束时间          1
		private String deviceId;//设备ID     1
		private int channeId = 1;//通道号          1
		private int recType = 0;//录像类型
		
		private boolean isLocalVideo =true;
		
		
		
		
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}
		public boolean isLocalVideo() {
			return isLocalVideo;
		}
		public void setLocalVideo(boolean isLocalVideo) {
			this.isLocalVideo = isLocalVideo;
		}
		public String getFileName() {
			return fileName;
		}
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
		public String getStartTime() {
			return startTime;
		}
		public void setStartTime(String bTime) {
			this.startTime = bTime;
		}
		public String getEndTime() {
			return endTime;
		}
		public void setEndTime(String eTime) {
			this.endTime = eTime;
		}
		public String getFileSize() {
			return fileSize;
		}
		public void setFileSize(String fileSize) {
			this.fileSize = fileSize;
		}
		public String getTimeLen() {
			return timeLen;
		}
		public void setTimeLen(String timeLen) {
			this.timeLen = timeLen;
		}
		public int getChanneId() {
			return channeId;
		}
		public void setChanneId(int chnIndex) {
			this.channeId = chnIndex;
		}
		public int getRecType() {
			return recType;
		}
		public void setRecType(int recType) {
			this.recType = recType;
		}
		public String getDeviceId() {
			return deviceId;
		}
		public void setDeviceId(String guId) {
			this.deviceId = guId;
		}
		@Override
		public String toString() {
			return "RecordInfo [fileName=" + fileName + ", fileSize=" + fileSize + ", timeLen=" + timeLen
					+ ", startTime=" + startTime + ", endTime=" + endTime + ", deviceId=" + deviceId + ", channeId="
					+ channeId + ", recType=" + recType + ", isLocalVideo=" + isLocalVideo + "]";
		}
		
		
}
