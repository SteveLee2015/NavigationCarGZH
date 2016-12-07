package com.novsky.map.util;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

/**
 * 日志信息写入文件�?
 * @author steven
 */
public class DswLog {

	/**
	 * 输入日志类型，w代表只输出警告信�?v代表输出�?��信息
	 */
	private static char LOG_TYPE='v';
	
	public static char LOG_INFO='i';
	
	public static char LOG_WARNNING='w';
	
	public static char LOG_ERROR='e';
	
	public static char LOG_DEBUG='d';
	
	/**
	 * 日志总开�?
	 */
	private static Boolean LOG_SWITH=true;
	/**
	 * 日志写入文件�?��
	 */
	private static Boolean LOG_WRITE_TO_FILE=true;
	/**
	 * SD卡中日志文件的最多保存天�?
	 */
	private static int SDCARD_LOG_FILE_SAVE_DAYS=0;
	/**
	 * 本类输出的日志文件名�?
	 */
	private static String LOGFILENAME="Log.txt";
	/**
	 * 日志文件在sdcard中的路径
	 */
	private static String LOG_PATH_SDCARD_DIR=Environment.getExternalStorageDirectory()+"/log/";
	
	 /**
	  * 日志的输出格�?
	  */
	private static SimpleDateFormat logSdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * 日志文件格式
	 */
	private static SimpleDateFormat logFile=new SimpleDateFormat("yyyy-MM-dd");
	
	public static void d(String tag,String text){
		log(tag,text,'W');
	}
	
	/**
	 * 根据TAG,MSG和等级，输出日志
	 */
	public static void log(String tag,String msg,char level){
		if(LOG_SWITH){
			switch (level) {
				case 'i':
					Log.i(tag, msg);
					break;
				case 'e':
					Log.e(tag, msg);
					break;
				case 'w':
					Log.w(tag, msg);
					break;
				case 'd':
					Log.d(tag, msg);
					break;
				default:
					Log.v(tag, msg);
					break;
			}
			if(LOG_WRITE_TO_FILE){
			    writeLogToFile(String.valueOf(level),tag,msg);	
			}
		}
	}
	/**
	 * 日志写入文件�?
	 * @param mylogtype
	 * @param tag
	 * @param text
	 */
	private static void writeLogToFile(String mylogtype,String tag,String text){
		Date nowTime=new Date();
		String needWriteFile=logFile.format(nowTime);
		String needWriteMessage=logSdf.format(nowTime)+" "+mylogtype+" "+tag+" "+text;
		/**
		 * 首先判断目录是否存在
		 */
		File file=new File(LOG_PATH_SDCARD_DIR,needWriteFile+LOGFILENAME);
		file.getParentFile().mkdirs();
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/**
		 * 后面这个参数代表是不是要接上文件中原来的数据,不进行覆�?
		 */
		try {
			FileWriter fileWriter=new FileWriter(file,true);
			BufferedWriter bufWriter=new BufferedWriter(fileWriter);
			
			bufWriter.write(needWriteMessage);
			bufWriter.newLine();
			bufWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void delFile(){
		String needDelFile=logFile.format(getDateBefore());
		File file=new File(LOG_PATH_SDCARD_DIR,needDelFile+LOGFILENAME);
		if(file.exists()){
		    file.delete();	
		}
	}
	
    /**
     * 得到现在时间前的几天日期,用来得到�?��删除的日志文件名
     * @return
     */
	private static Date getDateBefore(){
		Date nowTime=new Date();
		Calendar now=Calendar.getInstance();
		now.setTime(nowTime);
		now.set(Calendar.DATE, now.get(Calendar.DATE)-SDCARD_LOG_FILE_SAVE_DAYS);
		return now.getTime();
	}
	
	
}
