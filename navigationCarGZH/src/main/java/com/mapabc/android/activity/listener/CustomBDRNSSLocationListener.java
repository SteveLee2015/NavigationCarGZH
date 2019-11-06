package com.mapabc.android.activity.listener;

import android.os.Bundle;

import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;

/**
 * Created by lisheng on 2019/2/12 0012.
 */
public class CustomBDRNSSLocationListener implements BDRNSSLocationListener {

    @Override
    public void onLocationChanged(BDRNSSLocation location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
