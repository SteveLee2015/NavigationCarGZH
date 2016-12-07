package com.mapabc.android.activity.listener;


import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.mapabc.android.activity.R;
import com.mapabc.android.activity.utils.ToolsUtils;
import com.mapabc.naviapi.FavoriteAPI;
import com.mapabc.naviapi.favorite.FavoriteInfo;
import com.mapabc.naviapi.search.SearchResultInfo;
import com.mapabc.naviapi.type.Const;

public class AddFavoriteListener implements OnClickListener {
	private SearchResultInfo poiInfo;
	public AddFavoriteListener(SearchResultInfo poi){

		this.poiInfo = poi;
	}

	public void onClick(View v) {
		if(FavoriteAPI.getInstance().getFavoriteCount(Const.FAVORITE_HAUNT)>=200){
    		ToolsUtils.showTipInfo(v.getContext(), R.string.favorite_full_tip);
    	}else{
    		Log.e("AddFavoriteListener", "======poiInfo.iadcode======"+poiInfo.adCode);
    		FavoriteInfo favoriteInfo = new FavoriteInfo();
    		favoriteInfo.type = Const.FAVORITE_HAUNT;
    		ToolsUtils.getFavoriteInfo(poiInfo, favoriteInfo);
    		Log.e("AddFavoriteListener", "======favoriteInfo.iadcode======"+favoriteInfo.adCode);
    		
    		if(FavoriteAPI.getInstance().addFavorite(favoriteInfo)>0){
    			ToolsUtils.showTipInfo(v.getContext(), R.string.favorite_successfully);
    		}else{
    			ToolsUtils.showTipInfo(v.getContext(), R.string.favorite_successfail);
    		}
    	}
	}

}
