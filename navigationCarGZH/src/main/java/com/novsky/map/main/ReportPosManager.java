package com.novsky.map.main;

import android.location.BDLocationReport;


public class ReportPosManager {
	
	 private static ReportPosListener listen=null;
	 
	 
	 public  static void  addEventListener(ReportPosListener listener){
		 listen=listener;
	 }
	 
	 /**
	  * 收到位置报告
	  */
	 public static void receiverReportPos(BDLocationReport location){
		 if(listen!=null)	{	
			 listen.reportPos(location);
		 }
	 }
	 

}
