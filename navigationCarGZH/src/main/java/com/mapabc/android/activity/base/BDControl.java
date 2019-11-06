/**
 *
 */
package com.mapabc.android.activity.base;


import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.mapabc.android.activity.NaviStudioActivity;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.listener.DisPatchInfo;
import com.mapabc.android.activity.listener.ReceiveInfo;
import com.mapabc.naviapi.MapView;
import com.mapabc.naviapi.route.GPSRouteInfo;
import com.mapabc.naviapi.route.GpsInfo;
import com.mapabc.naviapi.route.SatInfo;

/**
 * desciption:北斗的控制器
 */
public class BDControl implements OnClickListener, ReceiveInfo {

    private NaviStudioActivity naviStudioActivity;
    public ImageView bdstate;
    public MapView mapView;
    private static final int UPDATEBDSTATUS = 106;// 更新北斗信息
    private static final int NOBD = 109; //刷新

    private String TAG = "BDControl";


    private Handler handler = new Handler() {


        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATEBDSTATUS:
                    Log.d(TAG, "fuck!!!");
                    GpsInfo pstGPSInfo = (GpsInfo) msg.obj;
                    int s_count = 0;
                    for (int i = 0; i < pstGPSInfo.useableSatNum; i++) {
                        SatInfo csatinfo = pstGPSInfo.satInfo[i];
                        if (csatinfo == null || csatinfo.angle <= 0 || csatinfo.satNo <= 0) {
                            continue;
                        }
                        if (csatinfo.satNo >= 160) {
                            s_count++;
                        }
                    }
                    if (pstGPSInfo != null) {
                        s_count = s_count / 2;
                    }
                    if (NaviControl.mgpsStatus == 3) {
                        switch (s_count) {
                            case 0:
                                //bdstate.setImageResource(R.drawable.navistudio_bd_0_);
                                break;
                            case 1:
                                bdstate.setImageResource(R.drawable.navistudio_bd_a_1);
                                break;
                            case 2:
                                bdstate.setImageResource(R.drawable.navistudio_bd_a_2);
                                break;
                            case 3:
                                bdstate.setImageResource(R.drawable.navistudio_bd_a_3);
                                break;
                            case 4:
                                bdstate.setImageResource(R.drawable.navistudio_bd_a_4);
                                break;
                            default:
                                bdstate.setImageResource(R.drawable.navistudio_bd_a_4);
                                break;
                        }
                    } else {
                        bdstate.setImageResource(R.drawable.navistudio_bd_0_);
                    }
                    break;
                case NOBD:
                    bdstate.setImageResource(R.drawable.navistudio_gps_0_x);
                    Log.d(TAG, "NOBD??????????????????????????????");
                    break;
                default:
                    break;
            }


        }

        ;
    };

    @Override
    public void DoGpsInfo(GpsInfo gpsInfo) {
        Message msg = Message.obtain();
        msg.what = UPDATEBDSTATUS;
        msg.obj = gpsInfo;
        handler.sendMessage(msg);
    }

    public BDControl(NaviStudioActivity activity, MapView mapView) {
        this.naviStudioActivity = activity;
        bdstate = this.naviStudioActivity.bdstate;
        this.mapView = mapView;
        DisPatchInfo.getInstance().addGpsInfoListener("BDControl", this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(bdstate)) {
            bdInfo();
        }
    }

    private void bdInfo() {
        Intent in = new Intent(Constants.ACTIVITY_BDINFO);
        this.naviStudioActivity.startActivity(in);
    }

    @Override
    public void DoRouteInfo(GPSRouteInfo routeInfo, boolean gpsNavi) {

    }

    //在gps断开时刷新gps状态
    public void refreshStatus() {
        if (NaviControl.mgpsStatus != 3) {
            Message msg = Message.obtain();
            msg.what = NOBD;
            handler.sendMessage(msg);
        }
    }
}

