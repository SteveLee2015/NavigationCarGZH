package com.novsky.map.main;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapabc.android.activity.R;
import com.novsky.map.util.OnCustomListListener;

public  class CustomListView  extends LinearLayout {

	private String TAG="CustomListView";
    private Context mContext=null;
    private ImageView  customLeftImage=null;
    private TextView   customContent=null;
    private ImageView  customRightImage=null;
    private int index=0;
    private String[]  data=null;
    private OnCustomListListener listener=null;
    
    public CustomListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext=context;
		init();
	}
    
	/**
	 * 初始化组件
	 */
	public void init(){ 
		 LayoutInflater.from(mContext).inflate(R.layout.custom_list_view ,this,true);
		 customLeftImage=(ImageView)this.findViewById(R.id.custom_list_lef_image);
		 customContent=(TextView)this.findViewById(R.id.custom_list_content);
		 customRightImage=(ImageView)this.findViewById(R.id.custom_list_right_image);
		
		 customLeftImage.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				if(index==0){
					index=data.length-1;
				}else{
					index--;
				}
				customContent.setText(data[index]);
				listener.onListIndex(index);
			}
		});
		customRightImage.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(index==data.length-1){
					index=0;
				}else{
					index++;
				}
				customContent.setText(data[index]);
				listener.onListIndex(index);
			}
		}) ;
	}
	/**
	 * 自定义ListView设置值
	 * @param data
	 */
	public void setData(String[] data){
		if(data!=null){
			this.data=data;
			customContent.setText(data[index]);
		}else{
		   	//DswLog.log(TAG, "SetData() input data is null!", 'i');
		}
	}
	/**
	 * 获得当前选中数据的index值
	 * @return
	 */
	public int getCurrentIndex(){
		return index;
	}
    /**
     * 设置监听
     * @param listener
     */
	public void setOnCustomListener(OnCustomListListener listener) {
		this.listener=listener;
	}
	
	/**
	 * 是否可用的方法
	 * @param istrue
	 */
	public void setCustomEnable(boolean istrue){
		if(istrue){
			customRightImage.setEnabled(true);
			customLeftImage.setEnabled(true);
			customContent.setEnabled(true);
		}else{
			customRightImage.setEnabled(false);
			customLeftImage.setEnabled(false);
			customContent.setEnabled(false);
		}
	}
	/**
	 * 设置
	 * @param i
	 */
	 public void setIndex(int i){
		 if(i<data.length){
			 index=i;
			 customContent.setText(data[i]); 
		 }
	 }
	
	 public void setIndex(String value){
		 if(value==null||"".equals(value)){
			 return ;
		 }
		 for(int i=0;i<data.length;i++){
			 if(data[i].equals(value)){
				 index=i;
				 customContent.setText(value);
				 break;
			 }
		 }
	 }
}
