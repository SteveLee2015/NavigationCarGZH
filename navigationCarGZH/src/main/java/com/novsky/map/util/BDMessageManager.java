package com.novsky.map.util;


/**
 * 北斗短信单例类
 * 
 * @author steve
 */
public class BDMessageManager {

	private static BDMessageManager instance = null;

	/**
	 * 短信内容
	 */
	private String  message = null;
	
	/**
	 * 通信类型
	 */
	private int  msgContentType=0;
	
	/**
	 * 用户地址
	 */
	private String userAddress=null;
	
	
	public static BDMessageManager getInstance() {
		if (instance == null) {
			instance = new BDMessageManager();
		}
		return instance;
	}

	
	public void destroy(){
		if(instance!=null){
			message=null;
			msgContentType=0;
			userAddress=null;
			instance=null;
		}
	}

	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public int getMsgContentType() {
		return msgContentType;
	}


	public void setMsgContentType(int msgContentType) {
		this.msgContentType = msgContentType;
	}


	public String getUserAddress() {
		return userAddress;
	}


	public void setUserAddress(String userAddress) {
		this.userAddress = userAddress;
	}
	
	public boolean check(){
		if(userAddress!=null&&!"".equals(userAddress)&&message!=null&&!"".equals(message)){
			return true;
		}else{
			return false;
		}
	}

}
