package com.novsky.map.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

/**
 * 北斗定位管理类
 * @author steve
 */
public class BDLocationManager {

	private static BDLocationManager instance = null;

	private static LocationManager mLocationManager = null;
	
	private List<LocationListener> listeners=new ArrayList<LocationListener>();

	private BDLocationManager() {
	}

	public static BDLocationManager getInstance(Context context) {
		if (instance == null) {
			instance = new BDLocationManager();
		    mLocationManager = (LocationManager)context
							.getSystemService(Context.LOCATION_SERVICE);
		}
		return instance;
	}

	public void requestLocationUpdate(LocationListener locationListener) {
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000, 0, locationListener);
		listeners.add(locationListener);
	}
	
	/**
	 * 删除掉所有的定位监听
	 */
	public void removeLocationUpdates(){
		if(listeners!=null){
			for(int i=0;i<listeners.size();i++){
				LocationListener listener=listeners.get(i);
		        mLocationManager.removeUpdates(listener); 
		        Log.i("BDLocationManager",listener.toString());
			}
			listeners.clear();
		}
	}
}
