package com.novsky.map.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 北斗参与定位卫星的广播接收者
 * @author steve
 */
public class BDStatelliteStatusReceiver extends BroadcastReceiver {

	
	private final String TAG="BDStatelliteStatusReceiver";
		
	private static final String BD_INUSE_UPDATE_ACTION ="android.location.BD_SVC_INUSE_NUMBER";

	private static final String BD_INUSE_NUM_UPDATE ="inUsedNumber";

	private static final String BD_INUSE_PRN_UPDATE ="inUsedList";
	
	private BDAvailableStatelliteManager statellitesManager=null;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (action.equals(BD_INUSE_UPDATE_ACTION)){
			statellitesManager=BDAvailableStatelliteManager.getInstance();
	        int number = intent.getIntExtra(BD_INUSE_NUM_UPDATE, 0);
	        int BdSvc[]=new int[number];
	        BdSvc =  intent.getIntArrayExtra(BD_INUSE_PRN_UPDATE);	       
	        statellitesManager.updateAvailableStatellites(BdSvc);
	    }
	}
}
