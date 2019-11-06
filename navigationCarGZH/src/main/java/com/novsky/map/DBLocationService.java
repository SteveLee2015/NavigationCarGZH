package com.novsky.map;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.BDRDSSManager;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.mapabc.android.activity.R;
import com.novsky.map.main.CycleLocationService;
import com.novsky.map.util.LocSetDatabaseOperation;
import com.novsky.map.util.LocationSet;
import com.novsky.map.util.Utils;

public class DBLocationService extends IntentService {
    LocSetDatabaseOperation operation;
    BDCommManager  manager;
    public static Context mContext;
    /**
     * 定位设置
     */
    private LocationSet set = null;

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);

    }

    public DBLocationService() {
        super("DBLocationService");
        operation=new LocSetDatabaseOperation(mContext);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        boolean isStart = Utils.isServiceRunning(getApplicationContext(), "com.novsky.map.main.CycleLocationService");
        if (!isStart) {
            /* 判断是否安装北斗卡 */
            if (!Utils.checkBDSimCard(getApplicationContext())) return;
            /* 定位设置查询 */

//            frequency = Integer.valueOf(set.getLocationFeq());
//            /*检查是否是循环定位*/
//            if (frequency > 0) {
//
//                Utils.COUNT_DOWN_TIME = frequency;
//                Utils.isCycleLocation = true;
//                Intent mIntent = new Intent();
//                mIntent.setClass(getActivity(), CycleLocationService.class);
//                getActivity().startService(mIntent);
//            } else {
//                Utils.COUNT_DOWN_TIME = cardManager.getCardInfo().mSericeFeq;
//                if (myCount != null) {
//                    myCount.start();
//                }
//                startLocationBtn.setClickable(false);
//            }
            try {
                manager.sendLocationInfoReqCmdBDV21(BDRDSSManager.ImmediateLocState.LOC_NORMAL_FLAG, Integer.valueOf(set.getHeightType()), "L", Double.valueOf(set.getHeightValue()).doubleValue(),
                        Double.valueOf(set.getTianxianValue()).doubleValue(), 0);
                Utils.isProgressDialogShowing = true;
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (Utils.isProgressDialogShowing) {
//                            progressDialog.dismiss();
//                            Toast.makeText(getActivity().getApplicationContext(), "未接收到定位信息!", Toast.LENGTH_LONG).show();
//                            Utils.isProgressDialogShowing = false;
//                        }
//                    }
//                }, 8000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        manager = BDCommManager.getInstance(mContext);
        set = operation.getFirst();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(operation != null)
        operation.close();
    }
}
