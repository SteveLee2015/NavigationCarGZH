package com.novsky.map.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.BDEventListener;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.utils.FMSharedPreferenceUtils;
import com.mapabc.android.activity.utils.ReceiverAction;
import com.novsky.map.main.BDContactActivity;
import com.novsky.map.main.BDResponseListener;
import com.novsky.map.util.BDContactColumn;
import com.novsky.map.util.BDMessageManager;
import com.novsky.map.util.Utils;

/**
 * 友邻位置 用ListView显示所有的数据,并通过
 *
 * @author steve
 */
public class SosSettingFragment extends Fragment {
    /**
     * 通讯录标识
     */
    private final int REQUEST_CONTACT = 1;
    private EditText bdloc_userAddress_et, ed_jyxx;
    private String TAG = "SosSettingFragment";
    private View bdloc_report_submit_btn;
    /**
     * RDSS管理类
     */
    private BDCommManager manager = null;

    /**
     * 反馈信息监听器
     */
    private BDEventListener fkilistener = null;

    /**
     * 传输类型
     */
    private int mTranslateType = 0;

    /**
     * 短信通信模式
     */
    private int mMsgCommunicationType = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.sos_setting_fragment, null);
        View bdloc_linker = view.findViewById(R.id.bdloc_linker);
        View bdloc_report_submit_btn = view.findViewById(R.id.bdloc_report_submit_btn);
        bdloc_userAddress_et = (EditText) view.findViewById(R.id.bdloc_userAddress_et);
        ed_jyxx = (EditText) view.findViewById(R.id.ed_jyxx);
        fkilistener = new BDResponseListener(getActivity());
        manager = BDCommManager.getInstance(getActivity().getBaseContext());
        try {
            manager.addBDEventListener(fkilistener);
        } catch (BDParameterException e) {
            e.printStackTrace();
        } catch (BDUnknownException e) {
            e.printStackTrace();
        }
        bdloc_linker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), BDContactActivity.class);
                intent.setData(BDContactColumn.CONTENT_URI);
                startActivityForResult(intent, REQUEST_CONTACT);
            }
        });
        bdloc_report_submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bdloc_userAddress_et.getText().toString().trim().length() == 0) {
                    Toast.makeText(getActivity().getApplicationContext(), "平台号码不能为空", Toast.LENGTH_SHORT).show();
                } else if (ed_jyxx.getText().toString().trim().length() == 0) {
                    Toast.makeText(getActivity().getApplicationContext(), "平台号码不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    String message = ed_jyxx.getText().toString();
                    String sendAddress = bdloc_userAddress_et.getText().toString();

                    if (sendAddress.contains("(")) {
                        sendAddress = sendAddress.substring(sendAddress.lastIndexOf("(") + 1, sendAddress.lastIndexOf(")"));
                    }
                    mTranslateType = Utils.checkMsg(message);
                    BDMessageManager messageManager = BDMessageManager.getInstance();
                    messageManager.setMessage(message);
                    messageManager.setMsgContentType(mMsgCommunicationType);
                    messageManager.setUserAddress(sendAddress);
                    FMSharedPreferenceUtils.getInstance().putString("MY_JYXX", ed_jyxx.getText().toString());
                    FMSharedPreferenceUtils.getInstance().putString("MY_ADDRESS_SOSSET", bdloc_userAddress_et.getText().toString());
                    Toast.makeText(getActivity().getApplicationContext(), "设置成功！", Toast.LENGTH_SHORT).show();

                    try {
                        manager.sendSOSSettingsCmd(sendAddress, 2, Utils.getSOSSettings(message));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        addReceiver();
        bdloc_userAddress_et.setText(FMSharedPreferenceUtils.getInstance().getString("MY_ADDRESS_SOSSET", ""));
        ed_jyxx.setText(FMSharedPreferenceUtils.getInstance().getString("MY_JYXX", ""));
        return view;
    }


    private void addReceiver() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(ReceiverAction.ACTION_RD_REPORT);
        getActivity().registerReceiver(newMessageReceiver, filter);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CONTACT) {
            if (resultCode == getActivity().RESULT_OK) {
                if (data == null) {
                    return;
                }
                Uri result = data.getData();
                Cursor cursor = getActivity().getContentResolver().query(result, BDContactColumn.COLUMNS, null, null, null);
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

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        Utils.destoryFriendLocationNotification(getActivity());

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(newMessageReceiver);
        Log.e(TAG, "onDestroy");
    }

    /**
     * 数据更新广播
     */
    BroadcastReceiver newMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            switch (action) {
                case ReceiverAction.ACTION_RD_REPORT: {
                    //更新数据
                    onStart();
                    break;
                }
            }

        }
    };
}
