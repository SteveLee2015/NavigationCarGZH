package com.mapabc.android.activity.listener;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.mapabc.android.activity.base.Constants;
import com.mapabc.naviapi.search.SearchResultInfo;
/**
 * @description: POI显示页面起点按钮监听器
 * @author menglin.cao 2012-08-30
 * @version:
 * @modify:
 * @Copyright: mapabc.com
 */
public class StartPointListener extends POIListener{
	
	private SearchResultInfo poiInfo;

	public StartPointListener(SearchResultInfo poi) {
		this.poiInfo = poi;
	}

	@Override
	public void onClick(View v){
		try {
			Bundle extra = new Bundle();
			extra.putInt(Constants.INTENT_ACTION, Constants.INTENT_TYPE_SETSTARTPOINT);
			extra.putSerializable(Constants.POI_DATA, poiInfo);
			Intent intent = new Intent(Constants.ACTIVITY_NAVISTUDIOACTIVITY);
			intent.putExtras(extra);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			v.getContext().startActivity(intent);
			//ActivityStack.newInstance().cleanHistory();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
