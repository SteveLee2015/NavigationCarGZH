package com.novsky.map.main;

/**
 * 
 * @author steve
 * 
 */
public class FriendLocation {
	private long id = 0;
	private  String address = "";// 友邻位置
	private  String reportTime = "";// 报告时间
	private  String friendsLon = "";// 经度
	private  String friendsLat = "";// 纬度
	private  String friendsHeight = "";// 高程

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public  String getAddress() {
		return address;
	}

	public  void setAddress(String address) {
		this.address = address;
	}

	public  String getReportTime() {
		return reportTime;
	}

	public  void setReportTime(String reportTime) {
		this.reportTime = reportTime;
	}

	public  String getFriendsLon() {
		return friendsLon;
	}

	public  void setFriendsLon(String friendsLon) {
		this.friendsLon = friendsLon;
	}

	public  String getFriendsLat() {
		return friendsLat;
	}

	public  void setFriendsLat(String friendsLat) {
		this.friendsLat = friendsLat;
	}

	public  String getFriendsHeight() {
		return friendsHeight;
	}

	public  void setFriendsHeight(String friendsHeight) {
		this.friendsHeight = friendsHeight;
	}

}
