package com.monitor.bus.bean;

import java.io.Serializable;
/**
* 设备端录像信息
*/
public class DevRecordInfo implements Serializable{

	private static final long serialVersionUID = 1L;
		private String fileName;//文件名称
		private String bTime;//开始时间
		private String eTime;//结束时间
		private String fileSize;//文件大小
		private String timeLen;//文件长度
		private int chnIndex;//通道号
		private int recType;//录像类型
		private String guId;//设备ID
		
		
		public String getFileName() {
			return fileName;
		}
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
		public String getbTime() {
			return bTime;
		}
		public void setbTime(String bTime) {
			this.bTime = bTime;
		}
		public String geteTime() {
			return eTime;
		}
		public void seteTime(String eTime) {
			this.eTime = eTime;
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
		public int getChnIndex() {
			return chnIndex;
		}
		public void setChnIndex(int chnIndex) {
			this.chnIndex = chnIndex;
		}
		public int getRecType() {
			return recType;
		}
		public void setRecType(int recType) {
			this.recType = recType;
		}
		public String getGuId() {
			return guId;
		}
		public void setGuId(String guId) {
			this.guId = guId;
		}
		
		
}
