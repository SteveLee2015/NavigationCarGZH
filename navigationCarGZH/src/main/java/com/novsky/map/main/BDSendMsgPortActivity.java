package com.novsky.map.main;


import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;

import com.mapabc.android.activity.R;
import com.novsky.map.util.TabSwitchActivityData;
import com.novsky.map.util.Utils;

public class BDSendMsgPortActivity extends TabActivity {

	private TabHost.TabSpec spec1=null;
	private TabHost.TabSpec spec2=null;
	private TabHost.TabSpec spec3=null;
	private TabHost.TabSpec spec4=null;
	private TabHost.TabSpec spec5=null;
	public static TabHost tabs=null;
	private Context mContext=this;
	private final int REQUEST_CONTACT=1;	
	private CustomLocationManager manager=null;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bdsend_msg_port);		
		tabs=this.getTabHost();
		LinearLayout ll = (LinearLayout)tabs.getChildAt(0);  
		TabWidget tw = (TabWidget)ll.getChildAt(0);
		Resources mResource=this.getResources();
		
		/*新建短信*/
		final LinearLayout tabIndicator1= (LinearLayout)LayoutInflater.from(this).
		inflate(R.layout.activit_bd_tab_layout, tw, false);  
		 TextView tvTab1 = (TextView)tabIndicator1.getChildAt(0);  
		tvTab1.setText(mResource.getString(R.string.msg_comm_reqeust));
		spec1 = tabs.newTabSpec("newmsg").setIndicator(tabIndicator1)  
		       .setContent(new Intent(this,SendMsgRequestActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));  
		tabs.addTab(spec1);
				
		/*收件箱*/
		final LinearLayout tabIndicator2= (LinearLayout)LayoutInflater.from(this).
	    inflate(R.layout.activit_bd_tab_layout, tw, false); 
		TextView tvTab2 = (TextView)tabIndicator2.getChildAt(0);  
		tvTab2.setText(mResource.getString(R.string.bd_msg_list));
		spec2 = tabs.newTabSpec("msglist").setIndicator(tabIndicator2)  
		  .setContent(new Intent(this,ReceiveMsgActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));  
		tabs.addTab(spec2);
		
		/*常用短信息*/
		final LinearLayout tabIndicator3= (LinearLayout)LayoutInflater.from(this).
				inflate(R.layout.activit_bd_tab_layout, tw, false);  
		 TextView tvTab3 = (TextView)tabIndicator3.getChildAt(0);  
		tvTab3.setText(mResource.getString(R.string.bd_instr_comm));
		Intent intent=new Intent(this,NaviTaskActivity.class);
		spec3 = tabs.newTabSpec("addressbook").setIndicator(tabIndicator3).setContent(intent);
		tabs.addTab(spec3);

        /*线路导航*/
		final LinearLayout tabIndicator4= (LinearLayout)LayoutInflater.from(this).
				inflate(R.layout.activit_bd_tab_layout, tw, false); 
		TextView tvTab4 = (TextView)tabIndicator4.getChildAt(0);  
		tvTab4.setText(mResource.getString(R.string.bd_line_comm));
		spec4 = tabs.newTabSpec("researchmsg").setIndicator(tabIndicator4)  
		        .setContent(new Intent(this,LineTaskActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));  
		tabs.addTab(spec4);	
		
		/*友邻位置*/
		final LinearLayout tabIndicator5= (LinearLayout)LayoutInflater.from(this).
				inflate(R.layout.activit_bd_tab_layout, tw, false); 
		TextView tvTab5 = (TextView)tabIndicator5.getChildAt(0);  
		tvTab5.setText(mResource.getString(R.string.friend_Location_comm));
		spec5 = tabs.newTabSpec("friendLocation").setIndicator(tabIndicator5)  
				.setContent(new Intent(this,LineTaskActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));  
		tabs.addTab(spec5);	
		
		
		
		tabs.setOnTabChangedListener(new OnTabChangeListener() {
				@Override
				public void onTabChanged(String tag) {
			       
					  if(tag.equals("addressbook")){
						   
					  }
				}
		 });		
		manager=new CustomLocationManager(mContext);
		manager.initLocation();
	}
	
    @Override
    protected void onResume() {
    	super.onResume();
    	TabSwitchActivityData mInstance = TabSwitchActivityData.getInstance();
    	if(Utils.smsNotificationShow){
    		TabSwitchActivityData.getInstance().setTabFlag(1);
    		Utils.smsNotificationShow=false;
    	}
    	if(tabs!=null){
  	        tabs.setCurrentTab(mInstance.getTabFlag());
    	}
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	Activity subActivity = getLocalActivityManager().getCurrentActivity(); 
    	 Log.i("BDSendMsgPortActivity", ""+requestCode);
    	//判断是否实现返回值接口 
    	if (subActivity instanceof OnTabActivityResultListener) { 
             //获取返回值接口实例             
    		OnTabActivityResultListener listener = (OnTabActivityResultListener) subActivity;
            //转发请求到子Activity             
    		listener.onTabActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		manager.initLocation();
	}
}
