package com.novsky.map.main;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapabc.android.activity.R;
import com.novsky.map.util.DswLog;

public class CustomProgressView extends LinearLayout {

	private Context mContext=null;
	private TextView custom_signal_1=null;
	private TextView custom_signal_2=null;
	private TextView custom_signal_3=null;
	private TextView custom_signal_4=null;
	private TextView[] array=new TextView[4];
	private int num=0;
	
	private String TAG="CustomProgressView";
	
	public CustomProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext=context;
		init();
		
	}
	
	/*初始化方法*/
	public void init(){
		View view=LayoutInflater.from(mContext).inflate(R.layout.custom_progress_view ,this,true);
		custom_signal_1=(TextView)this.findViewById(R.id.progress_signal_1);
		custom_signal_2=(TextView)this.findViewById(R.id.progress_signal_2);
		custom_signal_3=(TextView)this.findViewById(R.id.progress_signal_3);
		custom_signal_4=(TextView)this.findViewById(R.id.progress_signal_4);
		array[0]=custom_signal_1;
		array[1]=custom_signal_2;
		array[2]=custom_signal_3;
		array[3]=custom_signal_4;
	}
	
	public void setProgress(int num){
		
		if(num<=array.length){
			 for(int i=0;i<array.length;i++){
				 if(i<num){
					 array[i].setBackgroundColor(mContext.getResources().getColor(R.color.bs_signal_light));
				 }else{
					 array[i].setBackgroundColor(mContext.getResources().getColor(R.color.bs_signal_un_light));
				 }
			 }
		}else{
			DswLog.log(TAG,"num is morth than the max!", DswLog.LOG_INFO);
		}
	}
}
