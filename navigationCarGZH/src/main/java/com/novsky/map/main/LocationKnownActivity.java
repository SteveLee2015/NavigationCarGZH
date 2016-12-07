package com.novsky.map.main;


import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.mapabc.android.activity.NaviStudioActivity;
import com.mapabc.android.activity.R;

public class LocationKnownActivity extends TabActivity {

	public static TabHost tabs = null;
	private TabHost.TabSpec spec1 = null;
	private TabHost.TabSpec spec2 = null;
	private Context mContext = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bdsend_msg_port);

		tabs = this.getTabHost();
		LinearLayout ll = (LinearLayout) tabs.getChildAt(0);
		TabWidget tw = (TabWidget) ll.getChildAt(0);
		
        /*地图显示*/  
		final LinearLayout tabIndicator1 = (LinearLayout) LayoutInflater.from(
				this).inflate(R.layout.activit_bd_tab_layout, tw, false);
		TextView tvTab1 = (TextView) tabIndicator1.getChildAt(0);
		tvTab1.setText("地图");
		spec1 = tabs
				.newTabSpec("newmsg")
				.setIndicator(tabIndicator1)
				.setContent(
						new Intent(this,NaviStudioActivity.class)
								);
		tabs.addTab(spec1);
		final LinearLayout tabIndicator2 = (LinearLayout) LayoutInflater.from(
				this).inflate(R.layout.activit_bd_tab_layout, tw, false);
		TextView tvTab2 = (TextView) tabIndicator2.getChildAt(0);
		tvTab2.setText("坐标");
		spec2 = tabs
				.newTabSpec("msglist")
				.setIndicator(tabIndicator2)
				.setContent(
						new Intent(this, BD2LocActivity.class)
								);
		tabs.addTab(spec2);
	}
}
