
/**
 * Copyright (c) 2010-2100 AutoNavi Software Co., Ltd.  All rights reserved.
 */
/**
 * 
 */
package com.mapabc.android.activity.search;


import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

import com.mapabc.android.activity.BottomBaseActivity;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.base.Constants;
import com.mapabc.android.activity.listener.BackListener;
import com.mapabc.android.activity.search.adapter.TypeExpandListAdapter;
import com.mapabc.android.activity.utils.ActivityStack;
import com.mapabc.android.activity.utils.ToolsUtils;
import com.mapabc.naviapi.MapAPI;
import com.mapabc.naviapi.type.NSLonLat;



public class SearchAroundPOIActivity extends BottomBaseActivity implements OnChildClickListener{
	private static final String TAG="SearchAroundPOIActivity";
	private ExpandableListView chooseTypeExpandableListView;
	private static  int idicateWidth=50;//下拉按钮的宽度
	private NSLonLat center;
	private boolean  isFromTipSearch=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.e(TAG, "========onCreate==========");
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			center = (NSLonLat)bundle.getSerializable(Constants.SEARCHAROUND_CENTER_POINT);
			isFromTipSearch=(Boolean)bundle.getBoolean("fromTipSearch");
		}
		//setContentView(R.layout.searcharound);
		initTop();
		chooseTypeExpandableListView = (ExpandableListView) findViewById(R.id.lv_chooseType);
		chooseTypeExpandableListView.setIndicatorBounds(0, (int) (ToolsUtils.getCurScreenWidth(this)*1.8));
		chooseTypeExpandableListView.setAdapter(new TypeExpandListAdapter());
		idicateWidth = ToolsUtils.getIndicatorPosition(this);
		setIndicatorPosition();
		chooseTypeExpandableListView.setOnChildClickListener(this);
	}

	
	/**
	 * 设置显示器位置
	 */
	public void setIndicatorPosition() {

		DisplayMetrics dm = new DisplayMetrics(); // 取得窗口属性
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		// 窗口的宽度
		int screenWidth = dm.widthPixels;
		chooseTypeExpandableListView.setIndicatorBounds(screenWidth - idicateWidth, screenWidth);
	}

	/**
	 * 初使化顶部控件
	 */
	private void initTop(){
		//TextView topicTextView = (TextView)findViewById(R.id.txtTopic);
		title_name.setText(R.string.searcharoundtype_topic);
		//ImageButton backImageButton = (ImageButton)findViewById(R.id.btnBack);
		back.setOnClickListener(new BackListener(this));
	}



	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		try {
			Bundle bundle = new Bundle();
			bundle.putInt(Constants.SEARCHAROUND_OTYPE, groupPosition);
			bundle.putInt(Constants.SEARCHAROUND_STYPE, childPosition);
			if(center==null){
				center = MapAPI.getInstance().getVehiclePos();
			}
			bundle.putSerializable(Constants.SEARCHAROUND_CENTER_POINT, center);
			Intent intent = new Intent(Constants.ACTIVITY_SEARCHAROUNDRESULT);
			intent.putExtra("SearchArroundPOIActivity", bundle);
			this.startActivity(intent);
		} catch (Exception e) {
			Log.e("ExpandableListView onChildClick: ", e.toString());
		}
		return true;
	}
	@Override
	public void onBackPressed() {
		try{
			ActivityStack.newInstance().pop();
			finish();
			super.onBackPressed();
			if(isFromTipSearch){
				Intent intent = new Intent(Constants.ACTIVITY_NAVISTUDIOACTIVITY);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		}catch(Exception e){
			
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(chooseTypeExpandableListView != null){
			setIndicatorPosition();
		}
	}


	@Override
	protected int getContentView() {
		return R.layout.searcharound;
	}
}

