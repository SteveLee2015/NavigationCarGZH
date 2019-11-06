package com.novsky.map.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.BDEventListener;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.Network4GListener;
import com.mapabc.android.activity.R;
import com.novsky.map.main.BDContactActivity;
import com.novsky.map.main.BDResponseListener;
import com.novsky.map.util.BDContactColumn;
import com.novsky.map.util.BDMSG;
import com.novsky.map.util.BDMessageManager;
import com.novsky.map.util.DatabaseOperation;
import com.novsky.map.util.MessgeUsualOperation;
import com.novsky.map.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 新建短信 功能描述:
 * 1.点击"通讯录"按钮,选择要发送短信的人,并返回现在在收信人中。
 * 2.点击"发送"按钮,提示发送成功并跳转至短信列表，第一条显示显示发送的短信内容。
 *
 * @author steve
 */
public class Send4GMsgRequestFragment extends Fragment implements OnClickListener {


    private final String TAG = "SendMsgRequestActivity";

    private ImageView frequencyBtn = null;
    private TextView showNum = null;
    private LinearLayout msgLayout = null;
    private EditText userAddress = null, msgContent = null;
    private Button addressBook = null, manageMessage = null, sendMsgBtn = null;

    private final int REQUEST_CONTACT = 1;
    private int mMsgCommunicationType = 1, mTranslateType = 0;

    private BDCommManager manager = null;
    private ClipboardManager clipboardManager = null;

    private BDEventListener fkilistener = null;
    private String sendAddress = "", message = "";

    private static final int DELAY_SEND_COMMINT = 0x100001;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DELAY_SEND_COMMINT:
                    try {
                        byte[] arr = Utils.getMsg4GConfirmCmd(true);
                        manager.sendNetwork4GCmd(arr);
                    } catch (android.location.BDUnknownException e) {
                        e.printStackTrace();
                    } catch (android.location.BDParameterException e) {
                        e.printStackTrace();
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
            if (msg.replaceAll(" ","").equalsIgnoreCase(">")) {
                try {
                    byte[] byteArr =  Utils.get4GMsgArr("",sendAddress  , message);
                    byte[] cmd4G = Utils.getMsg4GBodyCmd(Utils.bytesToHexString2(byteArr));
                    manager.sendNetwork4GCmd(cmd4G);
                    mHandler.sendEmptyMessageDelayed(DELAY_SEND_COMMINT , 500);
                } catch (android.location.BDUnknownException e) {
                    e.printStackTrace();
                } catch (android.location.BDParameterException e) {
                    e.printStackTrace();
                }
            } else if (msg.startsWith("+CMS ERROR")) {
                mHandler.sendEmptyMessage(DELAY_SEND_COMMINT);
            }
        }
    };

    private TextWatcher textWatcher = new TextWatcher() {
        private CharSequence temp;
        private int selectionStart;
        private int selectionEnd;
        private Toast mToast = null;
        private boolean isOver = false;

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            temp = s;
        }

        public void afterTextChanged(Editable s) {
            /*判断写入的汉字的个数和数字字母个数,汉字个数*14 数字字母个数×4*/
            int num = Utils.checkStrBits(s.toString());
            int flag = Utils.checkMsg(s.toString());

            int temp = Utils.getMessageMaxLength();
            if (temp < 0) {
                temp = 0;
            }

            showNum.setText("当前输入：" + num + "/" + temp + "bit");
            selectionStart = msgContent.getSelectionStart();
            selectionEnd = msgContent.getSelectionEnd();
            if (num > temp) {
                if (!isOver) {
                    mToast = Toast.makeText(getActivity(), "输入超过最长字符，将不能发送短信!", Toast.LENGTH_SHORT);
                    mToast.show();
                    isOver = true;
                }
            } else {
                if (isOver) {
                    mToast.cancel();
                    isOver = false;
                }
            }
        }
    };

    private OnLongClickListener onLongClickListener = new OnLongClickListener() {
        @SuppressLint("NewApi")
        @Override
        public boolean onLongClick(View arg0) {
            int startIndex = msgContent.getSelectionStart();
            msgContent.requestFocus();
            if (!msgContent.getText().toString().equals("") && startIndex < msgContent.getText().toString().length()) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(Send4GMsgRequestFragment.this.getActivity());
                dialog.setPositiveButton("复制北斗短报文内容!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        ClipData data = ClipData.newPlainText("短信内容", msgContent.getText().toString());
                        clipboardManager.setPrimaryClip(data);
                        Toast.makeText(getActivity(), "已复制到剪贴板!", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
                return true;
            } else {
                return false;
            }
        }
    };

    private OnLongClickListener addressLongClickListener = new OnLongClickListener() {
        @SuppressLint("NewApi")
        @Override
        public boolean onLongClick(View arg0) {
            int startIndex = userAddress.getSelectionStart();
            userAddress.requestFocus();
            if (!userAddress.getText().toString().equals("") && startIndex < userAddress.getText().toString().length()) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(Send4GMsgRequestFragment.this.getActivity());
                dialog.setMessage(userAddress.getText().toString());
                dialog.setPositiveButton("复制北斗SIM卡号", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        ClipData data = ClipData.newPlainText("发短信北斗SIM卡号", userAddress.getText().toString());
                        clipboardManager.setPrimaryClip(data);
                        Toast.makeText(getActivity(), "已复制到剪贴板!", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
                return true;
            } else {
                return false;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*UI初始化*/
    public void initUI(View view) {
        fkilistener = new BDResponseListener(getActivity());
        manager = BDCommManager.getInstance(getActivity().getBaseContext());
        clipboardManager = (ClipboardManager) this.getActivity().getSystemService(this.getActivity().CLIPBOARD_SERVICE);
        userAddress = (EditText) view.findViewById(R.id.msg_user_address_edittxt);
        msgContent = (EditText) view.findViewById(R.id.bd_msg_content_edittxt);
        addressBook = (Button) view.findViewById(R.id.msg_user_address_book);
        manageMessage = (Button) view.findViewById(R.id.msg_manager_imageview);
        msgLayout = (LinearLayout) view.findViewById(R.id.bd_msg_layout);
        sendMsgBtn = (Button) view.findViewById(R.id.bd_msg_send_btn);
        frequencyBtn = (ImageView) view.findViewById(R.id.bd_msg_feq_btn);
        showNum = (TextView) view.findViewById(R.id.show_msg_num);
        showNum.setText("当前输入：0/" + Utils.getMessageMaxLength() + "bit");
        frequencyBtn.setOnClickListener(this);
        manageMessage.setOnClickListener(this);
        addressBook.setOnClickListener(this);
        sendMsgBtn.setOnClickListener(this);

        BDMessageManager messageManager = BDMessageManager.getInstance();
        msgContent.setText(messageManager.getMessage());
        userAddress.setText(messageManager.getUserAddress());

        SharedPreferences messagePreferences = getActivity().getSharedPreferences("BD_MESSAGE_CONTACT_PREF", getActivity().MODE_PRIVATE);
        String contactName = messagePreferences.getString("MESSAGE_CONTACT_NAME", "");
        if (contactName != null && !"".equals(contactName)) {
            userAddress.setText(contactName);
        }
        userAddress.setOnLongClickListener(addressLongClickListener);
        msgContent.setOnLongClickListener(onLongClickListener);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.communication_request, container, false);
        initUI(rootView);
        try {
            manager.addBDEventListener(fkilistener , network4GListener);
        } catch (BDParameterException e) {
            e.printStackTrace();
        } catch (BDUnknownException e) {
            e.printStackTrace();
        }
        msgContent.addTextChangedListener(textWatcher);
        mHandler.sendEmptyMessage(DELAY_SEND_COMMINT);
        return rootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            manager.removeBDEventListener(fkilistener , network4GListener);
        } catch (BDParameterException e) {
            e.printStackTrace();
        } catch (BDUnknownException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.msg_manager_imageview: {
                createMessageWordsDialog();
                break;
            }
            case R.id.msg_user_address_book: {
                Intent intent = new Intent();
                intent.setClass(getActivity(), BDContactActivity.class);
                intent.setData(BDContactColumn.CONTENT_URI);
                Utils.BD_MESSAGE_PAGER_INDEX = 1;
                startActivityForResult(intent, getActivity().RESULT_FIRST_USER);
                break;
            }
            case R.id.bd_msg_send_btn: {
                try {
                    message = msgContent.getText().toString();
                    sendAddress = userAddress.getText().toString();
                    /* 判断用户地址是否为空! */
                    if (sendAddress.equals("")) {
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.bd_address_no_content), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    /* 判断短信内容是否为空 */
                    if (message.equals("")) {
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.bd_msg_no_content), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (sendAddress.contains("(")) {
                        sendAddress = sendAddress.substring(sendAddress.lastIndexOf("(") + 1, sendAddress.lastIndexOf(")"));
                    }
                    mTranslateType = Utils.checkMsg(message);
                    BDMessageManager messageManager = BDMessageManager.getInstance();
                    messageManager.setMessage(message);
                    messageManager.setMsgContentType(mMsgCommunicationType);
                    messageManager.setUserAddress(sendAddress);

                    int length =  Utils.get4GMsgLength("",sendAddress  , message);
                    String cmd = Utils.getMsg4GHeadCmd(length);
                    manager.sendNetwork4GCmd(cmd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                saveToDatabase();
                msgContent.setText("");
                break;
            }
            default:
                break;
        }
    }

    /**
     * 保存至数据库
     */
    public void saveToDatabase() {
        /* 在数据库中保存该数据 */
        DatabaseOperation operation = DatabaseOperation.getInstance();
        BDMSG msg = new BDMSG();
        String address = userAddress.getText().toString();
        msg.setColumnsUserAddress(address);
        msg.setColumnsMsgType(mMsgCommunicationType + "," + mTranslateType);
        msg.setColumnsSendAddress(address);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        msg.setColumnsSendTime(sdf.format(new Date()));
        msg.setColumnsMsgLen(msgContent.getText().toString().length() + "");
        msg.setColumnsMsgContent(msgContent.getText().toString());
        msg.setColumnsCrc("0");
        msg.setColumnsMsgFlag("1");
        long id = operation.insert4G(msg);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void createMessageWordsDialog() {
        MessgeUsualOperation oper = new MessgeUsualOperation(getActivity());
        final String[] arrayWord = oper.getAllMessagesArray();
        oper.close();
        Dialog dialog = new AlertDialog.Builder(
                Send4GMsgRequestFragment.this.getActivity()).setTitle("常用短语")
                .setItems(arrayWord, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        int startIndex = msgContent.getSelectionStart();
                        String headerStr = msgContent.getText().toString().substring(0, startIndex);
                        String footerStr = msgContent.getText().toString().substring(startIndex);
                        String word = arrayWord[which];
                        if (word.contains(".")) {
                            word = word.substring(word.indexOf(".") + 1).replaceAll(" ", "");
                        }
                        msgContent.setText(headerStr + word + footerStr);
                        msgContent.setSelectAllOnFocus(true);
                        msgContent.setSelection((headerStr + word).length());
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == getActivity().RESULT_FIRST_USER) {
            if (resultCode == getActivity().RESULT_OK) {
                if (data == null) {
                    return;
                }
                String mUserAddress = getUserAddressFromProvider(data.getData());
                SharedPreferences messagePreferences = getActivity().getSharedPreferences("BD_MESSAGE_CONTACT_PREF", getActivity().MODE_PRIVATE);
                messagePreferences.edit().putString("MESSAGE_CONTACT_NAME", mUserAddress).commit();
            }
        }
    }

    @NonNull
    private String getUserAddressFromProvider(Uri result) {

        String mUserAddress = "";
        Cursor cursor = null;

        try {
            cursor = getActivity().getContentResolver().query(result, BDContactColumn.COLUMNS, null, null, null);
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.USER_NAME));
                String num = cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.CARD_NUM));
                mUserAddress = name + "(" + num + ")";
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return mUserAddress;
    }
}