package com.monitor.bus.model;

public class LoginInfo {
	String userName;
	String port;
	String passWord;
	String ip;
	
	public LoginInfo(){
		
	}
	public LoginInfo(String userName, String port, String passWord, String ip) {
		super();
		this.userName = userName;
		this.port = port;
		this.passWord = passWord;
		this.ip = ip;
	}
	public String getUserName() {
		return userName;
	}
	public String getPort() {
		return port;
	}
	public String getPassWord() {
		return passWord;
	}
	public String getIp() {
		return ip;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
}
