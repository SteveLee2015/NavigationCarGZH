package com.novsky.map.main;

import java.io.Serializable;
import java.util.List;

/**
 * 北斗路线导航实体
 * @author steve
 */
public class BDLineNav  implements Serializable{

	
	private String lineId;
	
	/**
	 * 必经点
	 */
	private List<BDPoint> passPoints;
	
	
	public String getLineId() {
		return lineId;
	}

	public void setLineId(String lineId) {
		this.lineId = lineId;
	}

	public List<BDPoint> getPassPoints() {
		return passPoints;
	}

	public void setPassPoints(List<BDPoint> passPoints) {
		this.passPoints = passPoints;
	}

	public String getPassPointsString(){
		if(passPoints!=null){
		  String str="";
		  for(BDPoint mBDPoint:passPoints){
			 str+=mBDPoint.toString()+",";  
		  }
		  return str.substring(0,str.length()-1);
		}else{
		  return null;
		}
	}

//	public int getCurrentIndex() {
//		return currentIndex;
//	}
//
//	public void setCurrentIndex(int currentIndex) {
//		this.currentIndex = currentIndex;
//	}
//
//	public int getTotalNumber() {
//		return totalNumber;
//	}
//
//	public void setTotalNumber(int totalNumber) {
//		this.totalNumber = totalNumber;
//	}

}
