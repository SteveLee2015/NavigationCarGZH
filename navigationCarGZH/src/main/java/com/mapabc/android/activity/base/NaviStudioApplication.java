/**
 * 
 */
package com.mapabc.android.activity.base;

import com.bd.comm.protocal.SerialApplication;
import com.mapabc.naviapi.type.Const;

/**
 * desciption:
 * 
 */
//public class NaviStudioApplication extends SerialApplication{
	public class NaviStudioApplication extends  SerialApplication{
	
	public boolean isRecordTrack=true;
	public boolean isTracePlay=false;
	public int TRACE_PLAY_MODE=Const.TRACE_PLAY_STOP;
	
	//标记星图 已定位  未定位
	public boolean isLocationed = false;
	
}
