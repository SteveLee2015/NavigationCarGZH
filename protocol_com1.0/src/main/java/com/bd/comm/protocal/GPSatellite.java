/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bd.comm.protocal;

import android.location.GpsStatus;
import android.location.LocationManager;

/**
 * This class represents the current state of a GPS satellite.
 * This class is used in conjunction with the {@link GpsStatus} class.
 */
public final class GPSatellite {
	   /* These package private values are modified by the GpsStatus class */
    boolean mValid;
    boolean mHasEphemeris;
    boolean mHasAlmanac;
    boolean mUsedInFix=false;
    int mPrn;  //卫星号码
    float mSnr; //信噪比
    float mElevation; //海拔
    float mAzimuth; //方位
    
    
    public GPSatellite(){
    	
    }
    
    GPSatellite(int prn) {
        mPrn = prn;
    }

    /**
     * Used by {@link LocationManager#getGpsStatus} to copy LocationManager's
     * cached GpsStatus instance to the client's copy.
     */
    void setStatus(BDSatellite satellite) {
        mValid = satellite.mValid;
        mHasEphemeris = satellite.mHasEphemeris;
        mHasAlmanac = satellite.mHasAlmanac;
        mUsedInFix = satellite.mUsedInFix;
        mSnr = satellite.mSnr;
        mElevation = satellite.mElevation;
        mAzimuth = satellite.mAzimuth;
    }

    /**
     * Returns the PRN (pseudo-random number) for the satellite.
     *
     * @return PRN number
     */
    public int getPrn() {
        return mPrn;
    }

    /**
     * Returns the signal to noise ratio for the satellite.
     *
     * @return the signal to noise ratio
     */
    public float getSnr() {
        return mSnr;
    }

    /**
     * Returns the elevation of the satellite in degrees.
     * The elevation can vary between 0 and 90.
     *
     * @return the elevation in degrees
     */
    public float getElevation() {
        return mElevation;
    }

    /**
     * Returns the azimuth of the satellite in degrees.
     * The azimuth can vary between 0 and 360.
     * @return the azimuth in degrees
     */
    public float getAzimuth() {
        return mAzimuth;
    }

    /**
     * Returns true if the GPS engine has ephemeris data for the satellite.
     *
     * @return true if the satellite has ephemeris data
     */
    public boolean hasEphemeris() {
        return mHasEphemeris;
    }

    /**
     * Returns true if the GPS engine has almanac data for the satellite.
     *
     * @return true if the satellite has almanac data
     */
    public boolean hasAlmanac() {
        return mHasAlmanac;
    }

    /**
     * Returns true if the satellite was used by the GPS engine when
     * calculating the most recent GPS fix.
     *
     * @return true if the satellite was used to compute the most recent fix.
     */
    public boolean usedInFix() {
        return mUsedInFix;
    }

	public void setHasEphemeris(boolean mHasEphemeris) {
		this.mHasEphemeris = mHasEphemeris;
	}

	public void setHasAlmanac(boolean mHasAlmanac) {
		this.mHasAlmanac = mHasAlmanac;
	}

	public void setUsedInFix(boolean mUsedInFix) {
		this.mUsedInFix = mUsedInFix;
	}

	public void setPrn(int mPrn) {
		this.mPrn = mPrn;
	}

	public void setSnr(float mSnr) {
		this.mSnr = mSnr;
	}

	public void setElevation(float mElevation) {
		this.mElevation = mElevation;
	}

	public void setAzimuth(float mAzimuth) {
		this.mAzimuth = mAzimuth;
	}
    
}
