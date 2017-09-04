package com.novsky.map.util;

import android.location.BDLocation;





/**
 * 北斗RDSS定位信息的单例类
 * 
 * @author steve
 */
public class BDRNSSLocationInfoManager {

	private static BDRNSSLocationInfoManager instance = null;

	private BDLocation location = null;

    public static BDRNSSLocationInfoManager getInstance() {
		if (instance == null) {
			instance = new BDRNSSLocationInfoManager();
		}
		return instance;
	}
    
	/**
	 * 得到北斗定位信息
	 * @return
	 */
	public BDLocation getLocatoion() {
		return location;
	}

	/**
	 * 设置北斗定位信息
	 * @param location
	 */
	public void  setBDLocation(BDLocation location) {
		synchronized(this){
			this.location=location;	
		}
	}
}
