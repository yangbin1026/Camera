package com.monitor.bus.bean;

import java.io.Serializable;

public class ServerInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	private String sServerID; 			//服务器ID
	private String sServerName;			//服务器名
	private String sServerIp;			//服务器IP
	private int sServerPort;			//服务器端口
	private String sDomainName;			//服务器域名
	private String sParentID;			//父级ID
	private String sCurFlag;				//当前标志
	private int nOnline;					//是否在线 （0：离线  1：在线） 
	private String sOnlineTime;			//在线时间
	private String sMemoryUsed;			//内存使用   "25" 
	private String sVirtualMemoryUsed;	//虚拟内存使用 "21" 
	private String sCPUUsedRate;			//CPU使用率 "0" 
	private String sThreadNumber; 		//线程号 "152" 
	private String sHandleNumber; 		//句柄号	"3183"
	
	public String getServerID() {
		return sServerID;
	}
	public void setServerID(String serverID) {
		sServerID = serverID;
	}
	public String getServerName() {
		return sServerName;
	}
	public void setServerName(String serverName) {
		sServerName = serverName;
	}
	public String getServerIp() {
		return sServerIp;
	}
	public void setServerIp(String serverIp) {
		sServerIp = serverIp;
	}
	public int getServerPort() {
		return sServerPort;
	}
	public void setServerPort(int serverPort) {
		sServerPort = serverPort;
	}
	public String getDomainName() {
		return sDomainName;
	}
	public void setDomainName(String domainName) {
		sDomainName = domainName;
	}
	public String getParentID() {
		return sParentID;
	}
	public void setParentID(String parentID) {
		sParentID = parentID;
	}
	public String getCurFlag() {
		return sCurFlag;
	}
	public void setCurFlag(String curFlag) {
		sCurFlag = curFlag;
	}
	public int getOnline() {
		return nOnline;
	}
	public void setOnline(int online) {
		nOnline = online;
	}
	public String getOnlineTime() {
		return sOnlineTime;
	}
	public void setOnlineTime(String onlineTime) {
		sOnlineTime = onlineTime;
	}
	public String getMemoryUsed() {
		return sMemoryUsed;
	}
	public void setMemoryUsed(String memoryUsed) {
		sMemoryUsed = memoryUsed;
	}
	public String getVirtualMemoryUsed() {
		return sVirtualMemoryUsed;
	}
	public void setVirtualMemoryUsed(String virtualMemoryUsed) {
		sVirtualMemoryUsed = virtualMemoryUsed;
	}
	public String getCPUUsedRate() {
		return sCPUUsedRate;
	}
	public void setCPUUsedRate(String cPUUsedRate) {
		sCPUUsedRate = cPUUsedRate;
	}
	public String getThreadNumber() {
		return sThreadNumber;
	}
	public void setThreadNumber(String threadNumber) {
		sThreadNumber = threadNumber;
	}
	public String getHandleNumber() {
		return sHandleNumber;
	}
	public void setHandleNumber(String handleNumber) {
		sHandleNumber = handleNumber;
	}
	@Override
	public String toString() {
		return "ServerInfo[ServerID="+sServerID+",ServerName="	+sServerName
				+",ServerIp="	+sServerIp	+",ServerPort="		+sServerPort
				+",DomainName="	+sDomainName	+",ParentID="		+sParentID
				+",CurFlag="	+sCurFlag	+",Online="			+nOnline
				+",OnlineTime="	+sOnlineTime	+",MemoryUsed="		+sMemoryUsed	
				+",VirtualMemoryUsed="		+sVirtualMemoryUsed
				+",CPUUsedRate="+sCPUUsedRate+",ThreadNumber="	+sThreadNumber
				+",HandleNumber="+sHandleNumber ;
	}
	
}
