package com.novsky.map.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.location.BDLocation;
import android.location.BDLocationReport;
import android.location.BDMessageInfo;
import android.location.BDParameterException;
import android.location.CardInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mapabc.android.activity.NaviStudioActivity;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.base.Constants;
import com.novsky.map.main.BDLocationReportImp;
import com.novsky.map.main.BDManagerHorizontalActivity;
import com.novsky.map.main.BDSendMsgLandScapeActivity;
import com.novsky.map.main.BDSendMsgPortActivity;
import com.novsky.map.main.CoodrinateDate;
import com.novsky.map.main.FriendBDPoint;
import com.novsky.map.main.FriendsLocationActivity;

import static com.novsky.map.util.Config.MY_LOC_REPORT_NOTIFICATION;

/**
 * 工具类
 * 
 * @author steve
 */
public class Utils{
	
	/**
	 * 工具类日志标识
	 */
	private static final String TAG = "Utils";
	/**
	 * 创建进度条
	 */
	private static ProgressDialog progressDialog = null;

	private static double PI = 3.14159265358979323846;

	public static int MESSAGE_MAX_LENGHTH = 0;

	public static int NOTIFICATION_ID = 0;

	public static double LOCATION_REPORT_LON = 0.0d;

	public static double LOCATION_REPORT_LAT = 0.0d;

	public static double LOCATION_REPORT_ALTITUDE = 0.0d;
	
	public static double LOCATION_REPORT_SPEED=0.0d;
	
	public static double LOCATION_REPORT_BEARING=0.0d;
	
	public static double LOCATION_REPORT_ACCURACY=0.0d;
	
	public static long   LOCATION_REPORT_TIME=0;
	
	public static boolean LOCATION_STATUS=false;

	public static String BD_USER_ADDRESS = "";

	private static List<FriendsLocation> list = null;

	/**
	 * Dialog窗口
	 */
	private static AlertDialog.Builder alert = null;

	private static CardInfo cardInfo = null;

	public static float LON_VALUE = 0.0f;

	public static float LAT_VALUE = 0.0f;

	public static String BD_REPORT_USER_ADDRESS = "";

	public static int CARD_FREQ = 0;

	public static String TIME_CHANGED_ACTION = "com.novsky.map.main.action.TIME_CHANGED_ACTION";
	
	public static String AUTO_REPORT_CHANGED_ACTION = "com.novsky.map.main.action.AUTO_REPORT_CHANGED_ACTION";
	
	
	public static String ALARM_CHANGED_ACTION="com.novsky.map.main.action.ALARM_CHANGED_ACTION";

	
	public static boolean  checkNaviMap= false;
	
	
	private static HashMap<String ,String> map=null;
	
	/**
	 * 判断当前北斗定位端口的对话框对象
	 */
	private static AlertDialog checkBDLocationPortDialog=null;
	
    public final static int HANDLER_LOCATION_STATUS=10000;
	
	public static String INIT_LOCATION_STATUS="";
	
	
	private static WakeLock wakeLock = null;  
	
	public static boolean smsNotificationShow=false;
	
	private static final double bj54a = 6378245.0;

	private static final double bj54f = 298.3;
	
	public static String DEVICE_MODEL=new Build().MODEL;
	
	public static Notification destoryNotification;
	
	/**
	 * message需要清除的notificationID
	 */
	public static int messageNotificationID;
	
	/**
	 * message需要清除的notificationID
	 */
	public static int friendLocationNotificationID;
	
	
	/**
	 * 标识当前更多页面显示的页面的Index值
	 */
	public static int BD_MANAGER_PAGER_INDEX=1;
	
	public static boolean isLand=true;
	
	public static HashMap<String ,String> getContractMap(Context mContext){
		if(map==null){
			map=getContentNew(mContext);
		}
		return map;
	}

	public static void setCardInfo(CardInfo info) {
		cardInfo = info;
		getMessageMaxLength(info.getCheckEncryption(), info.getCommLevel());
	}
	
	/**
	 * 根据电话号码获得名称.
	 * @param context
	 * @param phoneNum  电话号码
	 * @return
	 */
	public static String getContactNameFromPhoneNum(Context context, String phoneNum) {
		if(phoneNum==null||"".equals(phoneNum)){
			return null;
		}
		String contactName=null;
        Uri uri = Uri.parse("content://com.android.contacts/data/phones/filter/" + phoneNum);
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{"display_name"}, null, null, null);
        if (cursor.moveToFirst()) {
        	contactName= cursor.getString(0);
        }
        cursor.close();		
		return contactName;
	}
	
	/**
	 * 获得电话号码
	 * 
	 * @param contactId
	 * @return
	 */
	public static void deleteSamePhone(Context mContext, String userName) {
		Cursor cur =mContext.getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI, null,ContactsContract.Contacts.DISPLAY_NAME+"=?" , new String[]{userName}, null);
		if (cur.moveToFirst()) {
			do{
				int iUid = cur.getColumnIndex(ContactsContract.Contacts._ID);
				String sUID = cur.getString(iUid);
				mContext.getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI, ContactsContract.RawContacts.CONTACT_ID+"=?", new String[]{sUID});
			}while(cur.moveToNext());
		}
	}


	/**
	 * 插入电话号码
	 * @return
	 */
	 public static void insertPhoneNumber(Context mContext,String name,String telNumber) {
		//第一步,在raw_contacts表中添加联系人id,raw_contacts代表操作data表中的数据
		//第二部将联系人的各项信息添加到data表中;
		Uri uri=Uri.parse("content://com.android.contacts/raw_contacts");
		ContentResolver resolver=mContext.getContentResolver();
		ContentValues values=new ContentValues(); 
		long contactid=ContentUris.parseId(resolver.insert(uri, values));//可以得到uri的ids
		//添加姓名
		//得到了联系人的id
		uri=Uri.parse("content://com.android.contacts/data");
		values.put("raw_contact_id", contactid);
		values.put("mimetype", "vnd.android.cursor.item/name");
		values.put("data2", name);
		resolver.insert(uri, values);
		//添加电话
		values.clear();
		values.put("raw_contact_id", contactid);
		values.put("mimetype", "vnd.android.cursor.item/phone_v2");
		values.put("data2", "2");
		values.put("data1", telNumber);
		resolver.insert(uri, values);
	 }



	/**
	 * 
	 * @param checkEncryption
	 * @param level
	 */
	private static void getMessageMaxLength(String checkEncryption, int level) {
		if ("E".equals(checkEncryption)) {
			switch (level) {
			case 1:
				MESSAGE_MAX_LENGHTH = 140;
				break;
			case 2:
				MESSAGE_MAX_LENGHTH = 360;
				break;
			case 3:
				MESSAGE_MAX_LENGHTH = 580;
				break;
			case 4:
				MESSAGE_MAX_LENGHTH = 1680;
				break;
			default:
				break;
			}

		} else {
			switch (level) {
			case 1:
				MESSAGE_MAX_LENGHTH = 110;
				break;
			case 2:
				MESSAGE_MAX_LENGHTH = 408;
				break;
			case 3:
				MESSAGE_MAX_LENGHTH = 628;
				break;
			case 4:
				MESSAGE_MAX_LENGHTH = 848;
				break;
			default:
				break;
			}
		}
	}

	public static CardInfo getCardInfo() {
		if (cardInfo == null) {
			cardInfo = new CardInfo();
		}
		return cardInfo;
	}

	public static List<FriendsLocation> getFriendsLocations() {
		if (list == null) {
			list = new ArrayList<FriendsLocation>();
		}
		return list;
	}

	public static void addFriendsLocation(FriendsLocation location) {
		if (list == null) {
			list = new ArrayList<FriendsLocation>();
		}
		for (int i = 0; i < list.size(); i++) {
			FriendsLocation loc = list.get(i);
			if (loc.getUserId().equals(location.getUserId())) {
				list.remove(i);
			}
		}
		list.add(location);
	}

	public static void removeFriendsLocations() {
		if (list != null) {
			for (int index = 0; index < list.size(); index++) {
				list.remove(index);
			}
		}
	}

	/**
	 * 
	 * 把short数据转换成byte数组
	 * 
	 * @param data
	 *            short数据类型
	 * @return byte数组
	 */
	public static byte[] shortToByteArray(short data) {
		/* 定义byte数组 */
		byte[] shortBuf = new byte[2];
		for (int i = 0; i < 2; i++) {
			int offset = (shortBuf.length - 1 - i) * 8;
			// 把高八位和低八位分别放置byte数组
			shortBuf[i] = (byte) ((data >>> offset) & 0xff);
		}
		return shortBuf;
	}
	/**
	 * 将整型转换成byte数组,并且在byte数组中按照数据从低向高的存储数据
	 * @param value
	 * @return
	 */
	public static byte[] shortToByteArray1(short value) {
		byte[] b = new byte[2];
		for (int i = 0; i < 2; i++) {
			int offset = i* 8;
			b[i] = (byte) ((value >>> offset) & 0xFF);
		}
		return b;
	}

	/**
	 * 把Int数据转换成byte数组
	 * 
	 * @param value
	 * @return byte数组
	 */
	public static byte[] intToByteArray(int value) {
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++) {
			int offset = (b.length - 1 - i) * 8;
			b[i] = (byte) ((value >>> offset) & 0xFF);
		}
		return b;
	}

	/**
	 * 将整型转换成byte数组,并且在byte数组中按照数据从低向高的存储数据
	 * @param value
	 * @return
	 */
	public static byte[] intToByteArray1(int value) {
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++) {
			int offset = i* 8;
			b[i] = (byte) ((value >>> offset) & 0xFF);
		}
		return b;
	}
	
	
	/**
	 * 把byte数组转换成short
	 * 
	 * @param b
	 * @return
	 */
	public static final int byteArrayToShort(byte[] b) {
		return (b[0] << 8) + (b[1] & 0xFF);
	}

	/**
	 * 把byte数组转换成int
	 * 
	 * @param b
	 * @return
	 */
	public static final int byteArrayToInt(byte[] b) {
		return (b[0] << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8)
				+ (b[3] & 0xFF);
	}

	/**
	 * 创建ProgressDialog对象
	 * 
	 * @param mContext
	 * @param title
	 *            头信息
	 * @param message
	 *            提示内容
	 * @return
	 */
	public static ProgressDialog createProgressDialog(Context mContext,
			String title, String message) {
		progressDialog = new ProgressDialog(mContext);
		progressDialog.setTitle(title);
		progressDialog.setMessage(message);
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(true);
		return progressDialog;
	}

	public static AlertDialog.Builder createAlertDialog(Context mContext,
			String title, String message) {
		alert = new AlertDialog.Builder(mContext);
		alert.setTitle(title);
		alert.setMessage(message);
		return alert;
	}

	/**
	 * 二进制转换成十六进制字符串
	 * 
	 * @param src
	 * @return
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv + " ");
		}
		return stringBuilder.toString();
	}

	
	public static String bytesToHexString2(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString().toUpperCase();
	}

	
	/**
	 * 
	 * @param src
	 * @return
	 */
	public static String bytesToHexString3(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}		
		for (int i = 0; i < src.length; i++) {
			int v = src[src.length-1-i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}		
		return stringBuilder.toString().toUpperCase();
	}
	
	/**
	 * Convert hex string to byte[]
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.replaceAll(" ", "").toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	
	public static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/**
	 * 判断电文是汉字,代码还是混合
	 * 
	 * @param txt
	 * @return
	 */
	public static int checkMsg(String txt) {
		/* 汉字统计参数 */
		int count = 0;
		int flag = 0;
		/* 正则表达式判断汉字 */
		String regEx = "[\\u4e00-\\u9fa5]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(txt);
		while (m.find()) {
			for (int i = 0; i <= m.groupCount(); i++) {
				count = count + 1;
			}
		}
		/* 包含汉字 */
		if (count != 0) {
			/* 全是汉字 */
			if (count == txt.length()) {
				flag = 0;
				System.out.println("汉字!");
			} else {
				/* 混合 */
				flag = 2;
				System.out.println("混合");
			}
		} else {
			/* 不包含汉字 */
			// flag=1;
			// System.out.println("代码");
			String reg = "[0-9,A,B,C,D,E,F]*";
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher = pattern.matcher(txt);
			if (matcher.matches()) {
				flag = 1;  //字符
			} else {
				flag = 2;
			}
		}
		return flag;
	}

	/**
	 * 获得电话号码
	 * 
	 * @param contactId
	 * @return
	 */
	public static String getPhoneContacts(Context mContext, String contactId) {
		Cursor cursor = null;
		String name = "";
		try {
			Uri uri = Phone.CONTENT_URI;
			cursor = mContext.getContentResolver().query(uri, null,
					Phone.CONTACT_ID + "=?", new String[] { contactId }, null);
			if (cursor.moveToFirst()) {
				name = cursor.getString(cursor
						.getColumnIndex(Phone.DISPLAY_NAME));
			} else {
				Toast.makeText(mContext, "No contact found.", Toast.LENGTH_LONG)
						.show();
			}
		} catch (Exception e) {
			name = "";
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return name;
	}

	/**
	 * 获得Name
	 * 
	 * @param mContext
	 * @param cursor
	 * @return
	 */
	public static String getPhoneName(Context mContext, Cursor cursor) {
		return cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME));
	}

	/**
	 * 2.0固件以后的版本，获取联系人信息
	 * 
	 * @return null未取到信息 / 非null,取到信息
	 */
	private static  HashMap<String, String> getContentNew(Context mContext) {
		String[] projection = { ContactsContract.PhoneLookup.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.NUMBER };
		HashMap<String, String> map=new HashMap<String,String>();
		// 将自己添加到 msPeers 中
		Cursor cursor = mContext.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				projection, null, // WHERE clause.
				null, // WHERE clause value substitution
				null); // Sort order.
		if (cursor == null) {
			return null;
		}
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			// 取得联系人名字
			int nameFieldColumnIndex = cursor
					.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
			String name = cursor.getString(nameFieldColumnIndex);
			String number=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			map.put(number.replaceAll(" ", ""), name);
			Log.i("Contacts", "" + name + " .... " + number.replaceAll(" ", "")); // 这里提示
		}
		return map;
	}

	
	public static String showTwoBitNum(int num) {
		String str = String.valueOf(num);
		if (num < 10) {
			str = "0" + str;
		}
		return str;
	}

	/**
	 * 今天是周几
	 * 
	 * @param weekday
	 * @return
	 */
	public static String getCurrentWeekDay(int weekday) {
		String patten = "日一二三四五六";
		return String.valueOf(patten.charAt(weekday));
	}

	/**
	 * 增加卫星颗数
	 * 
	 * @param data
	 */
	// public static void addSatellite(BDSatelliteData data) {
	// if (list != null) {
	// list.add(data);
	// sortData();
	// }
	// }

	// public static void removeAllSatellite() {
	// if (list != null) {
	// list.clear();
	// }
	// }

	// public static BDSatelliteData getSatelliteById(String satelliteId) {
	// if (satelliteId == null || satelliteId.equals("0")) {
	// return null;
	// }
	// for (int i = 0; i < list.size(); i++) {
	// BDSatelliteData data = list.get(i);
	// if (satelliteId.equals(data.getSatelliteId() + "")) {
	// return data;
	// }
	// }
	// return null;
	// }

	// public static void sortData() {
	// // 对集合对象进行排序
	// StepComparator comparator = new StepComparator();
	// Collections.sort(list, comparator);
	// for (int i = 0; i < list.size(); i++) {
	// BDSatelliteData data = list.get(i);
	// }
	// }

	/**
	 * 获得所有的卫星数据
	 * 
	 * @return
	 */
	// public static List<BDSatelliteData> getSatData() {
	// return list;
	// }

	// public static void addLocalInfo(LocalCardInfo card){
	// info=card;
	// }
	// public static LocalCardInfo getLocalInfo(){
	// if(info==null){
	// info=new LocalCardInfo();}
	// return info;
	// }

	// public static void removeLocalInfo(){
	// info=null;
	// }
	public static String getPromptInfo(String Key) {
		String prompt = "";
		if (Key.equals("DWSQ")) {
			prompt = "定位申请";
		} else if (Key.equals("TXSQ")) {
			prompt = "通讯申请";
		}
		return prompt;
	}

	public static int dip2px(Context context, double dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(Context context, double pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 判断字符串是否是整数
	 * 
	 * @param str
	 * @return
	 */
	public static boolean checkNum(String str) {
		return str.matches("[0-9]+");
	}

	public static boolean checDoublekNum(String str) {
		return str.matches("[0-9.]+");
	}

	public static int getProgressValue(double value) {
		return (int) (value - (value / 3)) + 2;
	}

	/**
	 * 验证北斗是否打开
	 * 
	 * @param mContext
	 * @retun
	 */
	// public static boolean CheckGPS(Context mContext){
	// boolean bRes=false;
	// LocationManager
	// locManager=(LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
	// if(locManager.isProviderEnabled()){
	// bRes=true;
	// }
	// return bRes;
	// }

	// 测量两点之间的距离
	public static double Distance(double dblLong1, double dblLat1 /* 经纬度1 */,
			double dblLong2, double dblLat2/* 经纬度2 */) {
		double iResult = 0;

		double dblX1, dblY1;
		double dblX2, dblY2;
		// 转换成高斯坐标
		CoodrinateDate data1 = LBToGSXY(dblLong1, dblLat1, 0);
		CoodrinateDate data2 = LBToGSXY(dblLong2, dblLat2, 0);

		dblX1 = data1.getLon();
		dblY1 = data1.getLat();
		dblX2 = data2.getLon();
		dblY2 = data2.getLat();
		// 勾股定理
		iResult = Math.sqrt((dblX1 - dblX2) * (dblX1 - dblX2) + (dblY1 - dblY2)
				* (dblY1 - dblY2));
		return iResult;
	}

	public static double Distance(double dblLong1, double dblLat1 /* 经纬度1 */,
			short sHigh1, /* 高程1 */
			double dblLong2, double dblLat2/* 经纬度2 */, short sHigh2 /* 高程2 */) {
		double iResult = 0;
		double iSimple = Distance(dblLong1, dblLat1, dblLong2, dblLat2);
		double iValue = iSimple * iSimple + (sHigh2 - sHigh1)
				* (sHigh2 - sHigh1);
		iResult = Math.sqrt(iValue);
		return iResult;
	}

	/* 将经纬度坐标转换成高斯平面坐标 */
	public static CoodrinateDate LBToGSXY(double dblLong /* in 经度 */,
			double dblLat/* in 纬度 */, double dblHigh/* 高程 */) {
		return DD_GOSS(dblLong, dblLat, dblHigh, 0);
	}

	public static LocationParam LBToGOSS(double dblLong /* in 经度 */,
			double dblLat/* in 纬度 */, double dblHigh/* 高程 */) {
		return DD_GOSS1(dblLong, dblLat, dblHigh, 0);
	}

	/*
	 * **************************************************************************************************
	 * - 函数名称 : DD_GOSS(FP32 L, FP32 B, FP32 H, FP32 gcycz) - 函数说明 : 大地坐标转高斯坐标 -
	 * 输入参数 : L:经度;B:纬度;H:高程;gcycz:高程异常值 - 输出参数 : 无
	 * ******************************
	 * ********************************************************************
	 */
	public static LocationParam DD_GOSS1(double L /* in 经度 */
	, double B/* in 纬度 */, double H /* 高程 */, double gcycz /* 高程异常 */) {
		double SINB, COSB;
		/* SINB=sinB,COSB=cosB */
		double t;
		/* t=tgB */
		double e, ns;
		/* e=(a*a-b*b)/(a*a),ns is n */
		double ls, NL;
		/* ls=l"/p", N */
		double XL;
		/* X */
		double dataf1, dataf2, dataf3, dataf4, dataf5;
		/* temp FP32 data */
		double a;
		double f;
		// 54坐标系
		// if (zbx == 0)
		// {
		// a=6378245 ;
		// /*a*/
		// f=0.0033523 ;
		// }
		// //84坐标系
		// else
		// {
		a = 6378137;
		/* a */
		f = 0.003352810664;
		// }
		// a=6378149 ; /* a */
		// f=0.0033528 ;/* f */
		/* f */
		dataf1 = B * PI / 180.0;
		SINB = Math.sin(dataf1);
		COSB = Math.cos(dataf1);
		dataf2 = SINB / COSB;
		t = dataf2 * dataf2;
		/* t=t^2=square(tgB); */

		dataf1 = a * (1.0 - f);
		/* b */
		e = (a * a - dataf1 * dataf1) / (a * a);
		/* square(e) */

		dataf2 = Math.sqrt((a * a - dataf1 * dataf1) / (dataf1 * dataf1));
		/* e' */
		dataf3 = dataf2 * COSB;
		ns = dataf3 * dataf3;
		/* square(n) */

		dataf1 = Math.floor(L / 6) * 6 + 3;
		/* L0 */
		dataf2 = L - dataf1;
		ls = dataf2 * PI / 180.0;
		/* ls=l"/p" */

		dataf1 = Math.sqrt(1.0 - e * SINB * SINB);
		NL = a / dataf1;
		/* N */

		dataf1 = 1.0 + e * 3.0 / 4.0;
		dataf1 += e * e * 45.0 / 64.0;
		dataf1 += e * e * e * 175.0 / 256.0;
		dataf1 += e * e * e * e * 11025.0 / 16384.0;
		/* ~A */

		dataf2 = e * 3.0 / 4.0;
		dataf2 += e * e * 15.0 / 16.0;
		dataf2 += e * e * e * 525.0 / 512.0;
		dataf2 += e * e * e * e * 2205.0 / 2048.0;
		/* ~B */

		dataf3 = e * e * 15.0 / 64.0;
		dataf3 += e * e * e * 105.0 / 256.0;
		dataf3 += e * e * e * e * 2205.0 / 4096.0;
		/* ~C */

		dataf4 = e * e * e * 35.0 / 512.0;
		dataf4 += e * e * e * e * 315.0 / 2048.0;
		/* ~D */

		dataf5 = e * e * e * e * 315.0 / 16384.0;
		/* ~E */

		XL = dataf1 * B * PI / 180.0;
		XL -= dataf2 * SINB * COSB;
		XL += dataf3 * SINB * COSB * (2.0 * COSB * COSB - 1.0);
		XL -= dataf4 / 3.0 * (3.0 * SINB - 4.0 * SINB * SINB * SINB)
				* (4.0 * COSB * COSB * COSB - 3.0 * COSB);
		XL *= a;
		XL *= 1.0 - e;
		/* XL */

		dataf1 = XL;
		dataf1 += NL * ls * ls * SINB * COSB / 2.0;
		dataf2 = 5.0 - t + 9.0 * ns + 4.0 * ns * ns;
		dataf3 = NL * ls * ls * ls * ls * SINB * COSB * COSB * COSB / 24.0;
		dataf1 += dataf3 * dataf2;
		dataf2 = 61.0 - 58.0 * t + t * t;
		dataf3 = NL * Math.pow(ls, 6.0) * SINB * Math.pow(COSB, 5.0) / 720.0;
		dataf1 += dataf3 * dataf2;
		/* x */
		dataf4 = NL * ls * COSB;
		dataf2 = NL * ls * ls * ls * COSB * COSB * COSB * (1 - t + ns) / 6.0;
		dataf4 += dataf2;
		dataf3 = ls * COSB;
		dataf2 = dataf3 * dataf3 * dataf3 * dataf3 * dataf3;
		dataf3 = NL * ls * dataf2 / 120.0;
		dataf2 = 5 - 18 * t + t * t + 14 * ns - 58 * ns * t;
		dataf4 += dataf3 * dataf2;

		dataf4 += 500000.0;
		dataf2 = Math.floor(L / 6) + 1;
		dataf2 *= 1000000.0;
		dataf4 += dataf2;
		/* y */
		dataf3 = H - gcycz;
		/* H */
		LocationParam data = new LocationParam();
		data.setmLat(String.valueOf(dataf1));
		data.setmLon(String.valueOf(dataf4));
		data.setmHeight(String.valueOf(dataf3));
		return data;
	}

	/*
	 * **************************************************************************************************
	 * - 函数名称 : DD_GOSS(FP32 L, FP32 B, FP32 H, FP32 gcycz) - 函数说明 : 大地坐标转高斯坐标 -
	 * 输入参数 : L:经度;B:纬度;H:高程;gcycz:高程异常值 - 输出参数 : 无
	 * ******************************
	 * ********************************************************************
	 */
	public static CoodrinateDate DD_GOSS(double L /* in 经度 */
	, double B/* in 纬度 */, double H /* 高程 */, double gcycz /* 高程异常 */) {
		double SINB, COSB;
		/* SINB=sinB,COSB=cosB */
		double t;
		/* t=tgB */
		double e, ns;
		/* e=(a*a-b*b)/(a*a),ns is n */
		double ls, NL;
		/* ls=l"/p", N */
		double XL;
		/* X */
		double dataf1, dataf2, dataf3, dataf4, dataf5;
		/* temp FP32 data */
		double a;
		double f;
		// 54坐标系
		// if (zbx == 0)
		// {
		// a=6378245 ;
		// /*a*/
		// f=0.0033523 ;
		// }
		// //84坐标系
		// else
		// {
		a = 6378137;
		/* a */
		f = 0.003352810664;
		// }
		// a=6378149 ; /* a */
		// f=0.0033528 ;/* f */
		/* f */
		dataf1 = B * PI / 180.0;
		SINB = Math.sin(dataf1);
		COSB = Math.cos(dataf1);
		dataf2 = SINB / COSB;
		t = dataf2 * dataf2;
		/* t=t^2=square(tgB); */

		dataf1 = a * (1.0 - f);
		/* b */
		e = (a * a - dataf1 * dataf1) / (a * a);
		/* square(e) */

		dataf2 = Math.sqrt((a * a - dataf1 * dataf1) / (dataf1 * dataf1));
		/* e' */
		dataf3 = dataf2 * COSB;
		ns = dataf3 * dataf3;
		/* square(n) */

		dataf1 = Math.floor(L / 6) * 6 + 3;
		/* L0 */
		dataf2 = L - dataf1;
		ls = dataf2 * PI / 180.0;
		/* ls=l"/p" */

		dataf1 = Math.sqrt(1.0 - e * SINB * SINB);
		NL = a / dataf1;
		/* N */

		dataf1 = 1.0 + e * 3.0 / 4.0;
		dataf1 += e * e * 45.0 / 64.0;
		dataf1 += e * e * e * 175.0 / 256.0;
		dataf1 += e * e * e * e * 11025.0 / 16384.0;
		/* ~A */

		dataf2 = e * 3.0 / 4.0;
		dataf2 += e * e * 15.0 / 16.0;
		dataf2 += e * e * e * 525.0 / 512.0;
		dataf2 += e * e * e * e * 2205.0 / 2048.0;
		/* ~B */

		dataf3 = e * e * 15.0 / 64.0;
		dataf3 += e * e * e * 105.0 / 256.0;
		dataf3 += e * e * e * e * 2205.0 / 4096.0;
		/* ~C */

		dataf4 = e * e * e * 35.0 / 512.0;
		dataf4 += e * e * e * e * 315.0 / 2048.0;
		/* ~D */

		dataf5 = e * e * e * e * 315.0 / 16384.0;
		/* ~E */

		XL = dataf1 * B * PI / 180.0;
		XL -= dataf2 * SINB * COSB;
		XL += dataf3 * SINB * COSB * (2.0 * COSB * COSB - 1.0);
		XL -= dataf4 / 3.0 * (3.0 * SINB - 4.0 * SINB * SINB * SINB)
				* (4.0 * COSB * COSB * COSB - 3.0 * COSB);
		XL *= a;
		XL *= 1.0 - e;
		/* XL */

		dataf1 = XL;
		dataf1 += NL * ls * ls * SINB * COSB / 2.0;
		dataf2 = 5.0 - t + 9.0 * ns + 4.0 * ns * ns;
		dataf3 = NL * ls * ls * ls * ls * SINB * COSB * COSB * COSB / 24.0;
		dataf1 += dataf3 * dataf2;
		dataf2 = 61.0 - 58.0 * t + t * t;
		dataf3 = NL * Math.pow(ls, 6.0) * SINB * Math.pow(COSB, 5.0) / 720.0;
		dataf1 += dataf3 * dataf2;
		/* x */
		dataf4 = NL * ls * COSB;
		dataf2 = NL * ls * ls * ls * COSB * COSB * COSB * (1 - t + ns) / 6.0;
		dataf4 += dataf2;
		dataf3 = ls * COSB;
		dataf2 = dataf3 * dataf3 * dataf3 * dataf3 * dataf3;
		dataf3 = NL * ls * dataf2 / 120.0;
		dataf2 = 5 - 18 * t + t * t + 14 * ns - 58 * ns * t;
		dataf4 += dataf3 * dataf2;

		dataf4 += 500000.0;
		dataf2 = Math.floor(L / 6) + 1;
		dataf2 *= 1000000.0;
		dataf4 += dataf2;
		/* y */
		dataf3 = H - gcycz;
		/* H */
		CoodrinateDate data = new CoodrinateDate();
		data.setLat(dataf1);
		data.setLon(dataf4);
		data.setHeight(dataf3);

		return data;
	}

	public static LocationParam DD_KJZJ(double L, double B, double H) {
		double dataf1, dataf2, dataf3, dataf4;
		// 84
		/*
		 * a= 6378137; f=0.003352810664
		 * 
		 * 
		 * //54 a = 6378245 f = 0.0033523298692
		 */

		// //54 zuobiaoxi
		// if (zbx == 0)
		// {
		// dataf3=6378245.0 ; /* a */
		// dataf1=0.0033523 ;/* f */
		// }
		// //84 zuobiaoxi
		// else
		// {
		dataf3 = 6378137.0; /* a */
		dataf1 = 0.003352810664;/* f */
		// }

		dataf2 = (1.0 - dataf1) * dataf3;/*
										 * b = (1-0.0033523)*6378245 =
										 * 6356863.2092865
										 */

		dataf1 = dataf3 * dataf3 - dataf2 * dataf2; /*
													 * a^2-b^2 = 6378245*6378245
													 * - 6356863.2092865*
													 * 6356863.2092865 =
													 * 272299418444.73970016091775
													 */
		dataf1 /= dataf3 * dataf3; /* (a^2-b^2)/a^2 = 0.00669336208471 */
		/* dataf1=e^2 */
		dataf2 = B * PI / 180.0;
		dataf4 = Math.sin(dataf2);
		/* sin(B) */
		dataf2 = dataf3 / Math.sqrt(1.0 - dataf1 * dataf4 * dataf4);
		/* N */

		dataf3 = (dataf2 * (1.0 - dataf1) + H) * dataf4;
		/* Z */
		dataf1 = (dataf2 + H) * Math.cos(B * PI / 180.0)
				* Math.cos(L * PI / 180.0);
		/* X */
		dataf4 = (dataf2 + H) * Math.cos(B * PI / 180.0)
				* Math.sin(L * PI / 180.0);
		/* Y */

		// /* Z */
		// dblX = dataf1;
		// dblY = dataf4;
		// dblZ = (dataf3);
		LocationParam coodrinate = new LocationParam();
		coodrinate.setmLon(String.valueOf(dataf1));
		coodrinate.setmLat(String.valueOf(dataf4));
		coodrinate.setmHeight(String.valueOf(dataf3));
		return coodrinate;
	}

	/**
	 * 将经纬度转换为麦卡托平面坐标
	 * 
	 * @param L
	 * @param B
	 * @param H
	 * @param gcycz
	 */
	// void DD_MCTOR(FP32 L, FP32 B, FP32 H, FP32 gcycz)
	public static LocationParam DD_MCTOR(double L, double B, double H,
			double gcycz) {
		double dataf1, dataf2, dataf3;
		double lk, r0, U;

		dataf1 = L / 6.0;
		dataf2 = Math.floor(dataf1) * 6.0;
		dataf2 += 3.0;
		/* L0 */
		lk = (L - dataf2) * PI / 180.0;

		// if (zbx == 0)
		// {
		// r0=6378245 ;
		// /* a */
		// dataf2=0.0033523 ;
		//
		// }
		// else
		// {
		r0 = 6378137;
		/* a */
		dataf2 = 0.003352810664;
		// }
		/* f */
		dataf1 = r0 * (1 - dataf2);
		/* b */
		dataf2 = (r0 * r0 - dataf1 * dataf1) / (r0 * r0);
		/* e^2 */
		r0 = Math.cos(PI / 6.0);
		// //54×ø±êÏµ
		// if (zbx == 0)
		// {
		// r0=6378245*r0/Math.sqrt(1-dataf2*Math.sin(PI/6.0)*Math.sin(PI/6.0));
		// }
		// //84×ø±êÏµ
		// else{
		r0 = 6378137
				* r0
				/ Math.sqrt(1 - dataf2 * Math.sin(PI / 6.0)
						* Math.sin(PI / 6.0));
		// }

		dataf3 = Math.sqrt(dataf2);
		/* e */
		dataf2 = Math.sin(B * PI / 180.0);
		U = (1 - dataf3 * dataf2) / (1 + dataf3 * dataf2);
		dataf1 = Math.sqrt(U);
		U = Math.pow(dataf1, dataf3);
		dataf2 = Math.tan(B * PI / 360.0);
		/* tgB/2 */
		dataf1 = (1.0 + dataf2) / (1.0 - dataf2);
		U *= dataf1;
		// dblX = r0*log(U);
		// dblY = r0*lk;
		// dblZ = H - gcycz;

		LocationParam coodrinate = new LocationParam();
		coodrinate.setmLon(String.valueOf(r0 * Math.log(U)));
		coodrinate.setmLat(String.valueOf(r0 * lk));
		coodrinate.setmHeight(String.valueOf(H - gcycz));
		return coodrinate;
	}

	/**
	 * 将字符串坐标系转换成数值
	 * 
	 * @param str
	 * @return
	 */
	public static double getCoodrinate(String str) {
		if (str == null || "".equals(str) || !checkCoodrinate(str)) {
			return 0;
		}
		int degree = 0;
		int min = 0;
		int second = 0;
		double result = 0;

		String[] temp = str.split("°");
		degree = Integer.valueOf(temp[0].equals("") ? "0" : temp[0]);
		String[] temp1 = temp[1].split("′");
		min = Integer.valueOf(temp1[0].equals("") ? "0" : temp1[0]);
		second = Integer.valueOf(temp1[1].split("″")[0].equals("") ? "0"
				: temp1[1].split("″")[0]);
		result = degree + min / 60.0 + second / (60.0 * 60.0);
		return result;
	}

	/*
	 * 判断是否是坐标类型
	 */
	public static boolean checkCoodrinate(String str) {
		return str.matches("[0-9°′″]+");
	}

	/**
	 * 将字符串坐标系转换成数值
	 * 
	 * @param str
	 * @return
	 */
	public static byte[] getCoodrinate1(String str) {
		if (str == null || "".equals(str) || !checkCoodrinate(str)) {
			return null;
		}
		byte degree = 0;
		byte min = 0;
		byte second = 0;
		byte[] result = new byte[4];
		String[] temp = str.split("°");
		if (temp.length > 2) {
			degree = Byte.valueOf(temp[0].equals("") ? "0" : temp[0]);
			String[] temp1 = temp[1].split("′");
			if (temp1.length > 2) {
				min = Byte.valueOf(temp1[0].equals("") ? "0" : temp1[0]);
				second = Byte.valueOf(temp1[1].split("″")[0].equals("") ? "0"
						: temp1[1].split("″")[0]);
			}
		}
		result[0] = degree;
		result[1] = min;
		result[2] = second;
		result[3] = 0;

		return result;
	}

	/**
	 * 将经纬度转换成yyyyy.yy 或者 llll.ll
	 * 
	 * @param lon
	 * @return
	 */
	public static double translateLonLat(double num) {
		if (num == 0.0) {
			return 0.0;
		}
		String str = String.valueOf(num);
		String[] temp = str.split("\\.");
		double high = Double.valueOf(temp[0]);
		double low = Double.valueOf("0." + temp[1]);
		return high * 100 + low * 60;
	}

	/**
	 * 将经度yyyyy.yy转换成yyy.yyyy
	 * 
	 * @param str
	 * @return
	 */
	public static String getLon(String str) {
		int degree = Integer.valueOf(str.substring(0, 3));
		double num = Double.valueOf(str.substring(3));
		double second = num / 60.0d;
		return String.valueOf(degree + second);
	}

	/**
	 * 将经度llll.ll转换成ll.lll
	 * 
	 * @param str
	 * @return
	 */
	public static String getLat(String str) {
		int degree = Integer.valueOf(str.substring(0, 2));
		double num = Double.valueOf(str.substring(2)).doubleValue();
		double second = num / 60.0d;
		return String.valueOf(degree + second);
	}

	/**
	 * 转换经纬度
	 * @return
	 */
	public static String lonLatTranslater(String str) {
		if (str == null || "".equals(str)) {
			return null;
		}
		int degee = 0;
		int minute = 0;
		String[] temp = str.split("\\.");
		if (temp[0].length() == 5) {
			degee = Integer.valueOf(temp[0].substring(0, 3));
			minute = Integer.valueOf(temp[0].substring(3, 5));
		} else if (temp[0].length() == 4) {
			degee = Integer.valueOf(temp[0].substring(0, 2));
			minute = Integer.valueOf(temp[0].substring(2, 4));
		}
		String second = "0." + temp[1];
		DecimalFormat df = new DecimalFormat(".##");
		String sec = df.format(Double.valueOf(second) * 60);
		return (degee + "°" + minute + "′" + sec + "″");
	}

	public static String getTime(String time) {
		String hour = time.substring(0, 2);
		String minute = time.substring(2, 4);
		String second = time.substring(4, 6);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String date = sdf.format(new Date());
		return (date + " " + hour + ":" + minute + ":" + second);
	}

	/**
	 * 转换经纬度
	 * 
	 * @param lon
	 * @param lat
	 * @param height
	 * @return
	 */
	public static LocationParam translate(double lon, double lat,
			double height, int flag) {
		LocationParam coodrinate = null;
		switch (flag) {
		case 0: // 大地坐标
			coodrinate = new LocationParam();
			String mlon = String.valueOf(lon);
			coodrinate.setmLon(mlon);
			String mlat = String.valueOf(lat);
			coodrinate.setmLat(mlat);
			coodrinate.setmHeight(String.valueOf(height));
			break;
		case 1: // 高斯平面坐标
			coodrinate = LBToGOSS(Double.valueOf(getLat(String.valueOf(lon))),
					Double.valueOf(getLat(String.valueOf(lat))), height);
			break;
		case 2: // 麦卡托平面坐标
			coodrinate = DD_MCTOR(Double.valueOf(getLat(String.valueOf(lon))),
					Double.valueOf(getLat(String.valueOf(lat))), height, 0);
			break;
		case 3: // 空间直角坐标
			coodrinate = DD_KJZJ(Double.valueOf(getLat(String.valueOf(lon))),
					Double.valueOf(getLat(String.valueOf(lat))), height);
			break;
			
		case 4: // beijing54坐标
			//TODO
//			coodrinate = DD_KJZJ(Double.valueOf(getLat(String.valueOf(lon))),
//					Double.valueOf(getLat(String.valueOf(lat))), height);
			
			/*bj54坐标系*/
			//coodrinate=new BDLocation();
			coodrinate = new LocationParam();
			//double[] bj54lonlat=transWGS84ToBj54(bdlocation.mLongitude, bdlocation.mLatitude);
			double[] bj54lonlat=transWGS84ToBj54(lon, lat);
			//coodrinate.setLongitude(bj54lonlat[0]);
			coodrinate.setmLon(String.valueOf(bj54lonlat[0]));
			//coodrinate.setLatitude(bj54lonlat[1]);
			coodrinate.setmLat(String.valueOf(bj54lonlat[1]));
			//coodrinate.setEarthHeight(bdlocation.getEarthHeight());
			coodrinate.setmHeight(String.valueOf(height));
			
			break;
		default:
			break;
		}
		return coodrinate;
	}

	/**
	 * 判断字符串的占的bit数，汉字占用14bit ，字符数字占用4bit
	 * 
	 * @param str
	 * @return
	 */
	public static int checkStrBits(String str) {
		int chineseNum = getLenOfString(str);
		int num = str.toCharArray().length;
		//return (num - chineseNum) * 4 + (chineseNum * 14);
		return (num - chineseNum) * 8 + (chineseNum * 16);
	}

	/**
	 * 创建ProgressDialog对象
	 * 
	 * @param mContext
	 * @param message
	 * @return
	 */
	public ProgressDialog createProgressDialog(Context mContext, String message) {
		progressDialog = new ProgressDialog(mContext);
		progressDialog.setMessage(message);
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(true);
		return progressDialog;
	}

	public static int getLenOfString(String str) {
		// 汉字个数
		int chCnt = 0;
		String regEx = "[\\u4e00-\\u9fa5]"; // 如果考虑繁体字，u9fa5-->u9fff
		java.util.regex.Pattern p = java.util.regex.Pattern.compile(regEx);
		java.util.regex.Matcher m = p.matcher(str);
		while (m.find()) {
			chCnt++;
		}
		return chCnt;
	}

	/**
	 * 将yyy.yyy或ll.lll转换成yyyyy.yy或llll.ll
	 * 
	 * @param num
	 * @return
	 */
	public static double changeLonLatDegreeToMinute(double num) {
		String temp = String.valueOf(num);
		String[] arr = temp.split("\\.");
		if (arr.length == 2) {
			double temp_zhengshu = Double.valueOf(arr[0]).doubleValue();
			double temp_xiaoshu = Double.valueOf("0." + arr[1]).doubleValue();
			double changedNum = (temp_xiaoshu * 60.0d)
					+ (temp_zhengshu * 100.0d);
			return changedNum;
		} else {
			// 格式错误
			return 0.0d;
		}
	}

	/**
	 * 将经纬度的单位度转换成秒
	 * 
	 * @param num
	 * @return
	 */
	public static double changeLonLatDegreeToSecond(double num) {
		String lonLat = String.valueOf(num);
		String[] lonlat_arr = lonLat.split("\\.");
		if (lonlat_arr.length == 2) {
			double lonlat_zhengshu = Double.valueOf(lonlat_arr[0])
					.doubleValue();
			double lonlat_xiaoshu = Double.valueOf("0." + lonlat_arr[1])
					.doubleValue();
			double temp_minute = lonlat_xiaoshu * 60.0d;
			String[] minute_arr = String.valueOf(temp_minute).split("\\.");
			if (minute_arr.length == 2) {
				double changedNum = (lonlat_zhengshu * 10000.0d)
						+ (Double.valueOf(minute_arr[0]).doubleValue() * 100.0d)
						+ (Double.valueOf("0." + minute_arr[1]).doubleValue() * 60.0d);
				return changedNum;
			} else {
				Log.i("Utils", "changeLonLatDegreeToSecond()参数格式错误!");
				return 0.0d;
			}
		} else {
			Log.i("Utils", "changeLonLatDegreeToSecond()参数格式错误!");
			return 0.0d;
		}
	}

	/**
	 * 把经纬度单位为秒的格式转换为度
	 * 
	 * @param num
	 * @return
	 */
	public static double changeLonLatSecondUnitToDegree(double num) {
		String lonlat = String.valueOf(num);
		String[] array = lonlat.split("\\.");
		if (array.length == 2) {
			double lonlat_xiaoshu = Double.valueOf("0." + array[1]);
			String lonlat_zhengshu = array[0];
			int len = lonlat_zhengshu.length();
			double changedNum = 0.0d;
			if (len > 5) {
				changedNum = Double.valueOf(
						lonlat_zhengshu.substring(0, len - 5)).doubleValue()
						+ (Double.valueOf(
								lonlat_zhengshu.substring(len - 5, len - 3))
								.doubleValue() / 60.0d)
						+ (Double.valueOf(lonlat_zhengshu.substring(len - 3))
								.doubleValue() / 3600.0d);
			}
			return changedNum;
		} else {
			Log.i("Utils", "changeLonLatSecondUnitToDegree()参数格式错误!");
			return 0.0d;
		}
	}
     
	
	/**
	 * 设置Activity的背景
	 */
	public static void setActivityBackgroud(Context mContext,LinearLayout layout){
		/* 增加背景图片 */
		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
				R.drawable.bg);
		BitmapDrawable bd = new BitmapDrawable(bitmap);
		bd.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		bd.setDither(true);
		layout.setBackgroundDrawable(bd);
	}
	
	// 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
	public static void acquireWakeLock(Context mContext) {
		
		if (null == wakeLock) {
			PowerManager pm = (PowerManager) mContext
					.getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PostLocationService");
			if (null != wakeLock) {
				wakeLock.acquire();
				Log.i("Utils","------------------------------->acquireWakeLock()");
			}
		}
	}

	
	// 释放设备电源锁
	public static void releaseWakeLock() {
		if (null != wakeLock) {
			wakeLock.release();
			wakeLock = null;
		}
	}
	
	/**
	 * 对字符串进行CRC校验
	 * @param messageBodyStr
	 */
	public static String getCRCMessage(int flag,String messageBodyStr){
		byte[] content=null;
		try {
			content = messageBodyStr.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		byte[] allContent=new byte[content.length+5];
		allContent[0]=(byte)0xAA;
		allContent[1]=(byte)flag;
		System.arraycopy(content, 0, allContent, 2, content.length);
		allContent[content.length+2]=0;
//		byte bCrc=allContent[1];
//		for(int i=2;i<allContent.length-2;i++){
//			bCrc=(byte)(bCrc^allContent[i]);
//		}		
		allContent[content.length+3]=getCRC(allContent,1,allContent.length-2);
		allContent[content.length+4]=(byte)0xFF;
		String hex=bytesToHexString2(allContent);
		return hex;
	}
	
	
	/**
	 * 获得北斗位置报告CRC校验后的发送命令
	 * @param locationReport
	 * @return
	 */
	public static String getCRCLocationReport(BDLocationReport locationReport){
		byte[] report=new byte[27];
		report[0]=(byte)0xAA;
		report[1]=0x01;
		/*时间处理*/
		Calendar mCalendar=Calendar.getInstance();								
		byte[] time=intToByteArray1(mCalendar.get(Calendar.HOUR_OF_DAY)*3600+
				 mCalendar.get(Calendar.MINUTE)*60+
				 mCalendar.get(Calendar.SECOND));
		byte[] longtidute=intToByteArray1((int)(locationReport.getLongitude()*100000));
		byte[] latitude=intToByteArray1((int)(locationReport.getLatitude()*100000));
        byte[] speed=shortToByteArray1((short)((LOCATION_REPORT_SPEED/1.852d)*100));
        byte[] bearing=shortToByteArray1((short)(LOCATION_REPORT_BEARING*100));
        
        System.arraycopy(time, 0, report, 2, time.length);
        System.arraycopy(longtidute, 0, report, 6, longtidute.length);
        System.arraycopy(latitude, 0, report, 10, latitude.length);
        System.arraycopy(speed, 0, report, 14, speed.length);
        System.arraycopy(bearing, 0, report, 16, bearing.length);
        
		if(LOCATION_STATUS){
		    report[18]=0x01;
		}else{
			report[18]=0x00;
		}
		for(int i=1;i<=6;i++){
			report[18+i]=0x00;
		}
		report[25]=getCRC(report,1,report.length-2);
		report[26]=(byte)0xFF;	
		String hex=bytesToHexString2(report);
		Log.i("Utils", hex);
		return hex;	
	}
	
	/**
	 * 获得北斗位置报告CRC校验后的发送命令
	 * @param locationReport
	 * @return
	 */
	public static String getPuShiCRCLocationReport(List<BDLocationReportImp> locationReports,int timeFreq,int locationNum){
		if(locationReports==null)
			return "";
		byte[] report=new byte[12+13*locationNum];
		report[0]=(byte)0x00;
		report[1]=(byte)0xF1;
		String dateStr=locationReports.get(0).getReportTime();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date=null;
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Calendar mCalendar=Calendar.getInstance();
		mCalendar.setTime(date);
		int year=mCalendar.get(Calendar.YEAR);
		int month=mCalendar.get(Calendar.MONTH)+1;
		int day=mCalendar.get(Calendar.DAY_OF_MONTH);
		int hour=mCalendar.get(Calendar.HOUR_OF_DAY);
		int minute=mCalendar.get(Calendar.MINUTE);
		int second=mCalendar.get(Calendar.SECOND);
		report[2]=(byte)(year-2000);
		report[3]=(byte)month;
		report[4]=(byte)day;
		report[5]=(byte)hour;
		report[6]=(byte)minute;
		report[7]=(byte)second;
		report[8]=(byte)locationNum;
		short locationTimeFreq=(short)timeFreq;
		byte[] temp=Utils.shortToByteArray(locationTimeFreq);
		report[9]=temp[0];
		report[10]=temp[1];
		for(int i=0;i<locationReports.size();i++){
			BDLocationReportImp bdLocationReportImp=locationReports.get(i);
			double lon=bdLocationReportImp.getLongitude();
			byte lonDegressByte=(byte)lon;
			double lonMinuteDouble=(lon-lonDegressByte)*60;
			byte lonMinuteByte=(byte)lonMinuteDouble;
			double lonSecondDouble=(lonMinuteDouble-lonMinuteByte)*60;
			byte lonSecondByte=(byte)lonSecondDouble;
			byte lonMillSecond=(byte)((lonSecondDouble-lonSecondByte)*10);
			report[11+13*i]='E';
			report[12+13*i]=lonDegressByte;
			report[13+13*i]=lonMinuteByte;
			report[14+13*i]=lonSecondByte;
			report[15+13*i]=lonMillSecond;
			double lat=bdLocationReportImp.getLatitude();
			byte latDegressByte=(byte)lat;
			double latMinuteDouble=(lat-latDegressByte)*60;
			byte latMinuteByte=(byte)latMinuteDouble;
			double latSecondDouble=(latMinuteDouble-latMinuteByte)*60;
			byte latSecondByte=(byte)latSecondDouble;
			byte latMillSecond=(byte)((latSecondDouble-latSecondByte)*60);
			report[16+13*i]='N';
			report[17+13*i]=latDegressByte;
			report[18+13*i]=latMinuteByte;
			report[19+13*i]=latSecondByte;
			report[20+13*i]=latMillSecond;
			report[21+13*i]=(byte)bdLocationReportImp.getLocationReportSpeed();
			report[22+13*i]=(byte)bdLocationReportImp.getLocationReportBearing();
			int status=0;
			if(LOCATION_STATUS){
				status=(status&0x80);
			}
			report[23+13*i]=(byte)status;
		}
		report[11+13*locationNum]=getCRC(report,0,report.length-1);
		String hex=bytesToHexString2(report);
		return hex;	
	}
	
	
	public static String getPuShiCRCAlarm(BDLocationReportImp locationReports,byte alarmType){
		if(locationReports==null)
			return "";
		byte[] alarmArr=new byte[33];
		alarmArr[0]=(byte)0x00;
		alarmArr[1]=(byte)0xA2;
		alarmArr[2]=alarmType;
		String dateStr=locationReports.getReportTime();
		byte[] timeArr=dateStr.getBytes();
		System.arraycopy(timeArr, 0, alarmArr, 3, timeArr.length);
		double lon=locationReports.getLongitude();
		byte lonDegressByte=(byte)lon;
		double lonMinuteDouble=(lon-lonDegressByte)*60;
		byte lonMinuteByte=(byte)lonMinuteDouble;
		double lonSecondDouble=(lonMinuteDouble-lonMinuteByte)*60;
		byte lonSecondByte=(byte)lonSecondDouble;
		byte lonMillSecond=(byte)((lonSecondDouble-lonSecondByte)*10);
		alarmArr[3+timeArr.length]='E';
		alarmArr[4+timeArr.length]=lonDegressByte;
		alarmArr[5+timeArr.length]=lonMinuteByte;
		alarmArr[6+timeArr.length]=lonSecondByte;
		alarmArr[7+timeArr.length]=lonMillSecond;
		double lat=locationReports.getLatitude();
		byte latDegressByte=(byte)lat;
		double latMinuteDouble=(lat-latDegressByte)*60;
		byte latMinuteByte=(byte)latMinuteDouble;
		double latSecondDouble=(latMinuteDouble-latMinuteByte)*60;
		byte latSecondByte=(byte)latSecondDouble;
		byte latMillSecond=(byte)((latSecondDouble-latSecondByte)*60);
		alarmArr[8+timeArr.length]='N';
		alarmArr[9+timeArr.length]=latDegressByte;
		alarmArr[10+timeArr.length]=latMinuteByte;
		alarmArr[11+timeArr.length]=latSecondByte;
		alarmArr[12+timeArr.length]=latMillSecond;
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
		Date date=null;
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Calendar mCalendar=Calendar.getInstance();
		mCalendar.setTime(date);
		int year=mCalendar.get(Calendar.YEAR);
		int month=mCalendar.get(Calendar.MONTH)+1;
		int day=mCalendar.get(Calendar.DAY_OF_MONTH);
		int hour=mCalendar.get(Calendar.HOUR_OF_DAY);
		int minute=mCalendar.get(Calendar.MINUTE);
		int second=mCalendar.get(Calendar.SECOND);
		alarmArr[13+timeArr.length]=(byte)(year-2000);
		alarmArr[14+timeArr.length]=(byte)month;
		alarmArr[15+timeArr.length]=(byte)day;
		alarmArr[16+timeArr.length]=(byte)hour;
		alarmArr[17+timeArr.length]=(byte)minute;
		alarmArr[18+timeArr.length]=getCRC(alarmArr,0,alarmArr.length-1);
		String hex=bytesToHexString2(alarmArr);
		return hex;	
	}
	
	public static byte getAlarmType(String name){
		byte alarmtype=0;
		name=name.replaceAll(" ", "");
		if(name.equals("紧急救援")){
			alarmtype=0x01;
		}else if(name.equals("火灾")){
			alarmtype=(byte)0xA1;
		}else if(name.equals("人员落水")){
			alarmtype=(byte)0xA2;
		}else if(name.equals("船舶碰撞")){
			alarmtype=(byte)0xA3;
		}else if(name.equals("船舶故障")){
			alarmtype=(byte)0xA4;
		}else if(name.equals("恐怖袭击")){
			alarmtype=(byte)0xA5;
		}else if(name.equals("人员抓扣")){
			alarmtype=(byte)0xA6;
		}else if(name.equals("追赶")){
			alarmtype=(byte)0xA7;
		}else if(name.equals("船舶搁浅")){
			alarmtype=(byte)0xA8;
		}else if(name.equals("船舶风灾")){
			alarmtype=(byte)0xA9;
		}else if(name.equals("人员伤病")){
			alarmtype=(byte)0xAA;
		}else if(name.equals("船舶进水")){
			alarmtype=(byte)0xAB;
		}else if(name.equals("断电报警")){
			alarmtype=(byte)0xAF;
		}
		return alarmtype;
	}
	
	/**
	 * 获得CRC字符串
	 * @param array
	 * @param start 数组开始的索引
	 * @param end   数字结束的索引，不包含该值
	 * @return
	 */
	public static byte getCRC(byte[] array,int start,int end){
		byte bCrc=array[start];
		for(int i=start+1;i<end;i++){
			bCrc=(byte)(bCrc^array[i]);
		}
		return bCrc;
	}
	
	/**
	 * 把16进制字符串调整为从高位到低位的顺序.
	 * @param str
	 * @return
	 */
	public static String handlerHexStr(String str){
		if(str==null) return "";
		if(str.length()%2!=0){
			str="0"+str;
		}
		StringBuffer sb=new StringBuffer();		  
		for(int i=str.length();i>=2;i-=2){
             sb.append(str.substring(i-2, i)); 			  
		}
		return sb.toString();
	}
	
	/**
	 * 创建对话框Dialog
	 * 
	 * @param mContext
	 * @param title
	 * @param message
	 * @return
	 */
	public static AlertDialog createAlertDialog(Context mContext,
			String title, String message,boolean cancelable,OnClickListener positiveListener,String positiveText
			,OnClickListener negativeListener,String negativeText) {
			AlertDialog.Builder  builder= new AlertDialog.Builder(mContext);
			builder.setTitle(title);
			builder.setMessage(message);
			builder.setCancelable(cancelable);
			builder.setPositiveButton(positiveText, positiveListener);
			builder.setNegativeButton(negativeText, negativeListener);
		    return builder.create();
	}

	/**
	 * 检查北斗定位端口是否打开
	 * @param mContext
	 */
	public static void checkBDLocationPort(Context mContext,boolean isOpen,DialogInterface.OnClickListener positiveListener,
			DialogInterface.OnClickListener negitiveListener){
		 if(!isOpen&&(checkBDLocationPortDialog==null||(checkBDLocationPortDialog!=null&&!checkBDLocationPortDialog.isShowing()))){
			 checkBDLocationPortDialog=createAlertDialog(mContext,"提示","北斗导航未启用,是否进入启用?",false,
					 positiveListener,"是",negitiveListener,"否");
			 checkBDLocationPortDialog.show();
		 }
	}
	
	/**
	 * 判断当前号码是否认证中心号码
	 * @param address
	 * @return
	 */
	public static boolean isCheckAuthentication(String address){
		int cardNum=Integer.valueOf(address);
		if(cardNum>=455911&&cardNum<=455915){
			return true;
		}else if(cardNum>=524179&&cardNum<=524183){
			return true;
		}else if(cardNum==1930426){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 判断当前号码是否是授权用户
	 * @param address
	 * @return
	 */
	public static boolean isCheckUser(Context mContext,String address){
		AuthenticationDataOperation dataOper=new AuthenticationDataOperation(mContext);
		boolean istrue=dataOper.checkAccredit(address);
		dataOper.close();
		return istrue;
	}
	
	
	/**
	 * 想手机发送短信内容
	 * @param phoneNumber
	 * @param msg
	 * @return
	 */
	public static String sendPuShiPhoneSms(String phoneNumber,String msg){
		String cmds="";
		try {
			byte[] phoneNumberArr=phoneNumber.getBytes("GB2312");
			byte[] msgArr=msg.getBytes("GB2312");
			byte[] arr=new byte[4+phoneNumberArr.length+msgArr.length];
			arr[0]=0;
			arr[1]=(byte)0xA1;
			byte commFlag=0;
			commFlag=(byte)(commFlag|0x20);
			commFlag=(byte)(commFlag|0x0B);
			arr[2]=commFlag;
			for(int i=0;i<phoneNumberArr.length;i++){
				arr[3+i]=phoneNumberArr[i];
			}
			for(int j=0;j<msgArr.length;j++){
				arr[3+phoneNumberArr.length+j]=msgArr[j];
			}
			arr[3+phoneNumberArr.length+msgArr.length]=getCRC(arr,0,arr.length-1);
			cmds=bytesToHexString2(arr);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return cmds;
	}
	
	public static boolean checkActivityExist(Context mContext,String pack,String activity){
		Intent mIntent=new Intent();
		mIntent.setClassName(pack,activity);
		List<ResolveInfo> list=mContext.getPackageManager().queryIntentActivities(mIntent, 0);
		if(list.size()==0){
			return false;
		}else{
			return true;	
		}
	}
	
	
	public static boolean isServiceRunning(Context mContext,String className){
		boolean isRunning=false;
		ActivityManager activityMananger=(ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList=activityMananger.getRunningServices(Integer.MAX_VALUE);
		if(serviceList.size()<=0){
			return false;
		}
		for(int i=0;i<serviceList.size();i++){
			Log.i("Utils", serviceList.get(i).service.getClassName());
			if(serviceList.get(i).service.getClassName().equals(className)==true){
				isRunning=true;
				break;
			}
		}
		return isRunning;
	}
	
	/**
	 * 把GPS定位的值封装到BDLocationReport实体类中
	 */
	public static BDLocationReportImp addGPSLocationToAlarm(int fequency,String mUserAddress) {
		BDLocationReportImp report = new BDLocationReportImp();
		report.setHeight(LOCATION_REPORT_ALTITUDE);
		report.setHeightUnit("m");
		report.setLongitude(LOCATION_REPORT_LON);
		report.setLongitudeDir("");
		report.setLatitude(LOCATION_REPORT_LAT);
		report.setLatitudeDir("");
		report.setMsgType(1);
		report.setReportFeq(Integer.valueOf(fequency));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String time = sdf.format(new Date(LOCATION_REPORT_TIME));	
		report.setReportTime(time);
		report.setUserAddress(mUserAddress);
		report.setLocationReportSpeed(LOCATION_REPORT_SPEED);
		report.setLocationReportBearing(LOCATION_REPORT_BEARING);
		return report;
	}
	
	/**
	 * 构建位置报告2字节数组
	 * @param locationReport
	 * @return
	 */
	public static String buildeLocationReport1(BDLocationReport locationReport){
		byte[] mReportArray=new byte[13];
		mReportArray[0]=(byte)0xA0;
		/*位置报告时间*/
		Calendar calendar=Calendar.getInstance();
		int hour=calendar.get(Calendar.HOUR_OF_DAY);
		int minute=calendar.get(Calendar.MINUTE);
		int second=calendar.get(Calendar.SECOND);;
		int millSecond=calendar.get(Calendar.MILLISECOND);
		//毫秒取前两位
		if(millSecond>100){
			millSecond/=10;
		}
		int time=0;
		hour=hour<<19;
		minute=minute<<13;
		second=second<<7;
		time=hour|minute|second|millSecond;
		byte[] mLocationTimeArray=intToByteArray(time);
		System.arraycopy(mLocationTimeArray, 1, mReportArray, 1, mLocationTimeArray.length-1);
		/*位置报告经度*/
		
		int[] mReportLonArray=mTranslateLonLatUnit(locationReport.getLongitude());
		int mLonDegress=mReportLonArray[0];
		int mLonMinute=mReportLonArray[1];
		int mLonSecond=mReportLonArray[2];
		int mLonMillSec=mReportLonArray[3];
		mLonDegress=mLonDegress<<16;
		mLonMinute=mLonMinute<<10;
		mLonSecond=mLonSecond<<4;
		int mReportLongitude=mLonDegress|mLonMinute|mLonSecond|mLonMillSec;
		byte[] mLocationLonArray=intToByteArray(mReportLongitude);
		System.arraycopy(mLocationLonArray, 1, mReportArray, 4, 3);
		
		/*位置报告纬度*/
		int[] mReportLanArray=mTranslateLonLatUnit(locationReport.getLatitude());
		int mLatDegress=mReportLanArray[0];
		int mLatMinute=mReportLanArray[1];
		int mLatSecond=mReportLanArray[2];
		int mLatMillSec=mReportLanArray[3];
		int heightType=00;
		int mReportLatidute=0;
		mLatDegress=mLatDegress<<18;
		mLatMinute=mLatMinute<<12;
		mLatSecond=mLatSecond<<6;
		mLatMillSec=mLatMillSec<<2;
		mReportLatidute=mLatDegress|mLatMinute|mLatSecond|mLatMillSec|heightType;
		byte[] mLocationLatArray=intToByteArray(mReportLatidute);
		System.arraycopy(mLocationLatArray, 1, mReportArray, 7, 3);
		/*高程*/
		double reportHeight=locationReport.getHeight();
		int mHeightFlag=0;
		int mHeightValue=(int)Math.abs(reportHeight);
		if(reportHeight<0){
			mHeightFlag=1;
		}
		int mHeightExceptionFlag=0;
		int mHeightExceptionValue=0;
		
		mHeightFlag=mHeightFlag<<23;
		mHeightValue=mHeightValue<<9;
		mHeightExceptionFlag=mHeightExceptionFlag<<8;
		int mReportHeight=mHeightFlag|mHeightValue|mHeightExceptionFlag|mHeightExceptionValue;
		byte[] mLocationHeightArray=intToByteArray(mReportHeight);
		System.arraycopy(mLocationHeightArray, 1, mReportArray, 10, 3);
		return bytesToHexString2(mReportArray);
	} 
	/**
	 * 将以'度'为单位的经度或纬度转换为'度，分,秒，毫秒'格式
	 * @return
	 */
	public static int[] mTranslateLonLatUnit(double number) {
		if(number<=0){
			return null;
		}
		int[] mLonLat=new int[4];
		//经度或纬度的度值
		mLonLat[0]=(int)Math.floor(number);
		double mLonLatMinute=(number-mLonLat[0])*60.0d;
		//经度或纬度的分值
		mLonLat[1]=(int)Math.floor(mLonLatMinute);
		double mLonLatSecond=(mLonLatMinute-mLonLat[1])*60.0d;
		//经度或纬度的秒值
		mLonLat[2]=(int)Math.floor(mLonLatSecond);
		double mMilliSecond=mLonLatSecond-mLonLat[2];
		//经度或纬度的毫秒值
		mLonLat[3]=(int)Math.rint(mMilliSecond*10);
		return mLonLat;
	}
	
	/**
	 * 是否显示对话框
	 */
	public static boolean isProgressDialogShowing = false;
	
	/**
	 * 是否是循环定位
	 */
	public static boolean isCycleLocation = false;
	
	/**
	 * 是否是紧急定位
	 */
	public static boolean isImmediateLocation = false;
	
	/**
	 * 循环定位的频率
	 */
	public static int LOCATION_FRENQUENCY = 0;

	/**
	 * 倒计时计数
	 */
	public static int COUNT_DOWN_TIME = 0;
	
	/**
	 * 倒计时计数
	 */
	public static int LOCATION_REPORT_DOWN_TIME = 0;
	
	
	/**
	 * 北斗短信循环发送频率
	 */
	public static int BD_MESSAGE_FREQUNENCY = 0;
	
	/**
	 * 是否循环发送短信
	 */
	public static boolean isStopCycleMessage = false;
	
	/**
	 * 标识当前短信显示的页面的Index值
	 */
	public static int BD_MESSAGE_PAGER_INDEX=1;
	
	/**
	 * RNSS当前的定位模式
	 */
	public static int RNSS_CURRENT_LOCATION_MODEL = 0;
	/**
	 * 是否是采集数据
	 */
		public static boolean isSaveData = false;
	
	/**
	 * 保留Double小数显示的位数,如果小于则在末尾补零
	 * 
	 * @param num
	 *            double数
	 * @param bit
	 *            显示几位小数
	 * @return
	 */
	public static String setDoubleNumberDecimalDigit(double num, int bit) {
		if (bit <= 0) {
			return null;
		}
		StringBuilder pattern = new StringBuilder(".");
		for (int i = 0; i < bit; i++) {
			pattern.append("#");
		}
		DecimalFormat df = new DecimalFormat(pattern.toString());
		String numStr = df.format(num);
		int count = bit - numStr.split("\\.")[1].length();
		for (int i = 0; i < count; i++) {
			numStr += "0";
		}
		return numStr;
	}
	
	/**
	 * 把经纬度从分为单位转换成以度为单位
	 * 
	 * @param num
	 * @return
	 */
	public static double changeLonLatMinuteToDegree(double num) {
		if (num == 0.0 || num < 100) {
			return 0.0;
		}
		String value = String.valueOf(num);
		int index = value.indexOf(".");
		return Double.valueOf(value.substring(0, index - 2)).doubleValue()
				+ (Double.valueOf(value.substring(index - 2)).doubleValue() / 60.0d);
	}
	/**
	 * 构造发送手机短信命令
	 * 
	 * @param phoneNum
	 * @param msgContent
	 * @return
	 */
	public static String buildSendPhoneSMS(String phoneNum, String msgContent) {
		byte[] content = null;
		byte[] phoneNumByteArray = null;
		byte[] msgContentByteArray = null;
		try {
			phoneNumByteArray = phoneNum.getBytes("GBK");
			msgContentByteArray = msgContent.getBytes("GBK");
			content = new byte[4 + phoneNumByteArray.length
					+ msgContentByteArray.length];
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		content[0] = (byte) 0xCC;
		content[1] = (byte) phoneNum.length();
		System.arraycopy(phoneNumByteArray, 0, content, 2,
				phoneNumByteArray.length);
		content[phoneNumByteArray.length + 2] = 0x00;
		System.arraycopy(msgContentByteArray, 0, content,
				phoneNumByteArray.length + 3, msgContentByteArray.length);
		content[phoneNumByteArray.length + 3 + msgContentByteArray.length] = (byte) 0xDD;
		return bytesToHexString2(content);
	}

	/**
	 * 获得短信的最大长度
	 * 
	 * @param checkEncryption
	 *            是否加密
	 * @param level
	 *            卡级别
	 */
	public static int getMessageMaxLength() {
		BDCardInfoManager manager = BDCardInfoManager.getInstance();
		if (manager != null && manager.getCardInfo() != null) {
			if ("E".equals(manager.getCardInfo().checkEncryption)) {
				switch (manager.getCardInfo().mCommLevel) {
				case 1:
					MESSAGE_MAX_LENGHTH = 140-8;
					break;
				case 2:
					MESSAGE_MAX_LENGHTH = 360-8;
					break;
				case 3:
					MESSAGE_MAX_LENGHTH = 580-8;
					break;
				case 4:
					MESSAGE_MAX_LENGHTH = 1680-8;
					break;
				default:
					break;
				}
			} else {
				switch (manager.getCardInfo().mCommLevel) {
				case 1:
					MESSAGE_MAX_LENGHTH = 110-8;
					break;
				case 2:
					MESSAGE_MAX_LENGHTH = 408-8;
					break;
				case 3:
					MESSAGE_MAX_LENGHTH = 628-8;
					break;
				case 4:
					MESSAGE_MAX_LENGHTH = 848-8;
					break;
				default:
					break;
				}
			}
		}
		return MESSAGE_MAX_LENGHTH;
	}
	

	/**
	 * 校验当前字符串是否是数字
	 * 
	 * @param txt
	 * @return
	 */
	public static boolean isNumber(String txt) {
		String reg = "[0-9]*";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(txt);
		if (matcher.matches()) {
			return true;
		}
		return false;
	}
	
	/**
	 * 转换经纬度
	 * rdss
	 * @param lon
	 * @param lat
	 * @param height
	 * @return
	 */
	public static BDLocation translate(BDLocation bdlocation, int flag) {
		BDLocation coodrinate = null;
		switch (flag) {
		case 0:
			/* 大地坐标 */
			coodrinate = new BDLocation();
			coodrinate.setLongitude(bdlocation.mLongitude);
			coodrinate.setLatitude(bdlocation.mLatitude);
			coodrinate.setEarthHeight(bdlocation.getEarthHeight());
			break;
		case 4:
			/**北京54***/
			coodrinate=new BDLocation();
			double[] bj54lonlat=transWGS84ToBj54(bdlocation.mLongitude, bdlocation.mLatitude);
			coodrinate.setLongitude(bj54lonlat[0]);
			coodrinate.setLatitude(bj54lonlat[1]);
			coodrinate.setEarthHeight(bdlocation.getEarthHeight());
			break;
		case 1:
			/* 高斯平面坐标 */
			coodrinate = LBToGaoSi(bdlocation.mLongitude, bdlocation.mLatitude,
					bdlocation.getEarthHeight());
			break;
		case 2:
			/* 麦卡托平面坐标 */
			coodrinate = Maikatou(bdlocation.mLongitude, bdlocation.mLatitude,
					bdlocation.getEarthHeight());
			break;
		case 3:
			/* 空间直角坐标 */
			coodrinate = DD_KJZJ1(bdlocation.mLongitude, bdlocation.mLatitude,
					bdlocation.getEarthHeight());
			break;
		default:
			break;
		}

		coodrinate.mLocationTime = bdlocation.mLocationTime;
		return coodrinate;
	}

	/**
	 * 检查北斗SIM卡是否安装
	 * @param mContext
	 */
	public static boolean checkBDSimCard(Context mContext) {
		BDCardInfoManager cardManager = BDCardInfoManager.getInstance();
		boolean isTrue = false;
		try {
			isTrue = cardManager.checkBDSimCard();
		} catch (BDParameterException e) {
			e.printStackTrace();
		}
		if (!isTrue) {
			Toast.makeText(
					mContext,
					mContext.getResources().getString(R.string.have_not_bd_sim),
					Toast.LENGTH_SHORT).show();
			return false;
		} else {
			return true;
		}
	}

	
	/**
	 * GPS的坐标WGS84坐标系转BJ54坐标系 
	 * @param dLongitude
	 * @param dLatitude
	 * @return
	 */
	public static double[] transWGS84ToBj54(double dLongitude, double dLatitude){
	 	double EARTH_WGS84_A = 6378137.0000;
	 	double EARTH_WGS84_E2 = 0.00669437999013;
	 	double EARTH_WGS84_FLATTENING = 298.257223563;
	 	double EARTH_BJ54_A = 6378245.0;
	 	double EARTH_BJ54_E2 = 0.00667;
	 	double EARTH_BJ54_FLATTENING = 298.3;  
	 	double OMIGA = 206264.8062;
	 	double DELTA_X = -28.3;
	 	double DELTA_Y = 144.9;
	 	double DELTA_Z = 77.5;
    	double sinRenda = Math.sin(dLongitude);
    	double cosRenda = Math.cos(dLongitude);
    	double sinFi =Math.sin(dLatitude);
    	double cosFi =Math.cos(dLatitude);
    	double sinFi2 = sinFi*sinFi;
    	double eSinFi =Math.sqrt(1-EARTH_WGS84_E2*sinFi2);
    	double M = EARTH_WGS84_A*(1-EARTH_WGS84_E2)/(eSinFi*eSinFi*eSinFi);
    	double N = EARTH_WGS84_A/eSinFi;
    	double deltaE2 = 2*(1-1/EARTH_BJ54_FLATTENING)*(1/EARTH_WGS84_FLATTENING-1/EARTH_BJ54_FLATTENING);
    	double deltaA = EARTH_BJ54_A - EARTH_WGS84_A;
    	double deltaRenda = OMIGA*(cosRenda*DELTA_Y - sinRenda*DELTA_X)/(N*sinRenda);
    	double deltaFi = OMIGA*((EARTH_WGS84_A*deltaE2 + EARTH_WGS84_E2*deltaA)*sinFi*cosFi + EARTH_WGS84_A*EARTH_WGS84_E2*deltaE2*sinFi*sinFi*sinFi*cosFi-sinFi*cosRenda*DELTA_X - sinFi*sinRenda*DELTA_X + cosFi*DELTA_Z)/M;
        double[] bj54lonlat=new double[2];
        bj54lonlat[0] = dLongitude + deltaRenda/3600; //经度
	  	bj54lonlat[1] = dLatitude + deltaFi/3600; //纬度
	  	return bj54lonlat;
	}
	
	public static BDLocation Maikatou(double lon, double lat, double height) {
		BDLocation param = xy_(lon, lat, 500000, 0, 0.9996, -1, bj54a, bj54f, 0);
		param.setEarthHeight(height);
		return param;
	}

	public static BDLocation LBToGaoSi(double dblLong, double dblLat,
			double dalHeight) {
		return DD_GOSS2(dblLong, dblLat, dalHeight, 500000.0d, 0.0d, 1.0d,
				-1.0d, bj54a, bj54f, 0.0d);
	}

	public static BDLocation xy_(double lon, double lat, double efalsing,
			double nfalsing, double scale, double l0, double a, double f,
			double hcrr_h) {
		int i;
		double b = 0.0d, l = 0.0d, e = 0.0d, e1 = 0.0d, tt = 0.0d, n = 0.0d, g = 0.0d, m0 = 0.0d, x0_ = 0.0d, tmp = 0.0d;
		b = lat;
		l = lon;
		double[] par = new double[6];
		if (l0 == -1) {
			if (l > 0)
				l0 = (int) ((int) (l + 3) / 6. + 0.5) * 6 - 3;
			else
				l0 = (int) ((int) (l - 3) / 6. - 0.5) * 6 + 3;
		}
		l -= l0;
		if (l < -350.)
			l += 360.;
		l *= PI / 180;
		b *= PI / 180;
		e = 2.0 / f - 1.0 / f / f;
		e1 = e / (-e + 1.0);
		a += hcrr_h * (1 - e * Math.sin(b) * Math.sin(b)) / Math.sqrt(1.0 - e);
		par[0] = Math.pow(e, 5) * 43659.0 / 65536.0 + Math.pow(e, 4) * 11025.0
				/ 16384.0 + 1.0;
		par[0] += e * e * 45.0 / 64.0 + Math.pow(e, 3) * 175.0 / 256.0 + e
				* 0.75;
		par[1] = e * 0.75 + e * e * 15.0 / 16.0 + Math.pow(e, 3) * 525.0
				/ 512.0;
		par[1] += Math.pow(e, 4) * 2205.0 / 2048.0 + Math.pow(e, 5) * 72765.0
				/ 65536.0;
		par[2] = e * e * 15.0 / 64.0 + Math.pow(e, 3) * 105.0 / 256.0;
		par[2] += Math.pow(e, 4) * 2205.0 / 4096.0 + Math.pow(e, 5) * 10395.0
				/ 16384.0;
		par[3] = Math.pow(e, 3) * 35.0 / 512.0 + Math.pow(e, 4) * 315.0
				/ 2048.0;
		par[3] += Math.pow(e, 5) * 31185.0 / 131072.0;
		par[4] = Math.pow(e, 4) * 315.0 / 16384.0 + Math.pow(e, 5) * 3465.0
				/ 65536.0;
		par[5] = Math.pow(e, 5) * 693.0 / 131072.0;
		tmp = a * (-e + 1.0);
		par[0] *= tmp;
		for (i = 1; i < 6; i++)
			par[i] *= Math.pow(-1.0, (i * 1.0)) * tmp / (2.0 * i);
		x0_ = par[0] * b + par[1] * Math.sin(2.0 * b) + par[2]
				* Math.sin(4.0 * b);
		x0_ += par[3] * Math.sin(6.0 * b) + par[4] * Math.sin(8.0 * b) + par[5]
				* Math.sin(10.0 * b);
		tt = Math.tan(b);
		n = a / Math.sqrt(1.0 - e * Math.sin(b) * Math.sin(b));
		g = e1 * Math.cos(b) * Math.cos(b);
		m0 = l * Math.cos(b);

		double X = x0_ + n * tt * m0 * m0 / 2.0;
		X += (5.0 - tt * tt + 9.0 * g + 4.0 * g * g) * n * tt * Math.pow(m0, 4)
				/ 24.0;
		X += n * Math.pow(m0, 6) * tt
				* (61.0 - 58.0 * tt * tt + tt * tt * tt * tt) / 720.0;

		double Y = n * m0 + n * Math.pow(m0, 3) * (1.0 - tt * tt + g) / 6.0;
		Y += (5.0 - 18.0 * tt * tt + tt * tt * tt * tt + 14.0 * g - 58.0 * g
				* tt * tt)
				* n * Math.pow(m0, 5) / 120.0;
		X *= scale;
		Y *= scale;
		X += nfalsing;
		Y += efalsing;
		BDLocation param = new BDLocation();
		param.setLongitude(Y);
		param.setLatitude(X);
		return param;
	}
	
	public static BDLocation DD_GOSS2(double dblLong, double dblLat,
			double dblHeight, double efalsing, double nfalsing, double scale,
			double l0, double a, double f, double hcrr_h) {
		double X = 0.0;
		double Y = 0.0;
		double B, L;
		//

		int i;
		double b, l, e, e1, tt, n, g, m0, x0_, tmp;
		double[] par = new double[6];

		B = dblLat;
		L = dblLong;
		b = B;
		l = L;
		// if((Lf=='W'||Lf=='w')&&L>0)l*=-1;
		// if((Bf=='S'||Bf=='s')&&B>0)b*=-1;

		if (l0 == -1) {
			if (l > 0)
				l0 = (int) ((int) (l + 3) / 6. + 0.5) * 6 - 3;
			else
				l0 = (int) ((int) (l - 3) / 6. - 0.5) * 6 + 3;
		}
		l -= l0;
		if (l < -350.)
			l += 360.;
		l *= PI / 180;
		b *= PI / 180;
		e = 2.0 / f - 1.0 / f / f;
		e1 = e / (-e + 1.0);
		a += hcrr_h * (1 - e * Math.sin(b) * Math.sin(b)) / Math.sqrt(1.0 - e);
		par[0] = Math.pow(e, 5) * 43659.0 / 65536.0 + Math.pow(e, 4) * 11025.0
				/ 16384.0 + 1.0;
		par[0] += e * e * 45.0 / 64.0 + Math.pow(e, 3) * 175.0 / 256.0 + e
				* 0.75;
		par[1] = e * 0.75 + e * e * 15.0 / 16.0 + Math.pow(e, 3) * 525.0
				/ 512.0;
		par[1] += Math.pow(e, 4) * 2205.0 / 2048.0 + Math.pow(e, 5) * 72765.0
				/ 65536.0;
		par[2] = e * e * 15.0 / 64.0 + Math.pow(e, 3) * 105.0 / 256.0;
		par[2] += Math.pow(e, 4) * 2205.0 / 4096.0 + Math.pow(e, 5) * 10395.0
				/ 16384.0;
		par[3] = Math.pow(e, 3) * 35.0 / 512.0 + Math.pow(e, 4) * 315.0
				/ 2048.0;
		par[3] += Math.pow(e, 5) * 31185.0 / 131072.0;
		par[4] = Math.pow(e, 4) * 315.0 / 16384.0 + Math.pow(e, 5) * 3465.0
				/ 65536.0;
		par[5] = Math.pow(e, 5) * 693.0 / 131072.0;
		tmp = a * (-e + 1.0);
		par[0] *= tmp;
		for (i = 1; i < 6; i++)
			par[i] *= Math.pow(-1.0, (i * 1.0)) * tmp / (2.0 * i);
		x0_ = par[0] * b + par[1] * Math.sin(2.0 * b) + par[2]
				* Math.sin(4.0 * b);
		x0_ += par[3] * Math.sin(6.0 * b) + par[4] * Math.sin(8.0 * b) + par[5]
				* Math.sin(10.0 * b);

		tt = Math.tan(b);
		n = a / Math.sqrt(1.0 - e * Math.sin(b) * Math.sin(b));
		g = e1 * Math.cos(b) * Math.cos(b);
		m0 = l * Math.cos(b);
		X = x0_ + n * tt * m0 * m0 / 2.0;
		X += (5.0 - tt * tt + 9.0 * g + 4.0 * g * g) * n * tt * Math.pow(m0, 4)
				/ 24.0;
		X += n * Math.pow(m0, 6) * tt
				* (61.0 - 58.0 * tt * tt + tt * tt * tt * tt) / 720.0;
		Y = n * m0 + n * Math.pow(m0, 3) * (1.0 - tt * tt + g) / 6.0;
		Y += (5.0 - 18.0 * tt * tt + tt * tt * tt * tt + 14.0 * g - 58.0 * g
				* tt * tt)
				* n * Math.pow(m0, 5) / 120.0;
		X *= scale;
		Y *= scale;
		X += nfalsing;
		Y += efalsing;
		BDLocation coodrinate = new BDLocation();
		coodrinate.setLongitude(Y);
		coodrinate.setLatitude(X);
		coodrinate.setEarthHeight(dblHeight);
		return coodrinate;
	}

	public static BDLocation DD_KJZJ1(double L, double B, double H) {
		double dataf1, dataf2, dataf3, dataf4;
		// 84
		/*
		 * a= 6378137; f=0.003352810664
		 * 
		 * 
		 * //54 a = 6378245 f = 0.0033523298692
		 */

		// //54 zuobiaoxi
		// if (zbx == 0)
		// {
		dataf3 = 6378245.0; /* a */
		dataf1 = 0.0033523;/* f */
		// }
		// //84 zuobiaoxi
		// else
		// {
		// dataf3 = 6378137.0; /* a */
		// dataf1 = 0.003352810664;/* f */
		// }

		dataf2 = (1.0 - dataf1) * dataf3;/*
										 * b = (1-0.0033523)*6378245 =
										 * 6356863.2092865
										 */

		dataf1 = dataf3 * dataf3 - dataf2 * dataf2; /*
													 * a^2-b^2 = 6378245*6378245
													 * - 6356863.2092865*
													 * 6356863.2092865 =
													 * 272299418444.73970016091775
													 */
		dataf1 /= dataf3 * dataf3; /* (a^2-b^2)/a^2 = 0.00669336208471 */
		/* dataf1=e^2 */
		dataf2 = B * PI / 180.0;
		dataf4 = Math.sin(dataf2);
		/* sin(B) */
		dataf2 = dataf3 / Math.sqrt(1.0 - dataf1 * dataf4 * dataf4);
		/* N */

		dataf3 = (dataf2 * (1.0 - dataf1) + H) * dataf4;
		/* Z */
		dataf1 = (dataf2 + H) * Math.cos(B * PI / 180.0)
				* Math.cos(L * PI / 180.0);
		/* X */
		dataf4 = (dataf2 + H) * Math.cos(B * PI / 180.0)
				* Math.sin(L * PI / 180.0);
		/* Y */
		BDLocation coodrinate = new BDLocation();
		coodrinate.setLongitude(dataf1);
		coodrinate.setLatitude(dataf4);
		coodrinate.setEarthHeight(dataf3);
		return coodrinate;
	}
	
	/**
	 * 显示位置报告Notification
	 * @param report
	 */
	public  static void mShowLocationReportNotification(Context mContext,
			BDLocationReport report) {
		//Toast.makeText(mContext, "时间:"+report.getReportTime(), Toast.LENGTH_SHORT).show();
		NotificationManager mNotificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = buildReportNotification(mContext,
				report.getUserAddress());
		/* 点击该通知后要跳转的Activity */
		Intent notificationIntent =null;
		if("S500".equals(Utils.DEVICE_MODEL)){
			/*点击该通知后要跳转的Activity*/
			notificationIntent = new Intent(mContext, FriendsLocationActivity.class);
		}else{
			/*点击该通知后要跳转的Activity*/
			notificationIntent = new Intent(mContext, BDManagerHorizontalActivity.class);
		}
		Utils.BD_MANAGER_PAGER_INDEX=5; //显示友邻位置页面
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(
				mContext,
				new String("来自 " + report.mUserAddress + " 位置报告"),
				"经度：" + report.mLongitude
						+ "纬度:"
						+ report.mLatitude,
				contentIntent);
		NOTIFICATION_ID++;

		//mNotificationManager.notify(NOTIFICATION_ID % 10, notification);
		mNotificationManager.notify(Config.MY_LOC_REPORT_NOTIFICATION, notification);
		friendLocationNotificationID = NOTIFICATION_ID%10;
		destoryNotification = notification;
	}
	
	private static Notification buildReportNotification(Context mContext,
			String tickerText) {
		Notification notification = new Notification();
		notification.tickerText = tickerText;
		notification.icon = R.drawable.title_loc_flag;
		notification.when = System.currentTimeMillis();
		notification.defaults = Notification.DEFAULT_SOUND;
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		return notification;
	}
	
	public static double lonStr2Double(String lon){
		if(lon==null || "".equals(lon)){
			return 0;
		}
		if(lon.length()!=9){
			//Log.e(TAG, "Parameter is error!");
			return 0;
		}
		int lonDegress=Integer.valueOf(lon.substring(0, 3));
		double dlonMin=Double.valueOf(lon.substring(3,5)+"."+lon.substring(5,8)).doubleValue();
		return lonDegress+dlonMin/60.0;
	}
	
	public static String getLonDirection(String lon){
		if(lon==null || "".equals(lon)){
			return "";
		}
		if(lon.length()!=9){
			//Log.e(TAG, "Parameter is error!");
			return "";
		}
		String direction=lon.substring(8, 9);
		return direction;
	}
	
	public static double latStr2Double(String lat){
		if(lat==null || "".equals(lat)){
			return 0;
		}
		if(lat.length()!=8){
			Log.e(TAG, "Parameter is error!");
			return 0;
		}
		int latDegress=Integer.valueOf(lat.substring(0, 2));
		double dlanMin=Double.valueOf(lat.substring(2,4)+"."+lat.substring(4,7)).doubleValue();
		return latDegress+dlanMin/60.0;
	}

	//经过验证OK
	public static String getLatDirection(String lat){
		if(lat==null || "".equals(lat)){
			return "";
		}
		if(lat.length()!=8){
			Log.e(TAG, "Parameter is error!");
			return "";
		}
		String direction=lat.substring(7,8);
		return direction;
	}

	public static String lonlatDouble2Str(double lonlat){
		//1.把double类型的数值转换成字符串
		String lonlatStr=String.valueOf(lonlat);
		//2.小数点处把字符串分成两段,一段是经纬度数值以'度'为单位的整数字符串,另一段是经纬度数值以'度'为单位的小数字符串.
		String[] lonlatDegress=lonlatStr.split("\\.");
		//3.把经纬度值以'度'为单位的小数字符串转换成double类型,并且把单位从'度'转换为'分'
		double lonlatMinDouble=(Double.valueOf("0."+lonlatDegress[1]).doubleValue())*60.0;
		//4.将以'分'为单位的数值,转换为字符串.
		String lonlatMinStr=String.valueOf(lonlatMinDouble);
		//5.在小数点处把字符串分为两段.
		String[] lonlatMinArr=lonlatMinStr.split("\\.");
        String lonlatMinInteger=lonlatMinArr[0];
        String lonlatMinDecimal=lonlatMinArr[1];
        //6.保证分为单位的整数部分为2位数
        if(lonlatMinInteger.length()<2){
        	lonlatMinInteger="0"+lonlatMinInteger;
        }else if(lonlatMinInteger.length()>2){
        	lonlatMinInteger=lonlatMinInteger.substring(lonlatMinInteger.length()-2, lonlatMinInteger.length());
        }
        //7.保证以分为单位的小数部分有3位数
		int size=lonlatMinDecimal.length();
		if(size<3){
			for(int i=0;i<3-size;i++){
				lonlatMinDecimal+="0";
			}
		}else{
			lonlatMinDecimal=lonlatMinDecimal.substring(0, 3);
		}
		return lonlatDegress[0]+lonlatMinInteger+lonlatMinDecimal;
	}

	/**
	 * 检测当前北斗卫星RDSS是否适合发送定位/短报文/位置报告 信号分为差，一般，良好
	 * 
	 * @return
	 */
	public static int checkCurrentRDSSStatus(int[] beamWaves) {
		int[] beams = new int[5];
		int index = 0;
		for (int i = 0; i < 5; i++) {
			beams[i] = beamWaves[i * 2] + beamWaves[i * 2 + 1];
			if (beams[i] > 0) {
				index++;
			}
		}
		// if(index>=2){
		// //当前可发送定位/短报文
		// }else if(index>=1){
		// //当前只能发送短报文
		// }else{
		// //当前信号较差,不能发送与卫星较差，请到移动到空旷的地方。
		// }
		return index;
	}

	/**
	 * 把以'度，分,秒，毫秒'格式的经度或纬度转换为'度'为单位
	 * 
	 * @param array
	 * @return
	 */
	public static double mTranslateLonLatUnit(int[] array) {
		if (array == null) {
			return 0;
		}
		int mDegree = array[0];
		int mMinute = array[1];
		int mSecond = array[2];
		int mMilliSec = array[3];
		double mValue = mDegree + mMinute / 60.0d
				+ (mSecond + (mMilliSec / 10.0d)) / 3600.d;
		return mValue;
	}

	/**
	 * 显示短信Notification
	 * @param mContext
	 * @param message
	 */
	public static void mShowMessageNotification(Context mContext,
			BDMessageInfo message,String messagContent) {
		/*发送Notification*/
		NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification=buildMessageNotification(mContext,message.getmUserAddress());
		Intent notificationIntent =null;
		if("S500".equals(Utils.DEVICE_MODEL)){
			/*点击该通知后要跳转的Activity*/
			notificationIntent = new Intent(mContext, BDSendMsgPortActivity.class);
		}else{
			/*点击该通知后要跳转的Activity*/
			notificationIntent = new Intent(mContext, BDSendMsgLandScapeActivity.class);
		}
		Utils.BD_MESSAGE_PAGER_INDEX=2;
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,notificationIntent, 0);
		notification.setLatestEventInfo(mContext,new String("来自 " + message.getmUserAddress() + " 信息"),messagContent, contentIntent);
		NOTIFICATION_ID++;
		Utils.smsNotificationShow=true;
		//mNotificationManager.notify(NOTIFICATION_ID%10, notification);
		mNotificationManager.notify(Config.SMS_NOTIFICATION, notification);

		messageNotificationID = NOTIFICATION_ID%10;
		destoryNotification = notification;
	}
	/**
	 * 显示短信Notification
	 * @param mContext
	 * @param message
	 */
	@SuppressLint("NewApi")
	public static void mShowMessageNotification2(Context mContext,
			BDMessageInfo message,String messagContent,ArrayList<FriendBDPoint> friendLocationList) {
		/*发送Notification*/
		NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification=buildMessageNotification(mContext,message.getmUserAddress());
		Intent notificationIntent =null;
		if("S500".equals(Utils.DEVICE_MODEL)){
			/*点击该通知后要跳转的Activity*/
			notificationIntent = new Intent(mContext, BDSendMsgPortActivity.class);
		}else{
			/*点击该通知后要跳转的Activity*/
			notificationIntent = new Intent(mContext, NaviStudioActivity.class);
			notificationIntent.putExtra("friendLocationList", friendLocationList);
			notificationIntent.putExtra(Constants.INTENT_ACTION, Constants.INTENT_TYPE_FRIEND_LOCATION_LIST_NOTIFYCATION);
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		Utils.BD_MESSAGE_PAGER_INDEX=2;
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.INTENT_ACTION, Constants.INTENT_TYPE_FRIEND_LOCATION_LIST_NOTIFYCATION);
		bundle.putSerializable("friendLocationList", friendLocationList);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,notificationIntent, 0,bundle);
//		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,notificationIntent, 0);
		notification.setLatestEventInfo(mContext,new String("来自 " + message.getmUserAddress() + " 友邻位置"),messagContent, contentIntent);
		NOTIFICATION_ID++;
		Utils.smsNotificationShow=true;
		//mNotificationManager.notify(NOTIFICATION_ID%10, notification);
		mNotificationManager.notify(Config.FRIENDS_LOC_NOTIFICATION, notification);

		messageNotificationID = NOTIFICATION_ID%10;
		destoryNotification = notification;
	}
	
	private static Notification buildMessageNotification(Context mContext,
			String tickerText) {
		Notification notification = new Notification();
		notification.tickerText = tickerText;
		notification.icon = R.drawable.title_msg_flag;
		notification.when = System.currentTimeMillis();
		notification.defaults = Notification.DEFAULT_SOUND;
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		return notification;
	}
	
	/**
	 * 清除一个 nofifacation
	 */
	public static void destoryMessageNotification(Context mContext){
		
		if (messageNotificationID!=-1) {
			NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			//mNotificationManager.cancel(messageNotificationID);
			mNotificationManager.cancel(Config.SMS_NOTIFICATION);
		}
		
		
	}
	/**
	 * 清除一个 nofifacation
	 */
	public static void destoryFriendLocationNotification(Context mContext){
		
		if (friendLocationNotificationID!=-1) {
			NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			//mNotificationManager.cancel(friendLocationNotificationID);
			mNotificationManager.cancel(Config.MY_LOC_REPORT_NOTIFICATION);
		}
	}
	
	
	public static AlertDialog createAlertDialog(Context mContext, String title,
			String message, boolean cancelable,
			OnClickListener negativeListener, String negativeText) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setCancelable(cancelable);
		builder.setNegativeButton(negativeText, negativeListener);
		return builder.create();
	}
	
	/**
	 * 设置Activity的背景
	 * 
	 * @param mContext
	 *            上下文
	 * @param layout
	 */
	public static void setBackgroud(Context mContext, LinearLayout layout) {
		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
				R.drawable.bg);
		BitmapDrawable bd = new BitmapDrawable(bitmap);
		bd.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		bd.setDither(true);
		layout.setBackgroundDrawable(bd);
	}
	
	
	/** 
	 * 判断SDCard是否存在 [当没有外挂SD卡时，内置ROM也被识别为存在sd卡] 
	 *  
	 * @return 
	 */  
	public static boolean isSdCardExist() {  
	    return Environment.getExternalStorageState().equals(  
	            Environment.MEDIA_MOUNTED);  
	}  
	
	
	/** 
	 * 获取SD卡根目录路径 
	 *  
	 * @return 
	 */  
	public static String getSdCardPath() {  
	    boolean exist = isSdCardExist();  
	    String sdpath = "";  
	    if (exist) {  
	        sdpath = Environment.getExternalStorageDirectory()  
	                .getAbsolutePath();  
	    } else {  
	        sdpath = "不适用";  
	    }  
	    return sdpath;  
	  
	}  
	
	/** 
	 * 获取默认的文件路径 
	 *  
	 * @return 
	 */  
	public static String getDefaultFilePath() {  
	    String filepath = "";  
	    File file = new File(Environment.getExternalStorageDirectory(),  
	            "abc.txt");  
	    if (file.exists()) {  
	        filepath = file.getAbsolutePath();  
	    } else {  
	        filepath = "不适用";  
	    }  
	    return filepath;  
	}  
	
	/**
	 * 读取sd卡根目录文件  配置信息
	 * @param fileName
	 * @return
	 */
	public static String readFile_ExtSDcard(String fileName){
		
		String result =null;
		try {  //   /mnt/internalsd/MapAbc/version.properties
			File file2 = new File(Environment.getExternalStorageDirectory()+"/MapAbc",  
					fileName);  
			File file = new File("/mnt/extsd/MapAbc",  
					fileName);  
			FileInputStream is = new FileInputStream(file);  
			byte[] b = new byte[is.available()];  
			is.read(b);  
			result = new String(b);  
			System.out.println("读取成功："+result);  
		} catch (Exception e) {  
			e.printStackTrace();  
		}
		return result;  
	}
	/**
	 * 读取sd卡根目录文件  配置信息
	 * @param fileName
	 * @return
	 */
	public static String readFile_interlSDcard(String fileName){
		
		String result =null;
		try {  //   /mnt/internalsd/MapAbc/version.properties
			File file = new File(Environment.getExternalStorageDirectory()+"/MapAbc",  
					fileName);  
//			File file = new File("/mnt/extsd/MapAbc",  
//					fileName);  
			FileInputStream is = new FileInputStream(file);  
			byte[] b = new byte[is.available()];  
			is.read(b);  
			result = new String(b);  
			System.out.println("读取成功："+result);  
		} catch (Exception e) {  
			e.printStackTrace();  
		}
		return result;  
	}

	
	/**
	 * string转bcd
	 * @param asc
	 * @return
	 */
	public static byte[] str2Bcd(String asc) {
		int len = asc.length();
		int mod = len % 2;
		if (mod != 0) {
			asc = "0" + asc;
			len = asc.length();
		}
		byte abt[] = new byte[len];
		if (len >= 2) {
			len = len / 2;
		}
		byte bbt[] = new byte[len];
		abt = asc.getBytes();
		int j, k;
		for (int p = 0; p < asc.length() / 2; p++) {
			if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
				j = abt[2 * p] - '0';
			} else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
				j = abt[2 * p] - 'a' + 0x0a;
			} else {
				j = abt[2 * p] - 'A' + 0x0a;
			}
			if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
				k = abt[2 * p + 1] - '0';
			} else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
				k = abt[2 * p + 1] - 'a' + 0x0a;
			} else {
				k = abt[2 * p + 1] - 'A' + 0x0a;
			}
			int a = (j << 4) + k;
			byte b = (byte) a;
			bbt[p] = b;
			System.out.format("%02X\n", bbt[p]);
		}
		return bbt;
	}

	private static byte asc_to_bcd(byte asc) {
		byte bcd;

		if ((asc >= '0') && (asc <= '9'))
			bcd = (byte) (asc - '0');
		else if ((asc >= 'A') && (asc <= 'F'))
			bcd = (byte) (asc - 'A' + 10);
		else if ((asc >= 'a') && (asc <= 'f'))
			bcd = (byte) (asc - 'a' + 10);
		else
			bcd = (byte) (asc - 48);
		return bcd;
	}

	private static byte[] ASCII_To_BCD(byte[] ascii, int asc_len) {
		byte[] bcd = new byte[asc_len / 2];
		int j = 0;
		for (int i = 0; i < (asc_len + 1) / 2; i++) {
			bcd[i] = asc_to_bcd(ascii[j++]);
			bcd[i] = (byte) (((j >= asc_len) ? 0x00 : asc_to_bcd(ascii[j++])) + (bcd[i] << 4));
			System.out.format("%02X\n", bcd[i]);
		}
		return bcd;
	}

	/**
	 * bcd转String
	 * @param bytes
	 * @return
	 */
	public static String bcd2Str(byte[] bytes) {
		char temp[] = new char[bytes.length * 2], val;

		for (int i = 0; i < bytes.length; i++) {
			val = (char) (((bytes[i] & 0xf0) >> 4) & 0x0f);
			temp[i * 2] = (char) (val > 9 ? val + 'A' - 10 : val + '0');

			val = (char) (bytes[i] & 0x0f);
			temp[i * 2 + 1] = (char) (val > 9 ? val + 'A' - 10 : val + '0');
		}
		return new String(temp);
	}


	public static int dp2pixel(int dpvalue) {
		//Log.i("SNR", mDisplayMetrics.densityDpi + "");
		if (mDisplayMetrics == null)
			return 0;
		return (int) ((mDisplayMetrics.densityDpi / 160.0f) * dpvalue);
	}


	private static DisplayMetrics mDisplayMetrics = null;


	public static DisplayMetrics getmDisplayMetrics() {
		return mDisplayMetrics;
	}

	public static void setmDisplayMetrics(DisplayMetrics mDisplayMetrics) {
		Utils.mDisplayMetrics = mDisplayMetrics;
	}

}
