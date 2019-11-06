package com.novsky.map.main;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.mapabc.android.activity.R;
import com.novsky.map.util.DatabaseHelper.CustomColumns;
import com.novsky.map.util.DatabaseOperation;
import com.novsky.map.util.DswLog;
import com.novsky.map.util.TabSwitchActivityData;
import com.novsky.map.util.Utils;

/**
 * 发信箱  有错误 list泛型需要修改
 *
 * @author steve
 */
public class SendedMsgActivity extends Activity {

    private ListView listView = null;
    private SendMsgAdapter adapter = null;
    private List<Map<String, Object>> list = null;
    private String TAG = "SendedMsgActivity";
    private Context mContext = this;
    private TabSwitchActivityData mInstance = TabSwitchActivityData
            .getInstance();

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sended_msg);
        LinearLayout linear = (LinearLayout) this.findViewById(R.id.msg_sended_layout);
        /* 增加背景图片 */
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.bg);
        BitmapDrawable bd = new BitmapDrawable(bitmap);
        bd.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        bd.setDither(true);
        linear.setBackgroundDrawable(bd);
        mInstance.setTabFlag(2);
        listView = (ListView) this.findViewById(R.id.msg_sended_listview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        list = new ArrayList<Map<String, Object>>();
        /*1.从数据库中查询所有的短信数据,
         *  如果数据库没有数据则发送指令请求最新插入的数据*/
        DatabaseOperation operation = DatabaseOperation.getInstance();
        Cursor cursor = operation.getSendMessages();
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        while (cursor.moveToNext()) {
            Map<String, Object> map = new HashMap<String, Object>();
            DswLog.log(TAG, cursor.getColumnIndex(CustomColumns.COLUMNS_SEND_ADDRESS) + "," + cursor.getString(cursor.getColumnIndex(CustomColumns.COLUMNS_SEND_ADDRESS)), 'i');
            String msg = cursor.getString(cursor.getColumnIndex(CustomColumns.COLUMNS_MSG_CONTENT));
            if (msg.length() > 28) {
                msg = msg.substring(0, 26) + "...";
            }
            map.put("COLUMNN_ID", cursor.getString(cursor.getColumnIndex(CustomColumns._ID)));
            map.put("SEND_CONTENT_SIZE", cursor.getString(cursor.getColumnIndex(CustomColumns.COLUMNS_MSG_LEN)));
            map.put("SEND_CONTENT", msg);
            String date = cursor.getString((cursor.getColumnIndex(CustomColumns.COLUMNS_SEND_TIME)));
            /*
             *1.如果短信日期不是当天的信息,则仅仅显示月、日
             *2.如果短信日期不是当年的信息,则显示年、月、日
             */
            String reg = "[0-9,-]{2}:[0-9,-]{2}";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(date);
            if (matcher.matches()) {//如果事件格式00:00
                date = date.replaceAll("-", "0");
            } else {//時間格式yyyy-MM-dd HH:mm:ss
                String[] date1 = date.split(" ");
                String[] time = date1[0].split("-");
                if (time[2].equals(Utils.showTwoBitNum(day)) &&
                        time[1].equals(Utils.showTwoBitNum(month)) &&
                        time[0].equals(String.valueOf(year))) {
                    //显示时分秒
                    date = date1[1];
                } else if (time[2].equals(Utils.showTwoBitNum(day)) &&
                        (!time[1].equals(Utils.showTwoBitNum(month)) ||
                                !time[0].equals(String.valueOf(year)))) {
                    //显示月、日
                    date = time[1] + "月" + time[2] + "日";

                } else if (!time[2].equals(Utils.showTwoBitNum(day)) &&
                        !time[1].equals(Utils.showTwoBitNum(month)) &&
                        !time[0].equals(String.valueOf(year))) {
                    //显示年、月、日
                    date = time[0] + "年" + time[1] + "月" + time[2] + "日";
                }
            }
            map.put("SEND_DATE", date);
            String flag = cursor.getString((cursor.getColumnIndex(CustomColumns.COLUMNS_FLAG)));
            Log.i("SendedMsgActivity", flag + "");
//			if(flag!=null&&"0".equals(flag)){
            map.put("SEND_ID", "收件人:" + cursor.getString(cursor.getColumnIndex(CustomColumns.COLUMNS_SEND_ADDRESS)));
            map.put("MESSAGE_FLAG", this.getResources().getDrawable(R.drawable.message_received_box));
//			}else if(flag!=null&&"1".equals(flag)){
//				map.put("SEND_ID", "发件人:"+cursor.getString(cursor.getColumnIndex(CustomColumns.COLUMNS_SEND_ADDRESS)));
//				map.put("MESSAGE_FLAG", this.getResources().getDrawable(R.drawable.message_sended_box));
//			}else
            if (flag != null && "2".equals(flag)) {
                String userid = cursor.getString(cursor.getColumnIndex(CustomColumns.COLUMNS_SEND_ADDRESS));
                if (userid != null && !"".equals(userid)) {
                    map.put("SEND_ID", userid);
                } else {
                    map.put("SEND_ID", "草稿");
                }
                map.put("MESSAGE_FLAG", this.getResources().getDrawable(R.drawable.message_caogao));
            } else if (flag != null && "3".equals(flag)) {
                map.put("SEND_ID", "未读: " + cursor.getString(cursor.getColumnIndex(CustomColumns.COLUMNS_SEND_ADDRESS)));
                map.put("MESSAGE_FLAG", this.getResources().getDrawable(R.drawable.message_not_read));
            } else {

            }
            list.add(map);
        }
        /*2.把数据转换成List*/
        adapter = new SendMsgAdapter(SendedMsgActivity.this, null);
        listView.setAdapter(adapter);
        cursor.close();
        /**
         * 增加选项
         */
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String rowId = String.valueOf(list.get(position).get("COLUMNN_ID"));
                /*转发功能*/
                Intent intent = new Intent();
                intent.putExtra("MSG_DEL_ID", rowId);
                intent.setClass(mContext, MsgZhuanFaActivity.class);
                mContext.startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int index, long arg3) {
                AlertDialog.Builder alert = new AlertDialog.Builder(SendedMsgActivity.this.getParent());
                alert.setTitle("删除短信");
                alert.setMessage("是否删除该条短信?");
                alert.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int index1) {
                        DatabaseOperation operation = DatabaseOperation.getInstance();
                        int id = Integer.valueOf(String.valueOf(list.get(index).get("COLUMNN_ID")));
                        boolean istrue = operation.delete(id);
                        if (istrue) {
                            list.remove(index);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.bd_msg_del_success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.bd_msg_del_fail), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                alert.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });
                alert.show();
                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.delete_message_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all_messages:
                AlertDialog.Builder builder = new AlertDialog.Builder(SendedMsgActivity.this.getParent());
                builder.setTitle("全部删除短信");
                builder.setMessage("是否全部删除短信?");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        final DatabaseOperation operation = DatabaseOperation.getInstance();
                        boolean istrue = operation.deleteSendMessages();
                        if (istrue) {
                            list.clear();
                            adapter.notifyDataSetChanged();
                            Toast.makeText(mContext, "删除所有发件箱内容成功!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(mContext, "删除所有发件箱内容失败!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
                builder.show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
