package com.novsky.map.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Message;

import com.novsky.map.main.BDSendReportListener;
/**
 * 时间Dialog
 * @author steve
 */
public class TimerDialog {
	
	private static final int TYPE_POSITIVE = 1;
	private static final int TYPE_NEGATIVE = 2;
	
	private Context mContext;
	private int count=0;
	private int mCount = 0;
    private AlertDialog mDialog = null;
    private String message="";
    private boolean startFlag=false;
    //private boolean stopFlag=false;
    private String title="";
    private BDSendReportListener listener=null;
    
    public void setOnBDSendReportListener(BDSendReportListener listener){
    	this.listener=listener;
    }
    
	public TimerDialog(Context ctx){
		mContext = ctx;
		mDialog = new AlertDialog.Builder(mContext).create();
	}
	
	public void setMessage(String msg){
		this.message=msg;
		mDialog.setMessage(message);
	}
	
	public void setTitle(int resId){	
          mDialog.setTitle(resId);
	}
	
	public void setTitle(String title){
                this.title=title;
		mDialog.setTitle(title);
	}
	
	public void show(){
		mDialog.setCancelable(false);
		mDialog.show();
	}
	
	public void setPositiveButton(String text, OnClickListener listener){
		mDialog.setButton(Dialog.BUTTON_POSITIVE, text, listener);
	}
	
	public void setNegativeButton(String text, OnClickListener listener){
		mDialog.setButton(Dialog.BUTTON_NEGATIVE, text, listener);
	}
	
	
	
	/**
	 * 开始倒计时
	 * @param count
	 */
	public void startCount(int count){
		if(count <= 0){
			return;
		}
		mCount = count;
		this.count=count;
		if(!startFlag){
			mHandler.sendEmptyMessageDelayed(TYPE_POSITIVE, 200);
			startFlag=true;
		}
	}
	
    
    private Handler mHandler = new Handler(){
    	
		public void handleMessage(Message msg){
			    if(mCount > 0){
        			mCount--;
        			mDialog.setMessage(getTimeText(message,mCount));
        		}else{
        			mCount=count;
        			mDialog.setMessage("正在发送"+title+"!");
        			//发送监听
        			listener.sendLocationReport();
        		}
			    if(startFlag){
     			   mHandler.sendEmptyMessageDelayed(TYPE_NEGATIVE, 1000);
     			}
        }
    };
    
    
    public String getTimeText(String text, int count){
    	if(text != null && text.length() > 0 && count > 0){
    		int index = text.indexOf("(");
    		if(index > 0){
    			text = text.substring(0, index);
    			return (text + "("+count+"秒)");
    		}else{
    			return (text + "("+count+"秒)");
    		}
    	}
    	return text;
    }

    
    public void stop(){
    	 startFlag=false;
    	 //stopFlag=true;
        
    }

   public void dismiss(){
     mDialog.dismiss();
  }
}
