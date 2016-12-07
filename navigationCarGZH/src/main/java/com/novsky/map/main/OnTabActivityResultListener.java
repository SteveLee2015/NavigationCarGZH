package com.novsky.map.main;

import android.content.Intent;

/**
 * TabActivity子类的监听器
 * @author steve
 */
public interface OnTabActivityResultListener {
	
	public void onTabActivityResult(int requestCode, int resultCode,Intent data);

}
