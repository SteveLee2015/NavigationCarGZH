package com.mapabc.android.activity.base;

import android.content.Context;
import android.util.Log;

import com.mapabc.naviapi.MapAPI;
import com.mapabc.naviapi.type.Const;
import com.mapabc.naviapi.type.NSLonLat;
import com.mapabc.naviapi.type.Overlay;

public class ReportLayer {

    public static final int REPORT_LAY = 11;// 位置报告所在图层ID
    
    private static String TAG="ReportLayer";
	
	public boolean deleteOverlay(int overlayID){
		return MapAPI.getInstance().delOverlay(REPORT_LAY, overlayID, Const.MAP_OVERLAY_LINE);
	}
	public boolean deleteLayer(){
		return MapAPI.getInstance().delLayer(REPORT_LAY);
	}
	public boolean unShowLayer(){
		return MapAPI.getInstance().updateOverlays(REPORT_LAY, false);
	}
	public boolean ShowLayer(){
		return MapAPI.getInstance().updateOverlays(REPORT_LAY, true);
	}
	
	/**
	 * 增加位置报告点  删除上次报告位置
	 * */
	public boolean addReportPos(Context mContext,NSLonLat pos, int type,String cardNo) {
		String name=com.novsky.map.util.Utils.getContractMap(mContext).get(cardNo);
	    Log.i(TAG, "--------------------------->name="+name);
		String fullName=cardNo;
		if(name!=null&&!"".equals(name)){
			fullName=name;
	    }
		Log.i(TAG, "--------------------------->fullName="+fullName);
		int id=findOverlayByName(fullName);
		Log.i(TAG, "--------------------------->id="+id);
		Overlay overlay = new Overlay();
		overlay.id = type;
		if(id>=0){
		    MapAPI.getInstance().delOverlay(REPORT_LAY, id, Const.MAP_OVERLAY_POI);	
		    overlay.id = id;
		}
		Log.i(TAG, "--------------------------->type="+type);
		
		overlay.type = Const.MAP_OVERLAY_POI;
		overlay.hide = false;
		overlay.lons = new float[]{pos.x};
		overlay.lats = new float[]{pos.y};
		Log.i(TAG, "------------------>lons="+pos.x+",lat="+pos.y);
		overlay.labelText =fullName;
		overlay.painterName ="";
		overlay.labelName ="poi_point";		
		boolean addret = MapAPI.getInstance().addOverlay(REPORT_LAY, overlay);
		MapAPI.getInstance().setMapCenter(pos);
		return addret;
	}
	/**
	 * 增加位置报告点  不删除上次报告位置
	 * */
	public boolean addReportPosDontDel(Context mContext,NSLonLat pos, int type,String cardNo) {
		String name=com.novsky.map.util.Utils.getContractMap(mContext).get(cardNo);
		Log.i(TAG, "--------------------------->name="+name);
		String fullName=cardNo;
		if(name!=null&&!"".equals(name)){
			fullName=name;
		}
		Log.i(TAG, "--------------------------->fullName="+fullName);
		int id=findOverlayByName(fullName);
		Log.i(TAG, "--------------------------->id="+id);
		Overlay overlay = new Overlay();
		overlay.id = type;
//		if(id>=0){
//			MapAPI.getInstance().delOverlay(REPORT_LAY, id, Const.MAP_OVERLAY_POI);	
//			overlay.id = id;
//		}
		Log.i(TAG, "--------------------------->type="+type);
		
		overlay.type = Const.MAP_OVERLAY_POI;
		overlay.hide = false;
		overlay.lons = new float[]{pos.x};
		overlay.lats = new float[]{pos.y};
		Log.i(TAG, "------------------>lons="+pos.x+",lat="+pos.y);
		overlay.labelText =fullName;
		overlay.painterName ="";
		overlay.labelName ="poi_point";		
		boolean addret = MapAPI.getInstance().addOverlay(REPORT_LAY, overlay);
		MapAPI.getInstance().setMapCenter(pos);
		return addret;
	}
	
    public int findOverlayByName(String name){
		int count=MapAPI.getInstance().getOverlayCount(REPORT_LAY);
		Overlay overlay=new Overlay();
		for(int i=0;i<count;i++){
			if(MapAPI.getInstance().getOverlay(REPORT_LAY, i, overlay)){
			     if(overlay.labelText.equals(name)){
			    	  return i; 
			     }
			}
		}
		return -1; 
	}
	
    
    /**
     * int id = findOverlayByName(fullName);
     * type = id
     * @param type
     * @return
     */
	public boolean deleteReportPoint(int type){
		return MapAPI.getInstance().delOverlay(REPORT_LAY,type,Const.MAP_OVERLAY_POI);
	}
}
