package com.novsky.map.main;

import java.util.List;

/**
 * 友邻位置 深圳海力特fuck
 * @author steve
 */
public class FriendLocationNav {
	
	private long rowId;
	
	private String receiveTime;
	
	private String friendCount;
	
	
	
	/**
	 * 友邻集合
	 */
	private List<FriendBDPoint> friendLocationList;



	public long getRowId() {
		return rowId;
	}



	public void setRowId(long rowId) {
		this.rowId = rowId;
	}



	public String getReceiveTime() {
		return receiveTime;
	}



	public void setReceiveTime(String receiveTime) {
		this.receiveTime = receiveTime;
	}



	public String getFriendCount() {
		return friendCount;
	}



	public void setFriendCount(String friendCount) {
		this.friendCount = friendCount;
	}



	public List<FriendBDPoint> getFriendLocationList() {
		return friendLocationList;
	}



	public void setFriendLocationList(List<FriendBDPoint> friendLocationList) {
		this.friendLocationList = friendLocationList;
	}
	


}
