package com.novsky.map.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.BDEventListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.listener.CustomBDRNSSLocationListener;
import com.mapabc.android.activity.utils.FMSharedPreferenceUtils;
import com.novsky.map.main.BDContactActivity;
import com.novsky.map.main.CustomLocationManager;
import com.novsky.map.util.BDCardInfoManager;
import com.novsky.map.util.BDContactColumn;
import com.novsky.map.util.BDTimeCountManager;
import com.novsky.map.util.BDTimeFreqChangedListener;
import com.novsky.map.util.LocSetDatabaseOperation;
import com.novsky.map.util.Utils;

/**
 * 位置报送
 *
 * @author steve
 */
public class BDLocationReportFragmentNew extends Fragment implements OnClickListener, OnCheckedChangeListener {
    /**
     * 日志标识
     */
    private static final String TAG = "ReportActivity";
    /**
     * 短信通信类型
     */
    private static int msgComType = 1;
    /**
     * 服务频度
     */
    private int frequency = 0;
    /**
     * 用户地址
     */
    private String mUserAddress = "";

    private SharedPreferences reportSwitch = null;
    /**
     * RDSS管理类
     */
    private BDCommManager mananger = null;
    /**
     * 用户地址编辑框
     */
    private EditText addressEditText = null;

    private LinearLayout layout = null;
    /**
     * 选择用户的ImageView
     */
    private ImageView selectAddress = null;
    /**
     * 服务去频度编辑框
     */
    private EditText frequncyEditText = null;
    /**
     * 发送按钮
     */
    private Button sendBtn = null;
    /**
     * 北斗卡管理类
     */
    private BDCardInfoManager cardManager = null;
    /**
     * 自定义LocationManager类
     */
    private CustomLocationManager locationManager = null;
    /**
     * 是否启动循环策略
     */
    private CheckBox checkBox = null;


    private ProgressDialog progressDialog = null;


    private BDTimeCountManager timeManager = null;
    /**
     * 反馈信息监听器
     */
    private BDEventListener fkilistener = null;

    BDRNSSLocation rnsslocation = null;

    /**
     * 定义访问模式为私有模式
     */
    public static int MODE = Context.MODE_PRIVATE;

    /**
     * 设置保存时的文件的名称
     */
    public static final String PREFERENCE_NAME = "REPORT_MODEL_ACTIVITY";
    public static final String REPORT_MODEL = "REPORT_MODEL";
    public static final String REPORT_TIANXIAN_VALUE = "REPORT_TIANXIAN_VALUE";
    /**
     * RDSS定位设置数据操作
     */
    private LocSetDatabaseOperation settingDatabaseOper = null;
    /**
     * 数据库数据的总数和
     */
    private int locationSettingTotal = 0;


    private BDRNSSLocationListener mBDRNSSLocationListener = new CustomBDRNSSLocationListener() {
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


    private BDTimeFreqChangedListener timeFreqListener = new BDTimeFreqChangedListener() {
        @Override
        public void onTimeChanged(int remainder_time) {
                /*用消息传递数据*/
            Message msg = new Message();
            msg.arg1 = remainder_time;
            mHandler.sendMessage(msg);
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int remainder_time = msg.arg1;
            int frequ = reportSwitch.getInt("REPORT_FREQUENCY", 0);
            //循环报位
            if (frequ > 0) {
                if (sendBtn != null) {
                    sendBtn.setText("结束报位(" + remainder_time + "秒)");
                }
            }
            //单次报位
            else {
                if (remainder_time != 0) {
                    if (sendBtn != null && getActivity() != null) {
                        if (frequency > 0 && checkBox.isChecked()) {
                            if (frequency - cardManager.getCardInfo().mSericeFeq > remainder_time) {
                                Utils.COUNT_DOWN_TIME = 0;
                            } else {
                                Utils.COUNT_DOWN_TIME = cardManager.getCardInfo().mSericeFeq - (frequency - remainder_time);
                                frequency = 0;
                            }
                        }
                        sendBtn.setText("确  定(" + remainder_time + "秒)");
                    }
                } else {
                    sendBtn.setText("确  认");
                }
            }
        }
    };

    private Context mContext;
    private EditText ed_seconds;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        View rooter = inflater.inflate(R.layout.activity_location_report, null);

        initUI(rooter);
        //initData();
        return rooter;
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    public void initUI(View view) {
        ed_seconds = (EditText) view.findViewById(R.id.ed_seconds);
        addressEditText = (EditText) view.findViewById(R.id.bdloc_userAddress_et);
        selectAddress = (ImageView) view.findViewById(R.id.bdloc_linker);
        sendBtn = (Button) view.findViewById(R.id.bdloc_report_submit_btn);
        selectAddress.setOnClickListener(this);
        sendBtn.setOnClickListener(this);

        settingDatabaseOper = new LocSetDatabaseOperation(getActivity());
        locationSettingTotal = settingDatabaseOper.getSize();
        addressEditText.setText(FMSharedPreferenceUtils.getInstance().getString("MY_ADDRESS_WZ", ""));
        ed_seconds.setText(FMSharedPreferenceUtils.getInstance().getString("MY_SECOND", ""));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bdloc_linker: {
                Intent intent = new Intent();
                intent.setClass(getActivity(), BDContactActivity.class);
                intent.setData(BDContactColumn.CONTENT_URI);
                Utils.BD_MANAGER_PAGER_INDEX = 4;
                startActivityForResult(intent, getActivity().RESULT_FIRST_USER);
                break;
            }
            case R.id.bdloc_report_submit_btn:
                if (ed_seconds.getText() != null && ed_seconds.getText().length() != 0) {
                    int sec = Integer.parseInt(ed_seconds.getText().toString());
                    if (sec < 180) {
                        Toast.makeText(getActivity().getApplicationContext(), "报告频度不能小于180秒", Toast.LENGTH_SHORT).show();
                    } else {
                        String sendAddress = addressEditText.getText().toString();
                        /* 判断用户地址是否为空! */
                        if (addressEditText.getText().toString().equals("")) {
                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.bd_address_no_content), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (ed_seconds.getText().toString().contains("(")) {
                            sendAddress = sendAddress.substring(sendAddress.lastIndexOf("(") + 1, sendAddress.lastIndexOf(")"));
                        }
                        FMSharedPreferenceUtils.getInstance().putString("MY_ADDRESS_WZ", sendAddress);
                        FMSharedPreferenceUtils.getInstance().putString("MY_SECOND", ed_seconds.getText().toString());
                        Toast.makeText(getActivity().getApplicationContext(), "设置成功", Toast.LENGTH_SHORT).show();
//                        Intent alarmServiceIntent = new Intent(getActivity(), CycleAlarmServiceNew.class);
//                        getActivity().stopService(alarmServiceIntent);
//                        getActivity().startService(alarmServiceIntent);
                    }
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "报告频度不能小于180秒", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        layout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == getActivity().RESULT_FIRST_USER) {
            if (resultCode == getActivity().RESULT_OK) {
                if (data == null) {
                    return;
                }
                Uri result = data.getData();
                Cursor cursor = getActivity().getContentResolver().query(result, BDContactColumn.COLUMNS, null, null, null);
                String mUserAddress = "";
                if (cursor.moveToFirst()) {
                    final String name = cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.USER_NAME));
                    final String num = cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.CARD_NUM));
                    mUserAddress = name + "(" + num + ")";
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addressEditText.setText(num);
                            addressEditText.postInvalidate();
                        }
                    }, 500);

                }
                cursor.close();
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("BD_REPORT_CONTACT_PREF", getActivity().MODE_PRIVATE);
                sharedPreferences.edit().putString("CONTACT_NAME", mUserAddress).commit();
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {

        } else {

        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (settingDatabaseOper != null) {
            settingDatabaseOper.close();
        }
    }
}
