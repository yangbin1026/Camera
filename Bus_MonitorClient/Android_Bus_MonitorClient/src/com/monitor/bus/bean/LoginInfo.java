package com.monitor.bus.bean;

public class LoginInfo {
	String userName;
	int port;
	String passWord;
	String ip;
	
	public LoginInfo(){
		
	}
	public LoginInfo(String userName, int port, String passWord, String ip) {
		super();
		this.userName = userName;
		this.port = port;
		this.passWord = passWord;
		this.ip = ip;
	}
	public String getUserName() {
		return userName;
	}
	public int getPort() {
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
	public void setPort(int port) {
		this.port = port;
	}
	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	@Override
	public String toString() {
		return "LoginInfo [userName=" + userName + ", port=" + port + ", passWord=" + passWord + ", ip=" + ip + "]";
	}
	
}
