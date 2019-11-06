package com.novsky.map.main;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.Network4GListener;
import com.mapabc.android.activity.BottomBaseActivity;
import com.mapabc.android.activity.R;
import com.novsky.map.fragment.FriendLocationTaskFragment;
import com.novsky.map.fragment.LineTaskFragment;
import com.novsky.map.fragment.MsgUsalWordFragment;
import com.novsky.map.fragment.NaviTaskFragment;
import com.novsky.map.fragment.Send4GMsgRequestFragment;
import com.novsky.map.fragment.Sended4GMsgFragment;
import com.novsky.map.util.SCConstants;
import com.novsky.map.util.Utils;

import java.math.BigDecimal;

/**
 * 消息发送4G界面 横屏
 * @author Administrator
 */
public class Send4GMsgLandScapeActivity extends BottomBaseActivity implements Network4GListFragment.Callbacks {

    private static final String TAG = "BDSendMsgLandScapeActivity";

    private Context mContext = this;
    private ImageView m4GImageBtn;

    private static final int SEND_READ_4G_CMD = 0x1000001;
    private static final int SHOW_UI = 0x1000002;

    private BDCommManager manager = null;

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

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SEND_READ_4G_CMD:
                    try {
                        manager.sendNetwork4GCmd(SCConstants.QEURY_4G_SIGNAl);
                        mHandler.sendEmptyMessageDelayed(SEND_READ_4G_CMD, 1000);
                    }  catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case SHOW_UI:
                    String str = (String) msg.obj;
                    if (str.startsWith("+CSQ:")) {
                        String[] cmds = str.split("\\:");
                        if (cmds.length == 2) {
                            Integer value = Integer.valueOf(cmds[1].replaceAll(" " ,""));
                            Log.i("TEST" ,"=======================>value =" +value);
                            setImageByLeveBySignal(value);
                        }
                    }

                    break;
                default:
                    break;
            }
        }
    };

    public void setImageByLeveBySignal(int signal) {
        if (signal < 0 || signal > 31) {
            m4GImageBtn.setImageResource(R.drawable.g4_v_0);
        }
        if (signal > 0 && signal < 8) {
            m4GImageBtn.setImageResource(R.drawable.g4_v_1);
        } else if (signal >=8 && signal < 16) {
            m4GImageBtn.setImageResource(R.drawable.g4_v_2);
        } else if (signal >=16 && signal < 24) {
            m4GImageBtn.setImageResource(R.drawable.g4_v_3);
        } else {
            m4GImageBtn.setImageResource(R.drawable.g4_v_4);
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_4g_msg_land_scape;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title_name.setText("4G短信");
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Send4GMsgLandScapeActivity.this.finish();
            }
        });
        m4GImageBtn = findViewById(R.id.signal_4g_imageBtn);
        manager = BDCommManager.getInstance(this);
        try {
            manager.addBDEventListener(network4GListener);
        } catch (Exception e) {
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
    protected void onDestroy() {
        super.onDestroy();
        try {
            manager.removeBDEventListener(network4GListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemSelected(Integer id) {
        if (id == 1) {
            //新建短信
            Send4GMsgRequestFragment fragment = new Send4GMsgRequestFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.message_detail_container, fragment)
                    .commit();
        } else if (id == 2) {
            //短信列表
            Sended4GMsgFragment sendedMsgfragment = new Sended4GMsgFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.message_detail_container, sendedMsgfragment)
                    .commit();
        } else if (id == 3) {
            //常用短信
            MsgUsalWordFragment msgUsalWordFragment = new MsgUsalWordFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.message_detail_container, msgUsalWordFragment)
                    .commit();
        } else if (id == 4) {
            //指令导航
            NaviTaskFragment naviTaskFragment = new NaviTaskFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.message_detail_container, naviTaskFragment)
                    .commit();
        } else if (id == 5) {
            //路线导航
            LineTaskFragment listTaskFragment = new LineTaskFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.message_detail_container, listTaskFragment)
                    .commit();
        } else if (id == 6) {
            //友邻位置
            FriendLocationTaskFragment friendLocationTaskFragment = new FriendLocationTaskFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.message_detail_container, friendLocationTaskFragment)
                    .commit();
        }
    }
}
