package com.novsky.map.util;

import android.location.BDParameterException;
import android.location.CardInfo;
import android.util.Log;

/**
 * 北斗卡信息管理类
 * @author steve
 */
public class BDCardInfoManager {
	
	private static String TAG="BDCardInfoManager";

	private static BDCardInfoManager bdCardInfoManager=null;
	
	private CardInfo cardInfo=null;
		
	public static BDCardInfoManager getInstance(){
		if(bdCardInfoManager==null){
			bdCardInfoManager=new BDCardInfoManager();
		}
		return bdCardInfoManager;
	}
	
	
	private BDCardInfoManager(){
	}

	
	public CardInfo getCardInfo() {
		return cardInfo;
	}

	
	public void setCardInfo(CardInfo cardInfo) {
		this.cardInfo = cardInfo;
        Log.i(TAG, "==============>"+cardInfo.mCardAddress+","+cardInfo.mSericeFeq);
	}
	
	/**
	 * 是否安装北斗SIM卡 , -1表示发送读卡信息，0-表示未安装北斗卡，1-表示安装北斗卡
	 * @return
	 */
	public boolean checkBDSimCard() throws BDParameterException{
		if(cardInfo!=null){
			 if(cardInfo.mSericeFeq==9999){
				 return false; 
			 }else{
			 	 return true;
			 }
		}else{
		   throw new BDParameterException("BDCardInfoManager:checkBDSimCard() cardInfo is null!");	
		}
	}
}
