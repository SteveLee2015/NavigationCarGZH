package com.novsky.map.main;

/**
 * 北斗参与定位的有效卫星信息管理类
 * @author steve
 */
public class BDAvailableStatelliteManager {

	private static BDAvailableStatelliteManager instance=null;
	
	private  int[]  availableStatellites=null;
	
	private BDAvailableStatelliteManager(){
		
	}
	
	public static BDAvailableStatelliteManager getInstance(){
		if(instance==null){
			instance=new BDAvailableStatelliteManager();
		}
		return instance;
	}
	
	public void updateAvailableStatellites(int[] availableStatellites){
		this.availableStatellites=availableStatellites;
	}
	
	/**
	 * 判断卫星号为statelliteID的卫星是否参与定位
	 * @param statelliteID
	 * @return
	 */
	public boolean usedInFix(int statelliteID){
		if(availableStatellites!=null){
				for(int i=0;i<availableStatellites.length;i++){
					if(availableStatellites[i]==statelliteID){
						return true;
					}
				}
		}
		return false;
	}
	
	/**
	 * 删除数据
	 */
	public void removeAllDatas(){
		availableStatellites=null;
	}
}
