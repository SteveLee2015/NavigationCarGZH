package com.bd.comm.protocal;

import android.location.BDEventListener;

public interface Network4GListener extends BDEventListener {
    public void on4GResponse(String msg);
}
