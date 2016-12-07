package com.novsky.map.util;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

/**
 * 北斗时间频度管理类
 * @author steve
 */
public class BDTimeCountManager {
	
	
	private static final String TAG="BDTimeCountManager";
	
	private static BDTimeCountManager instance = null;
	
    private static Map<String,BDTimeFreqChangedListener> map=null;
	
	private BDTimeCountManager(){}
	
	public static BDTimeCountManager getInstance() {
		if (instance == null) {
			instance = new BDTimeCountManager();	
			map=new HashMap<String,BDTimeFreqChangedListener>();
		}
		return instance;
	}
	
	/**
	 * 注册北斗时间频度改变的监听器
	 * @param listener
	 */
	public synchronized void registerBDTimeFreqListener(String className,BDTimeFreqChangedListener listener){
		if(map==null){
			map=new HashMap<String,BDTimeFreqChangedListener>();
		}
		Log.i(TAG, "======>registerBDTimeFreqListener(className="+className+")");
		map.put(className, listener);
	}

	/**
	 * 注销北斗时间频度改变的事件
	 * @param listener
	 */
	public synchronized boolean unRegisterBDTimeFreqListener(String className){
		BDTimeFreqChangedListener  listener=null;
		if(map!=null){
			Log.i(TAG, "======>unRegisterBDTimeFreqListener(className="+className+")");
			listener=map.remove(className);
	    }
		return listener!=null;
	}
	
	/**
	 * 注销所有北斗时间频度的事件
	 */
	public synchronized void unRegisterAllBDTimeFreqListeners(){
		if(map!=null){
			map.clear();
			map=null;
	    }
	}

	/**
	 * 获得注册监听器的数据
	 * @return
	 */
	public int getSize(){
		int size=0;
		if(map!=null){
			size=map.size();
		}
	   return size;	
	}
	
	/**
	 * 获得当前所有的注册监听器
	 * @return
	 */
	public  Map<String,BDTimeFreqChangedListener> getTimeFreqListeners(){
		return map;
	}
	
	/**
	 *销毁对象
	 */
	public void destroy(){
		if(instance!=null){
			instance=null;	
		}
	}
}
