package com.novsky.map.main;

import java.util.List;

/**
 * 北斗指令导航实体
 * @author steve
 */
public class BDInstructionNav {
	
	private long rowId;
	
	
	private String lineId;
	
	/**
	 * 目的地
	 */
	private BDPoint targetPoint;
	
	/**
	 * 必经点
	 */
	private List<BDPoint> passPoints;
	
	/**
	 * 规避点
	 */
	private List<BDPoint> evadePoints;

	public String getLineId() {
		return lineId;
	}

	public void setLineId(String lineId) {
		this.lineId = lineId;
	}

	public BDPoint getTargetPoint() {
		return targetPoint;
	}

	public void setTargetPoint(BDPoint targetPoint) {
		this.targetPoint = targetPoint;
	}

	public List<BDPoint> getPassPoints() {
		return passPoints;
	}

	public void setPassPoints(List<BDPoint> passPoints) {
		this.passPoints = passPoints;
	}

	public List<BDPoint> getEvadePoints() {
		return evadePoints;
	}

	public void setEvadePoints(List<BDPoint> evadePoints) {
		this.evadePoints = evadePoints;
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
	
	public String getEvadePointsString(){
		if(evadePoints!=null){
			String str="";
			for(BDPoint mBDPoint:evadePoints){
				str+=(mBDPoint.toString()+",");
			}
			return str.substring(0,str.length()-1);
		}else{
		    return null;	
		}
	}

	public long getRowId() {
		return rowId;
	}

	public void setRowId(long rowId) {
		this.rowId = rowId;
	}
}
