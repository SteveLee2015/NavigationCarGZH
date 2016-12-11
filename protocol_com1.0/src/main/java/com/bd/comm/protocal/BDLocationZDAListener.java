package com.bd.comm.protocal;

import android.location.BDEventListener;

/**
 * 北斗定位策略监听类
 * @author llg
 */
public interface BDLocationZDAListener extends BDEventListener{
	void onZDATime(ZDATime mZDATime);
}
