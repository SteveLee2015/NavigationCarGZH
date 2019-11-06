/**
 *
 */
package com.mapabc.android.activity.base;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.BDEventListener;
import android.location.BDLocationReport;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.os.Handler;
import android.util.Log;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDLocationZDAListener;
import com.bd.comm.protocal.SerialApplication;
import com.bd.comm.protocal.ZDATime;
import com.mapabc.android.activity.utils.ReceiverAction;
import com.mapabc.naviapi.type.Const;
import com.novsky.map.DBLocationService;
import com.novsky.map.hook.HookManager;
import com.novsky.map.main.ReportPosManager;
import com.novsky.map.main.SMSReceiver;
import com.novsky.map.service.InstallLogService;
import com.novsky.map.util.Config;
import com.novsky.map.util.FriendsLocation;
import com.novsky.map.util.FriendsLocationDatabaseOperation;
import com.novsky.map.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NaviStudioApplication extends SerialApplication {

    public boolean isRecordTrack = true;
    public boolean isTracePlay = false;
    public int TRACE_PLAY_MODE = Const.TRACE_PLAY_STOP;
    private Context mContext = this;
    public static final String TAG = "NaviStudioApplication";
    private static NaviStudioApplication instance;

    //标记星图 已定位  未定位
    public boolean isLocationed = false;

    private BDCommManager mananger;
    private int FLAG;

    BDLocationZDAListener zdaListener = new BDLocationZDAListener() {
        @Override
        public void onZDATime(ZDATime mZDATime) {
            //Log.d(TAG,mZDATime.toString());

        }
    };


    // 位置报告1  位置报告2
    BDEventListener.BDLocReportListener locReportListener = new BDEventListener.BDLocReportListener() {
        @Override
        public void onLocReport(BDLocationReport bdLocationReport) {

            Log.d(TAG, bdLocationReport.toString());
            boolean result = mAddLocationReportToDatabase(bdLocationReport, Config.RN_RD_WAA);
            if (result) {
                //notification 通知
                /* 2.采用NotificationManager来显示 */
                Utils.mShowLocationReportNotification(mContext, bdLocationReport);
                if (Utils.checkNaviMap) {
                    ReportPosManager.receiverReportPos(bdLocationReport);
                }
                /*3 广播通知更新数据***/
                Intent intent1 = new Intent();
                intent1.setAction(ReceiverAction.ACTION_RD_REPORT);
                mContext.sendBroadcast(intent1);
            }
            //Toast.makeText(NaviStudioApplication.this, bdLocationReport.toString(), Toast.LENGTH_SHORT).show();
        }
    };


    public static NaviStudioApplication getContext() {

        return instance == null ? instance = new NaviStudioApplication() : instance;
    }

    public NaviStudioApplication() {
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        //注册位置报告 监听器 接收到数据后保存到数据库
        //同时 发送广播通知 友邻信息界面 更新数据
        HookManager.hookLocationManager();
        DBLocationService.mContext = this;
        mananger = BDCommManager.getInstance(this);
        try {
            mananger.addBDEventListener(locReportListener, zdaListener);
        } catch (BDParameterException e) {
            e.printStackTrace();
        } catch (BDUnknownException e) {
            e.printStackTrace();
        }
        SharedPreferences share = mContext.getSharedPreferences(Config.PREFERENCE_NAME, Config.MODE);
        FLAG = share.getInt(Config.LOCATION_MODEL, 0);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mananger.openZDA_UM220();
                Log.d(TAG, "open ZDA");
                mananger.setLocationStrategy(FLAG);
            }
        }, 1000);


        //new出上边定义好的BroadcastReceiver
        SMSReceiver smsBroadCastReceiver = new SMSReceiver();
        //实例化过滤器并设置要过滤的广播
        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        //注册广播
        mContext.registerReceiver(smsBroadCastReceiver, intentFilter);

        Intent mIntent = new Intent(this, InstallLogService.class);
        startService(mIntent);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "onTerminate");
        try {
            mananger.removeBDEventListener(locReportListener, zdaListener);
        } catch (BDUnknownException e) {
            e.printStackTrace();
        } catch (BDParameterException e) {
            e.printStackTrace();
        }
        Intent mIntent = new Intent(this, InstallLogService.class);
        stopService(mIntent);
    }


    /**
     * 保存友邻位置到数据库
     *
     * @param report
     * @returnal
     */
    private boolean mAddLocationReportToDatabase(BDLocationReport report, int flag) {


        FriendsLocationDatabaseOperation oper = new FriendsLocationDatabaseOperation(mContext);

        //double latitude = report.getLatitude()/100;
        //double longitude = report.getLongitude()/100;

        double latitude = Utils.changeLonLatMinuteToDegree(Double.valueOf(report.mLatitude));
        double longitude = Utils.changeLonLatMinuteToDegree(Double.valueOf(report.mLongitude));

        String latiFormat = String.format("%.6f", latitude);
        String longiFormat = String.format("%.6f", longitude);

        latitude = Double.parseDouble(latiFormat);
        longitude = Double.parseDouble(longiFormat);

        report.setLatitude(latitude);
        report.setLongitude(longitude);

        //ffffff.ff
        String locationTime = report.getReportTime();

        //本地时间
        String time = "00:00:00.00";
        if (locationTime.length() >= 6) {
            String hh = locationTime.substring(0, 2);
            int anInt = Integer.parseInt(hh);
            int beijingTime = (anInt + 8) % 24;
            String mm = locationTime.substring(2, 4);
            String ss = locationTime.substring(4, locationTime.length());
            time = beijingTime + ":" + mm + ":" + ss;
        }
        //添加日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        report.setReportTime(sdf.format(new Date()) + " " + time);

        FriendsLocation fl = new FriendsLocation();
        fl.setUserId(report.mUserAddress);


        fl.setLat(String.valueOf(report.mLatitude));
        fl.setLon(String.valueOf(report.mLongitude));

        fl.setHeight(String.valueOf(report.mHeight));
        fl.setReportTime(report.mReportTime);
        boolean isTrue = oper.insert(fl);
        oper.close();
        return isTrue;
    }


}
