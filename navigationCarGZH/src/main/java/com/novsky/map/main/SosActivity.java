package com.novsky.map.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.BDEventListener;
import android.location.BDLocation;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.listener.CustomBDRNSSLocationListener;
import com.mapabc.android.activity.utils.FMSharedPreferenceUtils;
import com.novsky.map.util.BDContactColumn;
import com.novsky.map.util.BDLocationManager;
import com.novsky.map.util.SCConstants;
import com.novsky.map.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SosActivity extends Activity implements View.OnClickListener {
    /**
     * 通讯录标识
     */
    private final int REQUEST_CONTACT = 1;
    /**
     * 页面标题
     */
    protected TextView title_name;
    private int COOD_FLAG = 0;
    /**
     * 返回键
     */
    protected ImageView back;
    private EditText ed_str, bdloc_userAddress_et;
    private Button cancleAlarmBtn, sendAlarmBtn;
    private TextView tv_type1, tv_type2, tv_type3, tv_type4, tv_type5, tv_type6, tv_str1, tv_str2;

    private BDCommManager mananger = null;
    public Context mContext = this;
    public static SosActivity sosActivity;
    private byte flag = 0;
    private String flagstr = "";

    private BDAlarmReceiver receiver = null;
    private final static int CMD_LOCATION_SUCCESS = 0x10001;
    private final static int CMD_LOCATION_FAIL = 0x10002;
    private final static int CMD_SYSTEM_LAUNCHER = 0x10003;
    private final static int CMD_NO_POWER = 0x10004;
    private final static int CMD_SILENCE = 0x10005;
    private final static int CMD_TIME = 0x10006;

    LocationUI locationUI = new LocationUI();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LocationUI location = (LocationUI) msg.obj;
            Utils.LOCATION_REPORT_LON = location.lon;
            Utils.LOCATION_REPORT_LAT = location.lat;
        }
    };
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 指令发送成功
                case CMD_LOCATION_SUCCESS: {
                    String mark = (String) msg.obj;
                    if (mark != null && "DWA".equals(mark)) {
                        Toast.makeText(
                                mContext,
                                mContext.getResources()
                                        .getString(R.string.cmd_success)
                                        .replace("CMD", "定位命令"), Toast.LENGTH_SHORT)
                                .show();
                    } else if (mark != null && "WAA".equals(mark)) {
                        Toast.makeText(
                                mContext,
                                mContext.getResources()
                                        .getString(R.string.cmd_success)
                                        .replace("CMD", "位置报告1"),
                                Toast.LENGTH_SHORT).show();
                    } else if (mark != null && "WBA".equals(mark)) {
                        Toast.makeText(
                                mContext,
                                mContext.getResources()
                                        .getString(R.string.cmd_success)
                                        .replace("CMD", "位置报告2"),
                                Toast.LENGTH_SHORT).show();
                    } else if (mark != null && "TXA".equals(mark)) {
//                        Toast.makeText(
//                                mContext,
//                                mContext.getResources()
//                                        .getString(R.string.cmd_success)
//                                        .replace("CMD", "报警"), Toast.LENGTH_SHORT)
//                                .show();
                    }
                    Utils.CARD_FREQ = Utils.getCardInfo().mSericeFeq;
                    break;
                }
                // 指令发送失败
                case CMD_LOCATION_FAIL:
                    String mark = (String) msg.obj;
                    if (mark != null && "DWA".equals(mark)) {
                        Toast.makeText(
                                mContext,
                                mContext.getResources()
                                        .getString(R.string.cmd_fail)
                                        .replace("CMD", "定位命令"), Toast.LENGTH_SHORT)
                                .show();
                    }
                    if (mark != null && "WAA".equals(mark)) {
                        Toast.makeText(
                                mContext,
                                mContext.getResources()
                                        .getString(R.string.cmd_fail)
                                        .replace("CMD", "位置报告"), Toast.LENGTH_SHORT)
                                .show();
                    }
                    if (mark != null && "WBA".equals(mark)) {
                        Toast.makeText(
                                mContext,
                                mContext.getResources()
                                        .getString(R.string.cmd_fail)
                                        .replace("CMD", "位置报告"), Toast.LENGTH_SHORT)
                                .show();
                    }
                    if (mark != null && "TXA".equals(mark)) {
//                        Toast.makeText(
//                                mContext,
//                                mContext.getResources()
//                                        .getString(R.string.cmd_fail)
//                                        .replace("CMD", "报警"), Toast.LENGTH_SHORT)
//                                .show();
                    }
                    Utils.CARD_FREQ = Utils.getCardInfo().mSericeFeq;
                    break;
                // 系统的抑制命令，发射被抑制
                case CMD_SYSTEM_LAUNCHER:
                    Toast.makeText(
                            mContext,
                            mContext.getResources().getString(
                                    R.string.system_un_launch), Toast.LENGTH_SHORT)
                            .show();
                    break;
                // 电量不足,发射被抑制
                case CMD_NO_POWER:
                    Toast.makeText(mContext,
                            mContext.getResources().getString(R.string.no_power),
                            Toast.LENGTH_SHORT).show();

                    break;
                // 无线电静默，发射被抑制
                case CMD_SILENCE:
                    Toast.makeText(
                            mContext,
                            mContext.getResources().getString(
                                    R.string.wireless_silence), Toast.LENGTH_SHORT)
                            .show();
                    break;
                // 超频
                case CMD_TIME:
                    Toast.makeText(
                            mContext,
                            mContext.getResources()
                                    .getString(R.string.service_feq_no)
                                    .replace("TIME", msg.arg1 + ""),
                            Toast.LENGTH_SHORT).show();
                    Utils.CARD_FREQ = (Utils.getCardInfo().mSericeFeq + msg.arg1);
                    break;
                default:
                    break;
            }
        }
    };


    private BDEventListener fkilistener = new BDEventListener.BDFKIListener() {
        @Override
        public void onTime(int time) {
            Message msg = new Message();
            msg.what = CMD_TIME;
            msg.arg1 = time;
            handler.sendMessage(msg);
        }

        @Override
        public void onSystemLauncher() {
            handler.sendEmptyMessage(CMD_SYSTEM_LAUNCHER);
        }

        @Override
        public void onSilence() {
            handler.sendEmptyMessage(CMD_SILENCE);
        }

        @Override
        public void onPower() {
            handler.sendEmptyMessage(CMD_NO_POWER);
        }

        @Override
        public void onCmd(String arg0, boolean istrue) {
            if (arg0 != null
                    && ("DWA".equals(arg0) || "WAA".equals(arg0)
                    || "WBA".equals(arg0) || "TXA".equals(arg0))) {
                Message msg = new Message();
                msg.obj = arg0;
                if (istrue) {
                    msg.what = CMD_LOCATION_SUCCESS;
                    handler.sendMessage(msg);
                } else {
                    msg.what = CMD_LOCATION_FAIL;
                    handler.sendMessage(msg);
                }
            }
        }
    };
    private BDRNSSLocationListener mBDRNSSLocationListener = new CustomBDRNSSLocationListener() {

        @Override
        public void onLocationChanged(BDRNSSLocation arg0) {
            super.onLocationChanged(arg0);
            if (locationUI != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                locationUI.time = sdf.format(new Date(arg0.getTime() + 8 * 60 * 60 * 1000));
                locationUI.lon = arg0.getLongitude();

                locationUI.lat = arg0.getLatitude();
                locationUI.height = (arg0 != null && arg0.getAltitude() != 0.0) ? String
                        .valueOf(arg0.getAltitude()) : getResources()
                        .getString(R.string.common_dadi_heigh_value);
            }

            Message message = mHandler.obtainMessage();
            message.obj = locationUI;
            mHandler.sendMessage(message);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.type1:
                flag = (byte) 0x01;
                flagstr = tv_type1.getText().toString().trim();
                ed_str.setText(tv_type1.getText());
                break;
            case R.id.type2:
                flag = (byte) 0x02;
                flagstr = tv_type2.getText().toString().trim();
                ed_str.setText(tv_type2.getText());
                break;
            case R.id.type3:
                flag = (byte) 0x03;
                flagstr = tv_type3.getText().toString().trim();
                ed_str.setText(tv_type3.getText());
                break;
            case R.id.type4:
                flag = (byte) 0x04;
                flagstr = tv_type4.getText().toString().trim();
                ed_str.setText(tv_type4.getText());
                break;
            case R.id.type5:
                flag = (byte) 0x05;
                flagstr = tv_type5.getText().toString().trim();
                ed_str.setText(tv_type5.getText());
                break;
            case R.id.type6:
                flag = (byte) 0x06;
                flagstr = tv_type6.getText().toString().trim();
                ed_str.setText(tv_type6.getText());
                break;
        }
    }

    public class LocationUI {
        String time;
        double lat;
        double lon;
        String height;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);
        sosActivity = this;
        TextView homeTitleBD1Num = (TextView) this.findViewById(R.id.home_title_bd1_num);
        TextView homeTitleBD2Num = (TextView) this.findViewById(R.id.home_title_bd2_num);

        ed_str = (EditText) this.findViewById(R.id.tv_str);
        tv_str1 = (TextView) this.findViewById(R.id.tv_str1);
        tv_str2 = (TextView) this.findViewById(R.id.tv_str2);
        tv_type1 = (TextView) this.findViewById(R.id.type1);
        tv_type2 = (TextView) this.findViewById(R.id.type2);
        tv_type3 = (TextView) this.findViewById(R.id.type3);
        tv_type4 = (TextView) this.findViewById(R.id.type4);
        tv_type5 = (TextView) this.findViewById(R.id.type5);
        tv_type6 = (TextView) this.findViewById(R.id.type6);
        tv_type1.setOnClickListener(this);
        tv_type2.setOnClickListener(this);
        tv_type3.setOnClickListener(this);
        tv_type4.setOnClickListener(this);
        tv_type5.setOnClickListener(this);
        tv_type6.setOnClickListener(this);
        bdloc_userAddress_et = (EditText) this.findViewById(R.id.bdloc_userAddress_et);
        bdloc_userAddress_et.setText(FMSharedPreferenceUtils.getInstance().getString("MY_ADDRESS_SOS", ""));
        title_name = (TextView) this.findViewById(R.id.title_name);
        back = (ImageView) this.findViewById(R.id.home_title_flag_img);
        sendAlarmBtn = (Button) findViewById(R.id.send_alarm_btn);
        sendAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.getCardInfo().mSericeFeq == 9999) {
                    Toast.makeText(mContext,
                            mContext.getResources().getString(R.string.have_not_bd_sim),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Utils.LOCATION_REPORT_LON == 0.0 || Utils.LOCATION_REPORT_LAT == 0.0) {
                    Toast.makeText(mContext, "获取当前位置信息失败,请重新发送!", Toast.LENGTH_SHORT).show();
                    return;
                }

                //启动循环
                if (flag == 0) {
                    Toast.makeText(mContext, "选择报警类型", Toast.LENGTH_SHORT).show();
                    return;
                }
                String address = bdloc_userAddress_et.getText().toString().trim();
                String message = tv_str1.getText().toString().trim() + flagstr + tv_str2.getText().toString().trim();
                FMSharedPreferenceUtils.getInstance().putString("address", address);
                FMSharedPreferenceUtils.getInstance().putInt("type", flag);
                FMSharedPreferenceUtils.getInstance().putString("message", message);
//                try {
//                    mananger.sendSOSSettingsCmd(address, 2, Utils.getSOSSettings(message));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                startCycleAlarmService();
                Toast.makeText(mContext, "开始循环报警!", Toast.LENGTH_LONG).show();
            }
        });
        cancleAlarmBtn = (Button) findViewById(R.id.cancle_alarm_btn);
        cancleAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        title_name.setText("SOS");
        //返回键
        //mReturnLayout=(ImageView)this.findViewById(R.id.home_title_flag_img);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        View view = this.findViewById(R.id.bdloc_linker);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(SosActivity.this, BDContactActivity.class);
                intent.setData(BDContactColumn.CONTENT_URI);
                SosActivity.this.startActivityForResult(intent, REQUEST_CONTACT);
            }
        });
        	/*RDSS中间件初始化*/
        mananger = BDCommManager.getInstance(this);
        try {
            mananger.addBDEventListener(fkilistener);
        } catch (BDParameterException e1) {
            e1.printStackTrace();
        } catch (BDUnknownException e1) {
            e1.printStackTrace();
        }
//        alarmPrefs = getSharedPreferences("BD_ALARM_CFG", 0);
//        boolean isStart=alarmPrefs.getBoolean("CHECK_START", false);
//        if(isStart){
        int index = 5;
        //alarmItem.setSelection(index);
        //alarmItem.setEnabled(false);
        // sendAlarmBtn.setText(mContext.getResources().getString(R.string.stop_alarm));
//        }
        registerBroadcastReceiver();
        if ("S500".equals(Utils.DEVICE_MODEL)) {
            BDLocationManager.getInstance(this).requestLocationUpdate(locationListener);
        } else {
            try {
                mananger.addBDEventListener(mBDRNSSLocationListener);
            } catch (BDParameterException e) {
                e.printStackTrace();
            } catch (BDUnknownException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CONTACT) {
            if (resultCode == RESULT_OK) {
                if (data == null) {
                    return;
                }
                Uri result = data.getData();
                Cursor cursor = mContext.getContentResolver().query(result, BDContactColumn.COLUMNS, null, null, null);
                String mUserAddress = "";
                if (cursor.moveToFirst()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.USER_NAME));
                    String num = cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.CARD_NUM));
                    mUserAddress = name + "(" + num + ")";
                }
                cursor.close();
                bdloc_userAddress_et.setText(mUserAddress);
            }
        }

    }
    
    private void startCycleAlarmService() {
        if (SCConstants.mLaunchSequence == SCConstants.DO_NOTHIING || SCConstants.mLaunchSequence == SCConstants.ALARM_STATUS) {
            Toast.makeText(mContext , R.string.now_sow_cmd, Toast.LENGTH_SHORT).show();
        } else {
            Intent alarmServiceIntent = new Intent(this, CycleAlarmService.class);
            mContext.startService(alarmServiceIntent);
            SCConstants.mLaunchSequence = SCConstants.ALARM_STATUS;
        }
    }

    private class BDAlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Utils.ALARM_CHANGED_ACTION.equals(action)) {
                Bundle bundle = intent.getExtras();
                boolean stopFlag = bundle.getBoolean("CYCLE_ALARM_FLAG");
                if (!stopFlag) {
                    sendAlarmBtn.setText(mContext.getResources().getString(R.string.start_alarm));
                    //alarmItem.setEnabled(true);
                }
            }
        }
    }

    /**
     * 定位监听
     */
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            Utils.checkBDLocationPort(SosActivity.this, false,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, 0); // 此为设置完成后返回到获取界面
                        }
                    }, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //Intent countTimeIntent = new Intent(getActivity(), CountDownTimeService.class);
                            //getActivity().stopService(countTimeIntent);
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            startActivity(intent);
                        }
                    });
        }

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                BDLocation bdloc = new BDLocation();
                bdloc.setLongitude(location.getLongitude());
                bdloc.setLatitude(location.getLatitude());
                bdloc.setEarthHeight(location.getAltitude());
                BDLocation bdlocation = Utils.translate(bdloc, COOD_FLAG);
                mSwitchCoodriate(bdlocation);
            }
        }
    };

    public void mSwitchCoodriate(BDLocation bdlocation) {
        // 设置经度
        String Longitude = (bdlocation != null && bdlocation.mLongitude != 0.0) ? Utils.setDoubleNumberDecimalDigit(bdlocation.mLongitude, 10) : SosActivity.this
                .getResources().getString(R.string.common_lon_value);
        // 设置纬度
        String Latitude = (bdlocation != null && bdlocation.mLatitude != 0.0) ? Utils.setDoubleNumberDecimalDigit(bdlocation.mLatitude, 10) : SosActivity.this
                .getResources().getString(R.string.common_lat_value);

    }

    /**
     * 注册广播
     */
    private void registerBroadcastReceiver() {
        receiver = new BDAlarmReceiver();
        IntentFilter filter = new IntentFilter(Utils.ALARM_CHANGED_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(receiver);
    }

}
