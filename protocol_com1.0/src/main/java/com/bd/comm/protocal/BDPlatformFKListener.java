package com.bd.comm.protocal;

import android.location.BDEventListener;

/**
 * Created by lisheng on 2019/2/20.
 */

public interface BDPlatformFKListener extends BDEventListener {
    public void onReceipt(String flag);
}
