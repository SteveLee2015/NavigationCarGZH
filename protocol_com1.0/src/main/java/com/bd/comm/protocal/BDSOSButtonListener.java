package com.bd.comm.protocal;

import android.location.BDEventListener;

public interface BDSOSButtonListener extends BDEventListener {
    public void onSosReceipt(int status) ;
}
