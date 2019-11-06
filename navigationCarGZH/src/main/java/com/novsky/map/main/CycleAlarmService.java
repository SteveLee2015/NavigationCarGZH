package com.novsky.map.main;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.mapabc.android.activity.base.NaviStudioApplication;
import com.mapabc.android.activity.listener.CustomBDRNSSLocationListener;
import com.mapabc.android.activity.utils.FMSharedPreferenceUtils;
import com.novsky.map.util.SCConstants;
import com.novsky.map.util.Utils;

/**
 * 循环报警服务
 *
 * @author steve
 */
public class CycleAlarmService extends Service {

    private final static String TAG = "CycleAlarmService";

    private String address = "";
    private BDCommManager bdManager = null;
    private static WakeLock wakeLock = null;
    private FMSharedPreferenceUtils prefUtils = null;

    private static final int DELAY_SEND_CMD = 0x1000001;
    private BDRNSSLocation bdLocation;

    private int mSendNum = 0;

    private BDRNSSLocationListener mBDRNSSLocationListener = new CustomBDRNSSLocationListener() {

        @Override
        public void onLocationChanged(BDRNSSLocation bdrnssLocation) {
            super.onLocationChanged(bdrnssLocation);
            bdLocation = bdrnssLocation;
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

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DELAY_SEND_CMD:
                    if (mSendNum < SCConstants.ALARM_MAX_NUM) {
                        mSendNum ++;
                        sendAlarmCmd();
                        mHandler.sendEmptyMessageDelayed(DELAY_SEND_CMD , SCConstants.BD_CARD_FREQ_MILLIS);
                    } else {
                        SCConstants.mLaunchSequence = SCConstants.UPDATE_MULTI_POSITION;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mSendNum = 0;
        prefUtils = FMSharedPreferenceUtils.getInstance();
        bdManager = BDCommManager.getInstance(NaviStudioApplication.getContext());
        try {
            bdManager.addBDEventListener(mBDRNSSLocationListener);
        } catch (BDParameterException e) {
            e.printStackTrace();
        } catch (BDUnknownException e) {
            e.printStackTrace();
        }
        mHandler.sendEmptyMessageDelayed(DELAY_SEND_CMD , 1000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CycleLocationReportService");
        if (null != wakeLock) {
            wakeLock.acquire();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public ComponentName startService(Intent service) {
        return super.startService(service);
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        if (bdManager != null) {
            try {
                bdManager.removeBDEventListener(mBDRNSSLocationListener);
            } catch (BDParameterException e) {
                e.printStackTrace();
            } catch (BDUnknownException e) {
                e.printStackTrace();
            }
        }
        if(mHandler != null) {
           mHandler.removeMessages(DELAY_SEND_CMD);
        }
    }

    private void sendAlarmCmd() {
        address = prefUtils.getString("address", "");
        if (address == null || "".equals(address)) {
            address = "145432";
        }
        int type = prefUtils.getInt("type", 0);
        String message = prefUtils.getString("message", "");
        String msg = Utils.getPuShiCRCAlarm2(bdLocation, message,(byte) type);
        Log.i(TAG, "msg=" + msg + ",address=" + address);
        try {
            bdManager.sendSMSCmdBDV21(address, 1, 2, "N", msg);
        } catch (BDUnknownException e) {
            e.printStackTrace();
        } catch (BDParameterException e) {
            e.printStackTrace();
        }
    }
}
