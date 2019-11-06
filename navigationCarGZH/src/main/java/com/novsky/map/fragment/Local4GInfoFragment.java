package com.novsky.map.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.Network4GListener;
import com.mapabc.android.activity.R;
import com.novsky.map.main.CustomProgressView;
import com.novsky.map.util.SCConstants;

/**
 * 本地4G卡信息
 *
 * @author steve
 */
public class Local4GInfoFragment extends Fragment {

    private static final String TAG = "LocalMachineInfoActivity";
    private ClipboardManager clipboardManager = null;

    private TextView localAddressTx = null;
    private TextView ICCardStatus = null;
    private CustomProgressView customProgressView = null;
    private BDCommManager manager = null;
    private static final int SEND_READ_4G_CMD = 0x1000001;
    private static final int SHOW_UI = 0x1000002;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SEND_READ_4G_CMD:
                    try {
                        manager.sendNetwork4GCmd(SCConstants.QEURY_4G_SIGNAl);
                        mHandler.sendEmptyMessageDelayed(SEND_READ_4G_CMD , 5000);
                    } catch (BDUnknownException e) {
                        e.printStackTrace();
                    } catch (BDParameterException e) {
                        e.printStackTrace();
                    }
                    break;
                case SHOW_UI:
                    String str = (String) msg.obj;
                    if (str.startsWith("+CSQ:")) {
                        String[] cmds = str.split("\\:");
                        if (cmds.length == 2) {
                            Log.i("TEST" ,"=========================>"+str);
                            Integer value = Integer.valueOf(cmds[1].replaceAll(" " ,""));
                            if (value < 0 || value > 31) {
                                ICCardStatus.setText("无卡");
                            } else {
                                ICCardStatus.setText("有卡");
                            }
                            int level = getLeveBySignal(value);
                            if (level >= 0) {
                                Log.i("TEST" ,"=======================>value =" +value);
                                customProgressView.setProgress(level);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private Network4GListener network4GListener = new Network4GListener() {
        @Override
        public void on4GResponse(String msg) {
            if (msg.startsWith("+CSQ:")) {
                Message message = mHandler.obtainMessage();
                message.what = SHOW_UI;
                message.obj = msg;
                mHandler.sendMessage(message);
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = BDCommManager.getInstance(getActivity());
        try {
            manager.addBDEventListener(network4GListener);
        } catch (BDParameterException e) {
            e.printStackTrace();
        } catch (BDUnknownException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.sendEmptyMessageDelayed(SEND_READ_4G_CMD , 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mHandler.hasMessages(SEND_READ_4G_CMD)) {
            mHandler.removeMessages(SEND_READ_4G_CMD);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_local_4g_info, null);
        clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        localAddressTx = (TextView) view.findViewById(R.id.local_address);
        ICCardStatus = (TextView) view.findViewById(R.id.local_iccard_status);
        customProgressView = view.findViewById(R.id.auto_progress);

        localAddressTx.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View arg0) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setMessage(localAddressTx.getText().toString());
                dialog.setPositiveButton("复制北斗SIM卡号", new DialogInterface.OnClickListener() {

                    @SuppressLint("NewApi")

                    public void onClick(DialogInterface arg0, int arg1) {
                        ClipData data = ClipData.newPlainText("北斗SIM卡号", localAddressTx.getText().toString());
                        clipboardManager.setPrimaryClip(data);
                        Toast.makeText(getActivity(), "已经复制到剪贴板", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
                return true;
            }
        });
        return view;
    }

    public void onStart() {
        super.onStart();
    }

    public void onDestroy() {
        super.onDestroy();
        try {
            manager.removeBDEventListener(network4GListener);
        } catch (BDParameterException e) {
            e.printStackTrace();
        } catch (BDUnknownException e) {
            e.printStackTrace();
        }
    }

    public int getLeveBySignal(int signal) {
        if (signal < 0 || signal > 31) {
            return -1;
        }
        if (signal > 0 && signal < 8) {
            return  1;
        } else if (signal >=8 && signal < 16) {
            return 2;
        } else if (signal >=16 && signal < 24) {
            return 3;
        } else {
            return 4;
        }
    }
}
