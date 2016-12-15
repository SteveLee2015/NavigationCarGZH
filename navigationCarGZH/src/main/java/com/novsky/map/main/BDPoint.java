package com.novsky.map.main;

/**
 * 北斗指令导航和路径导航的实体类
 * @author steve
 *
 */
public class BDPoint {
	
	/**
	 * 经度
	 */
	private double lon;
	
	/**
	 * 经度方向
	 */
	private String  lonDirection;
	
	/**
	 * 纬度
	 */
	private double lat;
	
	/**
	 * 纬度方向
	 */
	private String latDirection;
	
	

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	@Override
	public String toString() {
		return lon+","+lonDirection+","+lat+","+latDirection;
	}

	public String getLonDirection() {
		return lonDirection;
	}

	public void setLonDirection(String lonDirection) {
		this.lonDirection = lonDirection;
	}

	public String getLatDirection() {
		return latDirection;
	}

	public void setLatDirection(String latDirection) {
		this.latDirection = latDirection;
	}



	public boolean equals(Object obj){
		if(obj instanceof BDPoint){
			BDPoint user=(BDPoint)obj;
			return (toString().equals(user.toString()));
		}
		return super.equals(obj);
	}
	public int hashCode(){
		return toString().hashCode();
	}
}
