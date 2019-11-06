package com.novsky.map.main;

import android.app.Service;
import android.content.Intent;
import android.location.BDLocationReport;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.mapabc.android.activity.listener.CustomBDRNSSLocationListener;
import com.mapabc.android.activity.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by llg on 2016/12/10.
 */

public class BaseReportService extends Service{


    public static final int NO_LOCATION = 100;

    public BDRNSSLocation rnsslocation = null;

    private BDCommManager mananger;

    public  Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case NO_LOCATION:{

                    Toast.makeText(BaseReportService.this, "对不起,当前没有位置信息!", Toast.LENGTH_SHORT).show();
                    break;
                }
            }


        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mananger = BDCommManager.getInstance(this);
        try {
            mananger.addBDEventListener(mBDRNSSLocationListener);
        } catch (BDParameterException e) {
            e.printStackTrace();
        } catch (BDUnknownException e) {
            e.printStackTrace();
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mananger.removeBDEventListener(mBDRNSSLocationListener);
        } catch (BDUnknownException e) {
            e.printStackTrace();
        } catch (BDParameterException e) {
            e.printStackTrace();
        }
    }

    private BDRNSSLocationListener mBDRNSSLocationListener=new CustomBDRNSSLocationListener(){
        @Override
        public void onLocationChanged(BDRNSSLocation arg0) {
            super.onLocationChanged(arg0);
            rnsslocation = arg0;
        }

        @Override
        public void onProviderDisabled(String arg0) {

        }

        @Override
        public void onProviderEnabled(String arg0) {

        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

        }
    };



    /**
     * 把GPS定位的值封装到BDLocationReport实体类中
     */
    public BDLocationReport addGPSLocationToBDLocationReport(int reportFreq, String userAddress) {

        if(rnsslocation!=null){
            BDLocationReport report = new BDLocationReport();
            report.setHeightUnit("M");
            report.setLongitude(rnsslocation.getLongitude());
            report.setLongitudeDir(rnsslocation.getExtras().getString("londir"));
            report.setLatitudeDir(rnsslocation.getExtras().getString("latdir"));
            report.setHeight(rnsslocation.getAltitude());
            report.setLatitude(rnsslocation.getLatitude());
            report.setMsgType(1);
            //report.setReportFeq(Integer.valueOf(frequency));
            report.setReportFeq(0);
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss)");
//				String time = sdf.format(new Date());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String date = sdf.format(new Date());
            long time = rnsslocation.getTime();
            String rnDateTimeStr = DateUtils.getRNDateTimeStr(time);
            report.setReportTime(rnDateTimeStr);
            //report.setUserAddress(mUserAddress);
            report.setUserAddress(userAddress);
            return report;
        }else{
            return null;
        }
    }

}
