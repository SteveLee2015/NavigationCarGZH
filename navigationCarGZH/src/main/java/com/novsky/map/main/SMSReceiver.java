package com.novsky.map.main;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.BDLocationReport;
import android.location.BDMessageInfo;
import android.location.BDParameterException;
import android.location.BDRDSSManager;
import android.location.BDUnknownException;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.mapabc.android.activity.NaviStudioActivity;
import com.mapabc.android.activity.R;
import com.novsky.map.util.BDMSG;
import com.novsky.map.util.DatabaseOperation;
import com.novsky.map.util.FriendsLocation;
import com.novsky.map.util.FriendsLocationDatabaseOperation;
import com.novsky.map.util.Utils;
/**
 * 短信接收 
 * @author steve
 */
public class SMSReceiver extends BroadcastReceiver {
	/**
	 * 短信接收日志标识
	 */
	private static String TAG = "SMSReceiver";

	private Context mContext = null;
	
	private BDCommManager manager=null;
	
	private int BD_NOTIFICATION_ID=0;

	private BDFriendLocationOperation friendOperation;

	
	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		Utils.acquireWakeLock(mContext);
		manager=BDCommManager.getInstance(mContext);
		if (friendOperation==null) {
			friendOperation = new BDFriendLocationOperation(context);
		}
		BDMessageInfo info = intent.getParcelableExtra(BDRDSSManager.BDRDSS_MESSAGE);
		if (info != null){
			String msg = "";
			if("S500".equals(Utils.DEVICE_MODEL)){
				msg = new String(info.getMessage());
			}else{
				try {
					msg = new String(info.getMessage(),"GBK");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			Log.i(TAG, "msg="+msg);
			byte[] mMessageArray=null;
			byte[] mMessageArrayBCD=null;
			byte flag1=0;
			byte flag=0;
			//msg
			//F1031EAB9172140B92161E1E531EAB927A22206216200C531EAB9372022A2216202233
			if(msg.length()>1){
				mMessageArray=Utils.hexStringToBytes(msg);
				mMessageArrayBCD=Utils.str2Bcd(msg);
				flag1=mMessageArray[0];
				flag=mMessageArrayBCD[0];
			}
			if(msg.startsWith("$BDNAL")){
				String[] msgArr=msg.split(",");
				//线路
				String lineId=msgArr[1];
				int lineNum=(!"".equals(msgArr[2]))?Integer.valueOf(msgArr[2]):0;
				int lineTotalNum=(!"".equals(msgArr[3]))?Integer.valueOf(msgArr[3]):0;
				int coodrateNum=(!"".equals(msgArr[4]))?Integer.valueOf(msgArr[4]):0;//坐标数
				String passStr="";
				for(int j=0;j<coodrateNum*2;j++){
					if(msgArr[5+j].indexOf("*")>-1){
						passStr+=msgArr[5+j].split("\\*")[0];
					}else{
						passStr+=msgArr[5+j]+",";	
					}
				}
				//保存到数据库,是否需要记录该条数据
				BDLineNavOperation navOper=new BDLineNavOperation(mContext);
				final long id=navOper.insert(lineId,lineNum+"",lineTotalNum+"",passStr);
				boolean isCompletion=navOper.checkLineNavComplete(lineId);
				if(isCompletion){
					BDLineNav line=navOper.get(lineId);
					Intent receiverIntent1=new Intent();
				    receiverIntent1.putExtra("NAVILINEID", line.getLineId());
				    receiverIntent1.setAction("com.bd.action.NAVI_LINE_ACTION");
				    mContext.sendBroadcast(receiverIntent1);
					//Toast.makeText(mContext, ""+line.getLineId()+","+line.getPassPointsString(), Toast.LENGTH_LONG).show();
					NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
					Notification notification = new Notification();
					notification.tickerText = info.getmUserAddress();
					notification.icon = R.drawable.title_msg_flag;
					notification.when = System.currentTimeMillis();
					notification.defaults = Notification.DEFAULT_SOUND;
					notification.flags = Notification.FLAG_AUTO_CANCEL;
					/*点击该通知后要跳转的Activity*/
					Intent notificationIntent = new Intent(mContext,NaviStudioActivity.class);
					//Bundle mBundle=new Bundle();
					//mBundle.putLong("LINE_ID", (!"".equals(lineId))?Integer.valueOf(lineId):0);
					//notificationIntent.putExtras(mBundle);
					notificationIntent.putExtra("LINE_ID", (!"".equals(lineId))?Integer.valueOf(lineId):0);
					PendingIntent contentIntent = PendingIntent.getActivity(mContext,0,notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					notification.setLatestEventInfo(mContext,new String("来自 " + info.getmUserAddress() + " 信息"),"接收到从指挥机上发送的路线导航,是否进入导航?", contentIntent);
					BD_NOTIFICATION_ID++;
					mNotificationManager.notify(BD_NOTIFICATION_ID%10, notification);
				}else{
					//发送回执命令
					String mMessageContenet="$BDNAR,"+lineId+","+lineNum+","+lineTotalNum+"*41";
					try{
						manager.sendSMSCmdBDV21(info.mUserAddress,1,Utils.checkMsg(mMessageContenet),"N", mMessageContenet);
					} catch (BDUnknownException e) {
						e.printStackTrace();
					} catch (BDParameterException e) {
						e.printStackTrace();
					}
				}
				navOper.close();
			}else if(msg.startsWith("$BDNAC")){
				String[] msgArr=msg.split(",");
				//线路
				String lineId=msgArr[1];
				//目的点
				String targetLonStr=msgArr[2];
				String targetLatStr=msgArr[3];
				String passStr="";
				String avaidStr="";
				//必经点的数目
				int passPointNum=(!"".equals(msgArr[4]))?Integer.valueOf(msgArr[4]):0;
				int index=0;
				if(passPointNum!=0){
					for(int i=0;i<passPointNum*2;i++){
						passStr+=(msgArr[5+i]+",");
					}
					index=5+passPointNum*2;
				}else{
				    index=6;	
				}
				int notGoPointNum=(!"".equals(msgArr[index])?Integer.valueOf(msgArr[index]):0);
				index++;
				for(int j=0;j<notGoPointNum*2;j++){
					if(msgArr[index+j].indexOf("*")>-1){
						avaidStr+=msgArr[index+j].split("\\*")[0];
					}else{
						avaidStr+=msgArr[index+j]+",";	
					}
				}
				//保存到数据库,是否需要记录该条数据
				BDInstructionNavOperation navOper=new BDInstructionNavOperation(mContext);
				final long id=navOper.insert(lineId,targetLonStr+","+targetLatStr,passStr,avaidStr);
				navOper.close();
				if(id>0){
//				   //提示有一条指令导航
//				   AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
//				   builder.setTitle("提示");
//				   builder.setMessage("接收到从指挥机上发送的指令导航,是否进入导航?");
//				   builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						
//					}
//				   });
//				   builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						  //Intent mIntent=new Intent();
//						BDInstructionNavOperation navOper=new BDInstructionNavOperation(mContext);
//						BDInstructionNav nav=navOper.get(id);
//						Toast.makeText(mContext, ""+nav.getLineId()+","+nav.getTargetPoint().toString(), Toast.LENGTH_LONG).show();
//					}
//				   });
//				  builder.create().show();
					/* 发送Notification*/
					BDInstructionNavOperation navOper1=new BDInstructionNavOperation(mContext);
					BDInstructionNav nav=navOper1.get(id);
					navOper1.close();
					
					Intent receiverIntent1=new Intent();
					receiverIntent1.putExtra("BDINSTRLINEID", nav.getRowId());
					receiverIntent1.setAction("com.bd.action.BD_INSTR_LINE_ACTION");
					mContext.sendBroadcast(receiverIntent1);
					
					NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
					Notification notification = new Notification();
					notification.tickerText = info.getmUserAddress();
					notification.icon = R.drawable.title_msg_flag;
					notification.when = System.currentTimeMillis();
					notification.defaults = Notification.DEFAULT_SOUND;
					notification.flags = Notification.FLAG_AUTO_CANCEL;
					/* 点击该通知后要跳转的Activity*/
					Intent notificationIntent = new Intent(mContext,NaviStudioActivity.class);
					Bundle mBundle=new Bundle();
					mBundle.putLong("NAVI_ID",id);
					notificationIntent.putExtras(mBundle);
					PendingIntent contentIntent = PendingIntent.getActivity(mContext,0,notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					notification.setLatestEventInfo(mContext,new String("来自 " + info.getmUserAddress() + " 信息"),"接收到从指挥机上发送的指令导航,是否进入导航?", contentIntent);
					BD_NOTIFICATION_ID++;
					mNotificationManager.notify(BD_NOTIFICATION_ID%10, notification);
			   }
			}else if(msg.startsWith("$ZZHT")){
				String[] strArr=msg.split(",");
				String zzTime=strArr[0];
				String bjTime=strArr[1];
				String zzYear=zzTime.substring(5, 9); 
				String zzMonth=zzTime.substring(9, 11);
				String zzDay=zzTime.substring(11,13);
				String zzHour=zzTime.substring(13,15);
				String zzMinute=zzTime.substring(15,17);
				String zzSecond=zzTime.substring(17,19);
				String zzStep=zzTime.substring(19,21);
				String bjYear=bjTime.substring(0, 4); 
				String bjMonth=bjTime.substring(4, 6);
				String bjDay=bjTime.substring(6,8);
				String bjHour=bjTime.substring(8,10);
				String bjMinute=bjTime.substring(10,12);
				String bjSecond=bjTime.substring(12,14);
				String zzDateStr=zzYear+"-"+zzMonth+"-"+zzDay+" "+zzHour+":"+zzMinute+":"+zzSecond;
				String bjDateStr=bjYear+"-"+bjMonth+"-"+bjDay+" "+bjHour+":"+bjMinute+":"+bjSecond;
				//存储作战时间、作战步长、北京时间
				SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try{ 
					Date zzDate=sdf.parse(zzDateStr);
					Date bjDate=sdf.parse(bjDateStr);
				    SharedPreferences prefs=mContext.getSharedPreferences("BD_ZUO_ZHAN_TIME_PREFS", 0);
				    prefs.edit().putLong("BD_ZUO_ZHAN_TIME", zzDate.getTime()).commit();
				    prefs.edit().putLong("BD_BJ_TIME", bjDate.getTime()).commit();
				    prefs.edit().putInt("BD_ZUO_ZHAN_STEP", Integer.valueOf(zzStep)).commit();
				} catch (ParseException e) {
					e.printStackTrace();
				}
				AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
				builder.setTitle("提示");
				builder.setMessage("接收到平台发送的作战时间!");
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					    arg0.dismiss();	
					}
				});
				Dialog dialog=builder.create();
				dialog.setCancelable(false);
				dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				dialog.show();
		   }else{
				/*位置报告*/
				if (flag1==(byte)0xA0){
					Utils.BD_MANAGER_PAGER_INDEX=6;
					BDLocationReport report=new BDLocationReport();
					byte[] mTimeArray=new byte[4];
					mTimeArray[0]=0;
					mTimeArray[1]=mMessageArray[1];
					mTimeArray[2]=mMessageArray[2];
					mTimeArray[3]=mMessageArray[3];
					int mReportTime=Utils.byteArrayToInt(mTimeArray);
	                int mHour=(mReportTime>>19)&0x1F;
	                int mMinute=(mReportTime>>13)&0x3F;
	                int mSecond=(mReportTime>>7)&0x3F;
	                int mMilliSec=mReportTime&0x7F;
					String time=mHour+":"+mMinute+":"+mSecond;
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					report.setReportTime(sdf.format(new Date())+" "+time);
	                /*位置报告经度*/
	                byte[] mLonReport=new byte[4];
	                mLonReport[0]=0;
	                mLonReport[1]=mMessageArray[4];
	                mLonReport[2]=mMessageArray[5];
	                mLonReport[3]=mMessageArray[6];
	                int mReportLon=Utils.byteArrayToInt(mLonReport);
	                int[] mLonReportArray=new int[4];
	                mLonReportArray[0]=(mReportLon>>16)&0xFF;
	                mLonReportArray[1]=(mReportLon>>10)&0x3F;
	                mLonReportArray[2]=(mReportLon>>4)&0x3F;
	                mLonReportArray[3]=mReportLon&0x0F;
	                report.setLongitude(Utils.mTranslateLonLatUnit(mLonReportArray));				
	                /*位置报告纬度*/
	                byte[] mLatReport=new byte[4];
	                mLatReport[0]=0;
	                mLatReport[1]=mMessageArray[7];
	                mLatReport[2]=mMessageArray[8];
	                mLatReport[3]=mMessageArray[9];
	                int mReportLat=Utils.byteArrayToInt(mLatReport);
	                int[] mLatReportArray=new int[4];
	                mLatReportArray[0]=(mReportLat>>18)&0x3F;
	                mLatReportArray[1]=(mReportLat>>12)&0x3F;
	                mLatReportArray[2]=(mReportLat>>6)&0x3F;
	                mLatReportArray[3]=(mReportLat>>2)&0x0F;
	                report.setLatitude(Utils.mTranslateLonLatUnit(mLatReportArray));
	                /*高程类型*/
	                int mReportHeightType=mReportLat&0x3;
	                byte[] mHeightReportArray=new byte[4];
	                mHeightReportArray[0]=0;
	                mHeightReportArray[1]=mMessageArray[10];
	                mHeightReportArray[2]=mMessageArray[11];
	                mHeightReportArray[3]=mMessageArray[12];
	                int mReportHeight=Utils.byteArrayToInt(mHeightReportArray);
	                int mReportHeightFlag=(mReportHeight>>23)&0x1;
	                int mReportHeightValue=(mReportHeight>>9)&0x3FFF;
	                //高程异常整数符号
	                int mReportHeightExpFlag=(mReportHeight)&0x1;
	                //高程异常数值
	                int mReportHeightExpValue=(mReportHeight)&0xFF;
	                report.setUserAddress(info.getmUserAddress());
	                report.setHeight((mReportHeightFlag==0)?mReportHeightValue:(-mReportHeightValue));
	                report.setHeightUnit("m");
					/* 1.位置报告保存的数据库 */
					boolean isAdd =mAddLocationReportToDatabase(report);
					/* 2.采用NotificationManager来显示 */
					Utils.mShowLocationReportNotification(context,report);
					if (Utils.checkNaviMap) {
						ReportPosManager.receiverReportPos(report);
					}
				}else if (flag1==(byte)0xA2){
					Intent mIntent=new Intent();
					mIntent.setClassName(mContext, "com.bd.comm.protocol.BDLocationService");
					mContext.startService(mIntent);
				}else if (msg.startsWith("F1")) {//1个字节
					//发送时间
					String sendTimeStr = info.getmSendTime();
					if (sendTimeStr.isEmpty()) {
						long time=System.currentTimeMillis();
						Date date1 = new Date(time);  
						 SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");    
						 String str1 = sdf1.format(date1); 
						sendTimeStr = str1;
					}
					
					//F1 
					//03 16进制转为10进制
					//1EAB91 72140B92 161E1E53 
					//1EAB92 7A222062 16200C53
					//1EAB93 72022A22 16202233
					//海力特 友邻位置  自定义协议
					//1字节
					String countString = msg.substring(2, 4);
					int count =Integer.parseInt(countString);
							
					int length = 4+count*22;
					if (msg.length()!=length) {
						// 出错
						storeMsg(context, info, msg);
						Toast.makeText(mContext, "位置报告格式不正确!!", 0).show();
						return;
					}
					
					//循环
					
					ArrayList<FriendBDPoint> friendBDPoints = new ArrayList<FriendBDPoint>();
					for (int i = 0; i < count; i++) {
						
						//是否在地图显示 ""标示不在地图显示
						FriendBDPoint friend = new FriendBDPoint();
						
						//3字节 友邻id
						String friendIDStr = msg.substring(4+i*22, 10+i*22);
						//将string 转为  16进制 转为10进制
						//int parseInt = Integer.parseInt(friendIDStr, 10);
						Long parseLong = Long.parseLong(friendIDStr, 16);
						String s222 = Long.toString(parseLong);  
						
						//String hexString = Long.toHexString(parseLong);
						
						
						
						String s = "7890abcd";  
						String s1 = Integer.toString(Integer.parseInt(s, 16));  
						String s2 = Long.toString(Long.parseLong(s, 16));  
						
						//16进制 转  10进制
						
						
						//4字节 经度
						//度
						String longitudeStr1 = msg.substring(10+i*22, 12+i*22);
						int longitude1 = Integer.parseInt(longitudeStr1, 16);
						//分
						String longitudeStr2 = msg.substring(12+i*22, 14+i*22);
						int longitude2 = Integer.parseInt(longitudeStr2, 16);
						//秒
						String longitudeStr3 =msg.substring(14+i*22, 16+i*22);
						int longitude3 = Integer.parseInt(longitudeStr3, 16);
						//方向 1e2w3s4n
						String longitudeStr4 =msg.substring(16+i*22, 18+i*22);
						int longitude4 = Integer.parseInt(longitudeStr4, 16);
						
						String binaryString = Integer.toBinaryString(longitude4);
						//如果不足8 位呢
						binaryString = complete8(binaryString);
						String hight4Lon = binaryString.substring(0, 4);
						String low4Lon = binaryString.substring(4, 8);
						String longitude5 = Integer.valueOf(hight4Lon,2).toString();
						String EW = Integer.valueOf(low4Lon,2).toString();
						if (1==Integer.parseInt(EW)) {//e
							friend.setLonDirection("E"+"");
						}else if (2==Integer.parseInt(EW)) {//w
							friend.setLonDirection("W"+"");
							
						}
						
						//将度分秒 转化为 小数点度
						int longArray[] = {longitude1,longitude2,longitude3,Integer.parseInt(longitude5)};
						double lontitute = Utils.mTranslateLonLatUnit(longArray);
						
						
						
						//4字节 纬度  高4位为小秒  低4位为方向
						//度
						String latitudeStr1 = msg.substring(18+i*22, 20+i*22);
						int latitude1 = Integer.parseInt(latitudeStr1, 16);
						//分
						String latitudeStr2 = msg.substring(20+i*22, 22+i*22);
						int latitude2 = Integer.parseInt(latitudeStr2, 16);
						//秒
						String latitudeStr3 = msg.substring(22+i*22, 24+i*22);
						int latitude3 = Integer.parseInt(latitudeStr3, 16);
						//方向 1e2w3s4n
						String latitudeStr4 = msg.substring(24+i*22, 26+i*22);
						int latitude4 = Integer.parseInt(latitudeStr4, 16);
						
						String binaryString2 = Integer.toBinaryString(latitude4);
						binaryString2 = complete8(binaryString2);
						String hight4Lat = binaryString2.substring(0, 4);
						String low4Lat = binaryString2.substring(4, 8);
						
						String latitude5 = Integer.valueOf(hight4Lat,2).toString();
						String NS = Integer.valueOf(low4Lat,2).toString();
						
						if (3==Integer.parseInt(NS)) {//S
							friend.setLatDirection("S"+"");
						}else if (4==Integer.parseInt(NS)) {//N
							friend.setLatDirection("N"+"");
						}
						
						
						//将度分秒 转化为 小数点度
						int latArray[] = {latitude1,latitude2,latitude3,Integer.parseInt(latitude5)};
						double latitute = Utils.mTranslateLonLatUnit(latArray);
						
						//数据封装
						
						friend.setReceiveTime(sendTimeStr);
						friend.setCurrentID(i+"");
						friend.setFriendCount(count+"");
						friend.setFriendID(parseLong+"");
						friend.setLon(lontitute+"");
						friend.setLat(latitute+"");
						
						//保存到数据库
						long insertID = friendOperation.insert(friend);
						//添加到集合
						friendBDPoints.add(friend);
					}
					
					// 发送广播
					//TODO
					Intent receiverIntent = new Intent();
					receiverIntent.putExtra("sendTimeStr", sendTimeStr);
					receiverIntent
							.setAction("com.bd.action.FRIEND_LOCATIONMESSAGE_ACTION");
					context.sendBroadcast(receiverIntent);
					Utils.mShowMessageNotification2(context, info, msg,friendBDPoints);
					
				}else{
					
				   storeMsg(context, info, msg);
				}
			}
		}
	}

	private String complete8(String binaryString) {
		if (binaryString.length()<8) {
			int toAdd = 8-binaryString.length();
			for (int j = 0; j < toAdd; j++) {
				
				 binaryString = "0"+binaryString ;
			}
			
		}
		return binaryString;
	}

	/**
	 * 保存短报文
	 * @param context
	 * @param info
	 * @param msg
	 */
	private void storeMsg(Context context, BDMessageInfo info, String msg) {
		long rowId=saveMessageToDataBase(context, info,msg);
		   Utils.BD_MESSAGE_PAGER_INDEX=2;
		   //发送广播
		   Intent receiverIntent=new Intent();
		   receiverIntent.putExtra("ROWID", rowId);
		   receiverIntent.setAction("com.bd.action.MESSAGE_ACTION");
		   context.sendBroadcast(receiverIntent);
		   Utils.mShowMessageNotification(context, info,msg);
	}

	/**
	 * 保存收到短信至数据库中
	 * @param context
	 * @param info
	 */
	private long saveMessageToDataBase(Context context, BDMessageInfo info,String messagContent) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(new Date());
		DatabaseOperation oper = new DatabaseOperation(context);
		BDMSG bDMsg = new BDMSG();
		bDMsg.setColumnsUserAddress(info.getmUserAddress());
		bDMsg.setColumnsMsgType(info.getMsgType() + "," + info.getMsgCharset());
		bDMsg.setColumnsSendAddress(info.getmUserAddress());
		bDMsg.setColumnsSendTime(date);
		bDMsg.setColumnsMsgLen(String.valueOf(messagContent.length()));
		bDMsg.setColumnsMsgContent(messagContent);
		bDMsg.setColumnsCrc("");
		bDMsg.setColumnsMsgFlag("3");
		long rowId=oper.insert(bDMsg);
		oper.close();
		return rowId;
	}

	/**
	 * 保存友邻位置到数据库
	 * @param report
	 * @return
	 */
	private boolean mAddLocationReportToDatabase(BDLocationReport report) {
		FriendsLocationDatabaseOperation oper = new FriendsLocationDatabaseOperation(mContext);
		FriendsLocation fl = new FriendsLocation();
		fl.setUserId(report.mUserAddress);
		fl.setLat(String.valueOf(report.mLatitude));
		fl.setLon(String.valueOf(report.mLongitude));
		fl.setHeight(String.valueOf(report.mHeight));
		fl.setReportTime(report.mReportTime);
		boolean isTrue = oper.insert(fl);
		oper.close();
		return isTrue;
	}
	
}
