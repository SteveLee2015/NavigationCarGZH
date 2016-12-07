package com.mapabc.android.activity.route.track;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.mapabc.android.activity.base.AutoNaviMap;
import com.mapabc.android.activity.base.Constants;
import com.mapabc.naviapi.MapAPI;
import com.mapabc.naviapi.listener.TraceListener;
import com.mapabc.naviapi.type.Const;
import com.mapabc.naviapi.type.NSLonLat;
import com.mapabc.naviapi.type.Overlay;

public class MyTraceListener implements TraceListener{
	private static final String TAG="MyTraceListener";
	NSLonLat vehicleLonLat=null;
	private Context context;
	public static int TRACE_PLAY_MODE=-1;
	private long count=1420;
//	private boolean showTrace=false;
	private String tracedata;
	
	public MyTraceListener(Context context){
		this.context=context;
	}
	public Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1000:
				AutoNaviMap.getInstance(context).getMapView().invalidate();
				break;

			default:
				break;
			}
		}
		
	};
	@Override
	public void onTracePlayMode(int status) {
		switch (status) {
		case Const.TRACE_PLAY_START:
			TRACE_PLAY_MODE=Const.TRACE_PLAY_START;
			
			break;
		case Const.TRACE_PLAY_STOP:
			TRACE_PLAY_MODE=Const.TRACE_PLAY_STOP;
			
			break;
		case Const.TRACE_PLAY_PAUSE:
			TRACE_PLAY_MODE=Const.TRACE_PLAY_PAUSE;
			
			break;

		default:
			break;
		}
	}

	@Override
	public void onTracePlayPoint(float longitude, float latitude,
			int direction, String time, int percent) {
//		Log.e(TAG, "======longitude======="+longitude);
//		Log.e(TAG, "======latitude======="+latitude);

		vehicleLonLat=new NSLonLat(longitude, latitude);
		if (MapAPI.getInstance().getMapView() == Const.MAP_VIEWSTATE_NORTH) {
			if (MapAPI.getInstance().isCarInCenter()) {
				MapAPI.getInstance().setVehiclePosInfo(vehicleLonLat,
						direction);
				MapAPI.getInstance().setMapCenter(vehicleLonLat);
			} else {
				MapAPI.getInstance().setVehiclePosInfo(vehicleLonLat,
						direction);
				MapAPI.getInstance().setMapCenter(vehicleLonLat);
			}
		} else {
			MapAPI.getInstance().setMapAngle(360 - direction);
			if (MapAPI.getInstance().isCarInCenter()) {
				MapAPI.getInstance().setVehiclePosInfo(vehicleLonLat,
						0);
				MapAPI.getInstance().setMapCenter(vehicleLonLat);
			} else {
				MapAPI.getInstance().setMapCenter(vehicleLonLat);
				MapAPI.getInstance().setVehiclePosInfo(vehicleLonLat,
						0);
			}
		}
		
//		MapAPI.getInstance().setVehiclePosInfo(vehicleLonLat,
//				direction);
		Log.e(TAG, "==play:direction======="+direction);
//		MapAPI.getInstance().setMapCenter(vehicleLonLat);
		mHandler.sendEmptyMessage(1000);
	}

	@Override
	public void onTraceSavePoint(float longitude, float latitude,
			int direction, String time) {
		
		int res= SettingForTrackTools.getTrackSetting(context);
		if (res==0) {//在地图上显示轨迹点
			tracedata ="\r\n"+"时间:"+time
					+"\r\n"+"经度:"+longitude
					+"\r\n"+"纬度:"+latitude;
			count++;
			Log.e(TAG, "==save:direction======="+direction);
			Overlay overlay = new Overlay();
			overlay.id = count;
			overlay.type = Const.MAP_OVERLAY_POI;
			overlay.hide = false;
			overlay.lons = new float[]{longitude};
			overlay.lats = new float[]{latitude};
			overlay.labelText = "";
			overlay.painterName = "";
			overlay.labelName = "trace_point";		
			MapAPI.getInstance().addOverlay(Constants.TRACE_POINT, overlay);
			savedata();
		}
		
	}
	//GP 存储轨迹数据
	private  void savedata(){
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			String filename = "TraceDatacollect.txt";
			File sd = Environment.getExternalStorageDirectory() ;
			String path = sd.getPath() + "/TraceDatacollect"+ ".txt";
			File file = new File(path);
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
//				String aa ="时间："+mTime.getText().toString()+ "经度："+mLongitude.getText().toString() +" 纬度："+ mLatitude.getText().toString();
			String aa = tracedata;
			if(!aa.equals("")){
					String bb = aa+ "\r\n";
					File f = new File(Environment.getExternalStorageDirectory(), filename);
					FileOutputStream out = null;
					try {
						out = new FileOutputStream(f, true);
						out.write(bb.getBytes("UTF-8"));
						Toast.makeText(context,"定距采集数据成功！", Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						try {
							out.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}else{
					Toast.makeText(context,"没有可以存储的内容", Toast.LENGTH_SHORT).show();
				}	
		} else {
			Toast.makeText(context,
					"没有SD卡，请插入SD卡尝试！！！", Toast.LENGTH_SHORT).show();
		}
	}
}
