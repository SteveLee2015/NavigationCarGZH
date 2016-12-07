package com.novsky.map.main;

import android.location.BDLocationReport;


/**
 * 北斗位置报告实现
 * @author steve
 */
public class BDLocationReportImp extends BDLocationReport {
	
    public  double locationReportSpeed=0.0d;
	
	public  double locationReportBearing=0.0d;

	public double getLocationReportSpeed() {
		return locationReportSpeed;
	}

	public void setLocationReportSpeed(double locationReportSpeed) {
		this.locationReportSpeed = locationReportSpeed;
	}

	public double getLocationReportBearing() {
		return locationReportBearing;
	}

	public void setLocationReportBearing(double locationReportBearing) {
		this.locationReportBearing = locationReportBearing;
	}
	
}
