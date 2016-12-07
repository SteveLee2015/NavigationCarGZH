package com.novsky.map.util;

import android.location.BDLocation;

/**
 * 北斗RDSS定位信息的单例类
 * 
 * @author steve
 */
public class BDRDSSLocationInfoManager {

	private static BDRDSSLocationInfoManager instance = null;

	private BDLocation bdlocation = null;

	public static BDRDSSLocationInfoManager getInstance() {
		if (instance == null) {
			instance = new BDRDSSLocationInfoManager();
		}
		return instance;
	}
    
	/**
	 * 得到北斗定位信息
	 * @return
	 */
	public BDLocation getBDLocatoion() {
		return bdlocation;
	}

	/**
	 * 设置北斗定位信息
	 * @param location
	 */
	public void setBDLocation(BDLocation location) {
		
		if(bdlocation==null){
			bdlocation=new BDLocation();
		}
		/*将yyyyy.yy的经纬度格式转换成yyy.yyyy的格式*/
		bdlocation.setLongitude(Utils
				.changeLonLatMinuteToDegree(location.mLongitude));
		bdlocation.setLatitude(Utils
				.changeLonLatMinuteToDegree(location.mLatitude));
		bdlocation.setEarthHeight(location.earthHeight);
		/**
		 * 系统时间
		 */
		//SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//bdlocation.mLocationTime=sdf.format(new Date());
		bdlocation.mLocationTime=location.mLocationTime;
	}
}
