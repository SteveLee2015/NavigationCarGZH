package com.novsky.map.main;

import android.location.Location;

import com.bd.comm.protocal.BDRNSSLocation;

/**
 * 北斗位置报告单例管理类
 * 
 * @author steve
 */
public class BDLocationReportManager {

	/**
	 * 管理类
	 */
	private static BDLocationReportManager mInstance = null;
	/**
	 * 位置报告
	 */
	private  Location Location = null;

	private BDRNSSLocation rnssLocataion=null;
	
	private BDLocationReportManager() {

	}

	public static BDLocationReportManager getInstance() {
		if (mInstance == null){
			mInstance = new BDLocationReportManager();
		}
		return mInstance;
	}

	public  Location getLocation() {
		return Location;
	}

	public  void setLocation(Location location) {
		Location = location;
	}

	public void setRNSSLocation(BDRNSSLocation rnssLocataion){
		this.rnssLocataion=rnssLocataion;
	}
	
	public BDRNSSLocation getBDRNSSLocation(){
		return this.rnssLocataion;
	}
	
}
