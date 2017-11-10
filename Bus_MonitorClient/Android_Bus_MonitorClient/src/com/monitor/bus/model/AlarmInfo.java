package com.monitor.bus.model;

/**
 * 报警信息
 */
public class AlarmInfo {
	private String guId;//设备ID
	private int currentChn;//当前通道号
	private String expresion;//报警描述
	
	
	public String getGuId() {
		return guId;
	}
	public void setGuId(String guId) {
		this.guId = guId;
	}
	public int getCurrentChn() {
		return currentChn;
	}
	public void setCurrentChn(int currentChn) {
		this.currentChn = currentChn;
	}
	public String getExpresion() {
		return expresion;
	}
	public void setExpresion(String expresion) {
		this.expresion = expresion;
	}
	
	@Override
	public String toString() {
		return "AlarmInfo [guId=" + guId + ", currentChn=" + currentChn
				+ ", expresion=" + expresion +"]";
	}
	
}
