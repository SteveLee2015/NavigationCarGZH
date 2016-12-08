package com.bd.comm.protocal;

import android.location.BDEventListener;

import java.util.List;

public interface BDSatelliteListener extends BDEventListener {

	void onBDGpsStatusChanged(List<BDSatellite> list);
}
