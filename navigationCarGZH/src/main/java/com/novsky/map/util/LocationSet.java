package com.novsky.map.util;

/**
 * 定位设置
 * @author steve
 */
public class LocationSet {
	
	private int id;
	/**
	 * 定位频度
	 */
	private String locationFeq;
	/**
	 * 高程类型  0有测高  1无测高  2测高一  3测高二
	 */
	private String heightType; 
    /**
     * 高程数据
     */
	private String heightValue;
	
	private String tianxianValue;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public String getTianxianValue() {
		return tianxianValue;
	}
	public void setTianxianValue(String tianxianValue) {
		this.tianxianValue = tianxianValue;
	}
	public String getLocationFeq() {
		return locationFeq;
	}
	
	public void setLocationFeq(String locationFeq) {
		this.locationFeq = locationFeq;
	}
	
	public String getHeightType() {
		return heightType;
	}
	
	public void setHeightType(String heightType) {
		
		this.heightType = heightType;
	}
	
	public String getHeightValue() {
		return heightValue;
	}
	
	public void setHeightValue(String heightValue) {
		this.heightValue = heightValue;
	}
}
