package com.mapabc.android.activity.route;

import com.mapabc.android.activity.base.NaviControl;
import com.mapabc.android.activity.base.RouteLayer;
import com.mapabc.naviapi.MapAPI;
import com.mapabc.naviapi.RouteAPI;

public class RoutUtils {
	
	public static void removeCurrentRoute() {
		
		if(NaviControl.getInstance().naviStatus==NaviControl.NAVI_STATUS_REALNAVI){
			NaviControl.getInstance().stopRealNavi();
		}else if(NaviControl.getInstance().naviStatus==NaviControl.NAVI_STATUS_SIMNAVI){
			NaviControl.getInstance().stopSimNavi();
			MapAPI.getInstance().setVehiclePosInfo(RouteAPI.getInstance().getStartPoint(), 0);
		}
		MapAPI.getInstance().setMapCenter(MapAPI.getInstance().getVehiclePos());
		if(RouteAPI.getInstance().clearRoute()){
			
			RouteLayer r = new RouteLayer();
			r.deleteLayer();
		}
	}

}
