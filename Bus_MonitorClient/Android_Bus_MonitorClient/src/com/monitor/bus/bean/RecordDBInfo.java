package com.monitor.bus.bean;

/**
 * 报警信息
 */
public class RecordDBInfo {
	private String fileName;
	private String filePath;
	private String id;
	public String getFileName() {
		return fileName;
	}
	public String getFilePath() {
		return filePath;
	}
	public String getId() {
		return id;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public void setId(String id) {
		this.id = id;
	}
	
}
