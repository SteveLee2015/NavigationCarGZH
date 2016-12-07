package com.bd.comm.protocal;

import java.util.List;

import android.location.BDEventListener;

public interface BDSatelliteListener extends BDEventListener {

	void onBDGpsStatusChanged(List<BDSatellite> list);
}
