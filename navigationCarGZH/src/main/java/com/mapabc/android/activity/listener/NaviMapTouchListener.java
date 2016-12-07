package com.mapabc.android.activity.listener;


import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mapabc.android.activity.NaviStudioActivity;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.base.Constants;
import com.mapabc.android.activity.utils.ToolsUtils;
import com.mapabc.naviapi.MapAPI;
import com.mapabc.naviapi.MapView;
import com.mapabc.naviapi.TipParams;
import com.mapabc.naviapi.listener.MapTouchListener;
import com.mapabc.naviapi.type.NSLonLat;
import com.mapabc.naviapi.type.NSPoint;

/**
 * 长按，短按监听
 */
public class NaviMapTouchListener implements MapTouchListener,OnClickListener {
	
	private static final String TAG = "NaviMapTouchListener";
	NaviStudioActivity m_activity;
	MapView m_mapView;
	private LayoutInflater mInflater;
	View tipView;
	TextView tvTipRoadNam;
	ImageButton searchButton,endPointButton;
	private static final int H_SHOW_TIP = 100;// 弹出TIP
	// private static final int H_
    private NSLonLat lonlat = null;
	Handler h = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == H_SHOW_TIP) {
				if (m_mapView.isHasTip()) {
					m_mapView.hideTip();
				}
				NSPoint point = new NSPoint();
				point.x = msg.arg1;
				point.y = msg.arg2;
				NSLonLat lonlat = new NSLonLat();
				MapAPI.getInstance().screenCoordToWorldCoord(point, lonlat);
				NaviMapTouchListener.this.lonlat = lonlat;
				Log.i(TAG,"---------------------------------lonlat.x="+lonlat.x+",lonlat.y="+lonlat.y);
				String roadName = ToolsUtils.getRoadName(lonlat, 100);
				if (roadName.length() > 0) {
					tvTipRoadNam.setText(roadName);
				} else {
					tvTipRoadNam
							.setText(R.string.navimaptouchlistener_noroadname);
				}
				int width = 160;
				int height = 100;
				if(ToolsUtils.getDPI(m_activity)==ToolsUtils.XDPI){
					width = 210;
					height = 130;
				}
				Paint paint = new Paint();
				paint.setTextSize(tvTipRoadNam.getTextSize());
				int size = (int) paint.measureText(tvTipRoadNam.getText().toString());
				width = width + size;
				TipParams params = new TipParams(width, height, lonlat.x, lonlat.y);
				m_mapView.showTip(tipView, params);
			}
		}

	};

	public NaviMapTouchListener(NaviStudioActivity activity, MapView mapView) {
		m_activity = activity;
		m_mapView = mapView;
		mInflater = LayoutInflater.from(m_activity);
		tipView = mInflater.inflate(R.layout.navistudio_tip_layout, null);
		tvTipRoadNam = (TextView) tipView.findViewById(R.id.tv_tip_road_name);
		tvTipRoadNam.setTextColor(Color.WHITE);
		searchButton = (ImageButton)tipView.findViewById(R.id.ib_search_around);
		searchButton.setOnClickListener(this);
		endPointButton = (ImageButton)tipView.findViewById(R.id.ib_set_end_point);
		endPointButton.setOnClickListener(this);
	}

	/**
	 * 短按监听方法
	 */
	@Override
	public void onClickTouch(int x, int y){
		Log.e(TAG, "NaviMapTouchListener onClickTouch");
		if (m_mapView.isHasTip()) {
			m_mapView.hideTip();
		}
	}

	/**
	 * 长按监听方法
	 */
	@Override
	public void onLongTouch(int x, int y){
		Message msg = Message.obtain();
		msg.what = H_SHOW_TIP;
		msg.arg1 = x;
		msg.arg2 = y;
		h.sendMessage(msg);
	}


	@Override
	public void onClick(View arg0){
		if(arg0.equals(this.searchButton)){
			ToolsUtils.intentEvent(null,0,Constants.SEARCHAROUND_CENTER_POINT, lonlat, m_activity, Constants.ACTIVITY_SEARCHAROUND);
		}else if(arg0.equals(this.endPointButton)){
			m_activity.naviControl.calculatePath(lonlat,0);
		}
		m_mapView.hideTip();
	}
}
