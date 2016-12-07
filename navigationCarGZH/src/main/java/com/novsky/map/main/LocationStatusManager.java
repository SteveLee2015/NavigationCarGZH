package com.novsky.map.main;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.novsky.map.util.Utils;

/**
 * 定位信息管理
 * @author steve
 */
public class LocationStatusManager {
	
	
	private static LocationStatusManager instance=null;

	
	private static List<Handler> lists=null;
	
	private boolean isLocation=false;
	
	
	private LocationStatusManager(){
		
	}
	
	public static LocationStatusManager getInstance(){
		if(instance==null){
			instance=new LocationStatusManager();
			lists=new ArrayList<Handler>();
		}
		return instance;
	}

	public void add(Handler mHandler){
		
		if(lists!=null){
			lists.add(mHandler);
		}
	}
	
	
	public void remove(Handler mHandler){
		 if(lists!=null){
			 lists.remove(mHandler);
		 }
	}
	
	/**
	 * 更新当前卫星定位状态
	 * @param isFix
	 */
	public void updateLocationStatus(boolean isFix){
		if(lists!=null){
		     for(int i=0;i<lists.size();i++){
		    	 Handler mHandler=lists.get(i); 
		    	 Message message=mHandler.obtainMessage();
		    	 Bundle bundle=new Bundle();
		    	 bundle.putBoolean("LOCATION_FIX_STATUS", isFix);
		    	 message.setData(bundle);
		    	 message.what=Utils.HANDLER_LOCATION_STATUS;
		    	 mHandler.sendMessage(message);
		     }
		}		
	}
	
	public void setLocationStatus(boolean isLocation){
		this.isLocation=isLocation;
	}
	
	
	public boolean getLocationStatus(){
		return isLocation;
	}
}
