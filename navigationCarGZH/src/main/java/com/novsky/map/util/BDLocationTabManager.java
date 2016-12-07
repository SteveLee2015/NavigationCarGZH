package com.novsky.map.util;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.mapabc.android.activity.R;

/**
 * 北斗定位Tab管理类
 * @author steve
 */
public class BDLocationTabManager {

	private  Context mContext=null;
	
	/**
	 * 显示RDSS定位的TabSpec
	 */
	private  TabHost.TabSpec tabSpec= null;
	
	private  TabWidget tabWidget=null;
	
	private TabHost tabHost=null;
	
	private int  index=0;
	
	public BDLocationTabManager(Context mContext,TabHost tabHost){
		this.mContext=mContext;
		this.tabHost=tabHost;
		LinearLayout ll = (LinearLayout) tabHost.getChildAt(0);
		tabWidget=(TabWidget) ll.getChildAt(0);
	}
	
	
	/**
	 * 增加Tab
	 * @param tabName  tab名称
	 * @param tabSpecName 
	 * @param clazz 加载的类
	 */
	public void  addTab(String tabName,String tabSpecName,Class clazz){		
			index++;
		    final LinearLayout tabIndicator= (LinearLayout)LayoutInflater.from(
					mContext).inflate(R.layout.activit_bd_tab_layout, tabWidget, false);
			TextView tvTab1 = (TextView) tabIndicator.getChildAt(0);
			tvTab1.setText(tabSpecName);
			tabSpec= tabHost.newTabSpec(tabName+index).setIndicator(tabIndicator).setContent(new Intent(mContext,clazz).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			tabHost.addTab(tabSpec);
	}
	
	public void setCurrentPageIndex(int index){
		tabHost.setCurrentTab(index);
	}
}
