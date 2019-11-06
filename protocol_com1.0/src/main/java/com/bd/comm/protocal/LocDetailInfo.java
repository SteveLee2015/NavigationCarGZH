package com.bd.comm.protocal;

/**
 * Created by lisheng on 2019/2/19.
 */

public class LocDetailInfo {

    private long time;
    private float speed;
    private float bearing;

    private boolean mAvailable;

    public void setParams(boolean mAvailable ,long time , float speed , float bearing) {
        this.mAvailable = mAvailable;
        this.time = time;
        this.speed = speed;
        this.bearing = bearing;
    }

    public void clearParams() {
        this.time = 0;
        this.speed = 0f;
        this.bearing = 0f;
    }

    public boolean isEmpty() {
        if (time == 0 && speed == 0 && bearing == 0) {
            return true;
        }
        return false;
    }

    public long getTime() {
        return time;
    }

    public float getSpeed() {
        return speed;
    }

    public float getBearing() {
        return bearing;
    }

    public boolean isAvailable() {
        return mAvailable;
    }
}
