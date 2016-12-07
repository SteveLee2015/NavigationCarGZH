package com.novsky.map.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.novsky.map.util.Utils;

/**
 * 定位状态的广播接收者
 * @author steve
 */
public class LocationStatusReceiver extends BroadcastReceiver {

	private final String TAG="LocationStatusReceiver";

	private LocationStatusManager manager=null;
	
	private BDAvailableStatelliteManager availableManager=null;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		    final String action = intent.getAction();
	        final boolean enabled = intent.getBooleanExtra("enabled", false);
	        manager=LocationStatusManager.getInstance();
	        availableManager=BDAvailableStatelliteManager.getInstance();      
	        if (action.equals("android.location.GPS_FIX_CHANGE") && enabled) {
                Log.i(TAG,"=======>GPS is getting fixes");
                manager.updateLocationStatus(true);
                manager.setLocationStatus(true);
                Utils.INIT_LOCATION_STATUS="已定位";
	        } else if (action.equals("android.location.GPS_ENABLED_CHANGE") && !enabled) {
	        	Log.i(TAG,"=======>GPS is off");
	        } else {
	        	Log.i(TAG,"=============> GPS is on, but not receiving fixes");
	        	manager.updateLocationStatus(false);
	        	manager.setLocationStatus(false);
	        	Utils.INIT_LOCATION_STATUS="未定位";
	        	availableManager.removeAllDatas();
	        }
	 }
}
