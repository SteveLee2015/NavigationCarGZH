package com.novsky.map.util;

/**
 * @author steven
 */
public class TabSwitchActivityData {
	
	
	private  int TAB_FLAG=0;
	
	private  String PACKAGE_NAME="";
	
	private  String CLASS_NAME="";
	
	private static TabSwitchActivityData mInstance=null;
	
	/**
	 * 获得实例方法
	 */
	public static TabSwitchActivityData getInstance(){
		if(mInstance==null){
			mInstance=new TabSwitchActivityData();
		}
		return mInstance;
	}
	
	private TabSwitchActivityData(){
	 	
	}
	
	public  void setTabFlag(int FLAG){
		TAB_FLAG=FLAG;
	}
	
	public  void setPackageName(String packageName){
		PACKAGE_NAME=packageName;
	}
	
	public  void setClassName(String className){
		CLASS_NAME=className;
	}
	
	public int getTabFlag(){
		return TAB_FLAG;
	}
	
	public String getPackName(){
		return PACKAGE_NAME;
	}
	
	public String getClassName(){
		return CLASS_NAME;
	}

}
