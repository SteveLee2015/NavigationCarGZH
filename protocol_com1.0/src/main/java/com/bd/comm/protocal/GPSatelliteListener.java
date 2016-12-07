package com.bd.comm.protocal;

import java.util.List;

import android.location.BDEventListener;

public interface GPSatelliteListener extends BDEventListener {
	void onGpsStatusChanged(List<GPSatellite> list);
}
