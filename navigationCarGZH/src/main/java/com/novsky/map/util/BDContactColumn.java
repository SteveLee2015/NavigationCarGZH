package com.novsky.map.util;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 北斗通讯录实体类
 * @author steve
 */
public class BDContactColumn implements BaseColumns{
	
	
	public static String AUTHORITY="com.bd.comm.protocol.BDContactColumn";
	
	
	public  static Uri CONTENT_URI=Uri.parse("content://"+AUTHORITY+"/bdcontact");
	

	public static final String  CONTENT_TYPE="vnd.android.cursor.dir/vnd.android.BDContactProvider";
	
	
	public static final String  CONTENT_ITEM_TYPE="vnd.android.cursor.item.dir/vnd.android.BDContactProvider";


	/**
	 * 表名称
	 */
	public  static final String TABLE_NAME="DB_CONTACT_INFO_TB";
	
	/**
	 * 用户名称
	 */
	public  static final String USER_NAME="bd_user_name";
	
	/**
	 * 北斗卡号
	 */
	public  static final String CARD_NUM="bd_card_num";
	
	/**
	 * 北斗卡的级别
	 */
	public  static final String CARD_LEVEL="bd_card_level";
	
	/**
	 * 北斗卡序列号
	 */
	public  static final String CARD_SERIAL_NUM="bd_card_serial_num";
	
	/**
	 * 北斗卡频度
	 */
	public  static final String CARD_FREQUENCY="bd_card_frequency";
	
	/**
	 * 用户地址
	 */
	public  static final String USER_ADDRESS="user_address";
	
	/**
	 * 手机号码
	 */
	public  static final String PHONE_NUMBER="phone_number";
	
	/**
	 * 邮件地址
	 */
	public  static final String USER_EMAIL="user_email";
	
	/**
	 * 是否是本机卡号 0-外卡 1-本机卡
	 */
	public  static final String CHECK_CURRENT_NUM="check_curr_num";
	
	/**
	 * 首字母的索引
	 */
	public  static final String FIRST_LETTER_INDEX="fst_letter_num";
	
	/**
	 * 备注
	 */
	public  static final String REMARK="remark";
	
	public  static  String  DEFAULT_SORT_ORDER=BDContactColumn._ID+ " desc";
	
	public  static String[] COLUMNS={
		_ID,USER_NAME,CARD_NUM,CARD_LEVEL,
		CARD_SERIAL_NUM,CARD_FREQUENCY,
		USER_ADDRESS,PHONE_NUMBER,USER_EMAIL,
		CHECK_CURRENT_NUM,FIRST_LETTER_INDEX,REMARK
	};

}
