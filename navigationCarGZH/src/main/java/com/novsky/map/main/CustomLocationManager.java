package com.novsky.map.main;

import android.content.Context;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.mapabc.android.activity.listener.CustomBDRNSSLocationListener;
import com.novsky.map.util.Utils;

/**
 * 自定义LocationManager
 *
 * @author steve
 */
public class CustomLocationManager implements LocationListener {
    /**
     * 日志标识
     */
    private static final String TAG = "CustomLocationManager";
    /**
     * RNSS管理类
     */
    private LocationManager lm = null;


    private BDCommManager bdCommManager = null;


    private Context mContext = null;

    /**
     * 最小时间
     */
    private long minTime = 1000l;

    /**
     * 最小距离
     */
    private float minDistance = 0.0f;

    /**
     * 北斗位置报告单例类
     */
    private BDLocationReportManager locationReportManager = null;


    private BDRNSSLocationListener mBDRNSSLocationListener = new CustomBDRNSSLocationListener() {
        @Override
        public void onLocationChanged(BDRNSSLocation arg0) {
            super.onLocationChanged(arg0);
            locationReportManager.setRNSSLocation(arg0);
        }

        @Override
        public void onProviderDisabled(String arg0) {

        }

        @Override
        public void onProviderEnabled(String arg0) {

        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

        }
    };

    public CustomLocationManager(Context mContext) {
        this.mContext = mContext;
    }

    public CustomLocationManager(Context mContext, long minTime, float minDistance) {
        this.mContext = mContext;
        this.minTime = minTime;
        this.minDistance = minDistance;
    }

    public void initLocation() {
        locationReportManager = BDLocationReportManager.getInstance();
        if ("S500".equals(Utils.DEVICE_MODEL)) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            String provider = lm.getBestProvider(criteria, true);
            lm.requestLocationUpdates(provider, minTime, minDistance, this);
        } else {
            bdCommManager = BDCommManager.getInstance(mContext);
            try {
                bdCommManager.addBDEventListener(mBDRNSSLocationListener);
            } catch (BDParameterException e) {
                e.printStackTrace();
            } catch (BDUnknownException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        locationReportManager.setLocation(location);
    }

    @Override
    public void onProviderDisabled(String arg0) {

    }

    @Override
    public void onProviderEnabled(String arg0) {

    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {


    }

    /**
     * 注销
     */
    public void removeLocation() {
        if ("S500".equals(Utils.DEVICE_MODEL)) {
            if (lm != null) {
                lm.removeUpdates(this);
            }
        } else {
            try {
                bdCommManager.removeBDEventListener(mBDRNSSLocationListener);
            } catch (BDUnknownException e) {
                e.printStackTrace();
            } catch (BDParameterException e) {
                e.printStackTrace();
            }
        }
    }
}
