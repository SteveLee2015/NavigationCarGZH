package com.novsky.map.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.mapabc.android.activity.NaviStudioActivity;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.listener.CustomBDRNSSLocationListener;
import com.novsky.map.main.BDLineNav;
import com.novsky.map.main.BDLineNavOperation;
import com.novsky.map.main.BDPoint;
import com.novsky.map.util.CollectionUtils;
import com.novsky.map.util.Config;

import java.util.List;

/**
 * 路线导航
 *
 * @author steve
 */
public class LineTaskFragment extends Fragment {
    /**
     * 日志标识
     */
    private static final String TAG = "LineTaskFragment";
    /**
     * 导航任务的ListView对象
     */
    private ListView listView = null;
    /**
     * 绑定在ListView组件的所有短信数据适配器对象
     */
    private LineTaskAdapter mLineTaskAdapter = null;
    private TextView noLinePrompt = null;
    private BDRNSSLocation mRnssLocation = null;
    private String newNavId = "";


    private BDLineNavOperation operation = null;

    private BDRNSSLocationListener mBDRNSSLocationListener = new CustomBDRNSSLocationListener() {
        @Override
        public void onLocationChanged(BDRNSSLocation arg0) {
            super.onLocationChanged(arg0);
            mRnssLocation = arg0;
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

    private BDCommManager mBDCommManager = null;

    private List<BDLineNav> navs = null;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String navLineId = intent.getStringExtra("NAVILINEID");
            BDLineNav navi = operation.get(navLineId);
            if (operation.checkLineNavComplete(navLineId)) {
                int count = 0;
                for (BDLineNav mBDLineNav : navs) {
                    if (mBDLineNav.getLineId().equals(navLineId)) {
                        count++;
                    }
                }
                if (count == 0) {
                    navs.add(navi);
                }
                newNavId = navLineId;
            }
            mLineTaskAdapter.notifyDataSetChanged();
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBDCommManager = BDCommManager.getInstance(getActivity().getApplicationContext());
        operation = new BDLineNavOperation(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_nav_line, null);
        listView = (ListView) rootView.findViewById(R.id.nav_line_listview);
        listView.setCacheColorHint(0);
        noLinePrompt = (TextView) rootView.findViewById(R.id.no_nav_line_prompt);
        /*1.从数据库中查询所有的短信数据,如果数据库没有数据则发送指令请求最新插入的数据*/
        navs = operation.getNavLineList();
        mLineTaskAdapter = new LineTaskAdapter(getActivity(), navs);
        if (navs.size() > 100) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("提示");
            builder.setMessage("指令导航数量超过1000条,请删除不必要的指令导航信息!");
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
        if (navs.size() > 0) {
			/*2.把数据转换成List*/
            listView.setAdapter(mLineTaskAdapter);
			/*增加选项*/
            listView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    final BDLineNav bdLineNav = navs.get(position);
                    String lineId = bdLineNav.getLineId();
                    BDLineNav line = operation.get(lineId);
                    if (line == null) return;

                    List<BDPoint> listRoute = line.getPassPoints();
                    //去除重复数据
                    List<BDPoint> listNew = CollectionUtils.removeDuplicateT(listRoute);
                    String info = "";
                    for (int i = 0; i < listNew.size(); i++) {
                        BDPoint mPoint = listNew.get(i);
                        //String s = mPoint.getLat()+","+mPoint.getLatDirection()+","+mPoint.getLon()+","+mPoint.getLonDirection()+"\n";
                        String s = mPoint.getLon() + "," + mPoint.getLonDirection() + "," + mPoint.getLat() + "," + mPoint.getLatDirection() + "\n";
                        info += s;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("查看路线导航信息");
                    //builder.setMessage("路线ID:"+bdLineNav.getLineId()+"\n"+"路线点:\n"+bdLineNav.getPassPointsString());
                    builder.setMessage("路线ID:" + bdLineNav.getLineId() + "\n" + "路线点:\n" + info);
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
            });

            listView.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                               final int position, long arg3) {
                    final BDLineNav bdLineNav = navs.get(position);
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle("删除路线导航");
                    alert.setMessage("是否删除该条路线导航?");
                    alert.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int index) {
                            boolean istrue = operation.delete(bdLineNav.getLineId());
                            if (istrue) {
                                navs.remove(position);
                                mLineTaskAdapter.notifyDataSetChanged();
                                Toast.makeText(getActivity(), "删除路线导航成功!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "删除路线导航失败!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    alert.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();
                        }
                    });
                    alert.setNeutralButton("全部删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean istrue = operation.delete();
                            if (istrue) {
                                navs.clear();
                                mLineTaskAdapter.notifyDataSetChanged();
                                Toast.makeText(getActivity(), "删除路线导航成功!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "删除路线导航失败!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    alert.create().show();
                    return true;
                }
            });
        } else {
            noLinePrompt.setText("当前没有路线导航！");
            noLinePrompt.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    public void onResume() {
        super.onResume();
        try {
            mBDCommManager.addBDEventListener(mBDRNSSLocationListener);
        } catch (BDParameterException e) {
            e.printStackTrace();
        } catch (BDUnknownException e) {
            e.printStackTrace();
        }
        IntentFilter filter = new IntentFilter("com.bd.action.NAVI_LINE_ACTION");
        getActivity().registerReceiver(receiver, filter);

        //取消线路导航 通知
        NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(Config.BDNAL_NOTIFICATION);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
        try {
            mBDCommManager.removeBDEventListener(mBDRNSSLocationListener);
        } catch (BDParameterException e) {
            e.printStackTrace();
        } catch (BDUnknownException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (operation != null) {
            operation.close();
            operation = null;
        }
    }

    public class LineTaskAdapter extends BaseAdapter {

        private ViewHolder viewHolder = null;
        private Context mContext = null;
        private LayoutInflater mInflater = null;
        private List<BDLineNav> list = null;

        /**
         * 构造方法
         *
         * @param mContext
         * @param list
         */
        public LineTaskAdapter(Context mContext, List<BDLineNav> list) {
            this.mContext = mContext;
            this.list = list;
            this.mInflater = LayoutInflater.from(mContext);
        }


        public int getCount() {
            return list.size();
        }


        public Object getItem(int arg0) {
            return list.get(arg0);
        }

        public long getItemId(int arg0) {
            return 0;
        }

        public View getView(final int position, View contentView, ViewGroup parent) {
            if (contentView == null) {
                viewHolder = new ViewHolder();
                contentView = mInflater.inflate(R.layout.item_line_task_msg, null);
                viewHolder.sendId = (TextView) contentView.findViewById(R.id.item_nav_line_id);
                viewHolder.content = (TextView) contentView.findViewById(R.id.item_nav_line_content);
                viewHolder.date = (TextView) contentView.findViewById(R.id.item_nav_line_date);
                viewHolder.naviBtn = (Button) contentView.findViewById(R.id.item_navi_line_btn);
                viewHolder.navLineImage = (ImageView) contentView.findViewById(R.id.nav_line_imageview);
                contentView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) contentView.getTag();
            }
            final BDLineNav nav = list.get(position);

            viewHolder.sendId.setText("路线ID:" + String.valueOf(nav.getLineId()));
            viewHolder.content.setText("路线点:" + nav.getPassPointsString());
            if (newNavId.equals(nav.getLineId())) {
                Bitmap bm = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.message_not_read);
                viewHolder.navLineImage.setImageBitmap(bm);
            } else {
                Bitmap bm = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.message_received_box);
                viewHolder.navLineImage.setImageBitmap(bm);
            }
            viewHolder.naviBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {


                    String lineId = String.valueOf(nav.getLineId());
                    boolean isCompletion = operation.checkLineNavComplete(lineId);

                    if (isCompletion) {
                        Intent notificationIntent = new Intent(mContext, NaviStudioActivity.class);
                        String lineId2 = nav.getLineId();
                        notificationIntent.putExtra("LINE_ID", (!"".equals(lineId)) ? Integer.valueOf(lineId2) : 0);
                        startActivity(notificationIntent);
                    } else {

                        //弹出提示框 ,线路导航数据尚未传递完成
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle("提示");
                        alert.setMessage("该条路线导航数据,尚未传递完成,是否导航?");
                        alert.setPositiveButton("导航", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int index) {
                                //方式1开启导航
                                Intent notificationIntent = new Intent(mContext, NaviStudioActivity.class);
                                String lineId = nav.getLineId();
                                notificationIntent.putExtra("LINE_ID", (!"".equals(lineId)) ? Integer.valueOf(lineId) : 0);
                                startActivity(notificationIntent);
                                //方式2 拒绝导航

                            }
                        });
                        alert.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {

                                arg0.dismiss();
                            }
                        });
                        alert.create().show();
                    }
                }
            });
            return contentView;
        }
    }

    public static class ViewHolder {
        /**
         * 发送ID
         */
        TextView sendId;
        /**
         * 信息大小
         */
        TextView size;
        /**
         * 内容
         */
        TextView content;
        /**
         * 日期
         */
        TextView date;
        /**
         * 导航按钮
         */
        Button naviBtn;
        /**
         * 图片
         */
        ImageView navLineImage;
    }
}
