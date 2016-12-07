package com.bd.comm.protocal;

import android.location.BDEventListener;
import android.os.Bundle;

/**
 * RNSS定位监听器
 * @author steve
 */
public interface BDRNSSLocationListener extends BDEventListener {
	void onLocationChanged(BDRNSSLocation location);

	void onStatusChanged(String provider, int status, Bundle extras);

	void onProviderEnabled(String provider);

	void onProviderDisabled(String provider);

}
