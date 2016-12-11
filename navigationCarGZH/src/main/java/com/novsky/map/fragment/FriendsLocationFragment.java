package com.novsky.map.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.mapabc.android.activity.NaviStudioActivity;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.utils.ReceiverAction;
import com.novsky.map.main.FriendsLoctionAdapter;
import com.novsky.map.util.FriendsLocationDatabaseOperation;
import com.novsky.map.util.Utils;

import java.util.List;
import java.util.Map;

/**
 * 友邻位置 用ListView显示所有的数据,并通过
 *
 * @author steve
 */
public class FriendsLocationFragment extends Fragment {

    private ListView listView = null;
    private String TAG = "FriendsLocationFragment";
    private FriendsLoctionAdapter adapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.activity_friends_location, null);
        listView = (ListView) view.findViewById(R.id.friends_loc_listview);
        // 监听广播
        addReceiver();
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
        // 数据显示
        final FriendsLocationDatabaseOperation oper = new FriendsLocationDatabaseOperation(
                getActivity());
        final List<Map<String, Object>> list = oper.getAllLocationList();
        oper.close();
        adapter = new FriendsLoctionAdapter(getActivity(), list);
        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                final int index = position;
                final String[] arrayFruit = getActivity().getResources()
                        .getStringArray(R.array.friend_loc_oper);
                Dialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(
                                getActivity()
                                        .getResources()
                                        .getString(
                                                R.string.title_activity_friends_location))
                        .setItems(arrayFruit,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        if (which == 0) {
                                            // 显示地图
                                            Intent notificationIntent = new Intent(
                                                    getActivity(),
                                                    NaviStudioActivity.class);
                                            Map<String, Object> map = list
                                                    .get(index);
                                            String id = String.valueOf(map
                                                    .get("F_ID"));
                                            notificationIntent.putExtra(
                                                    "RERPORT_ROW_ID",
                                                    Integer.valueOf(id));
                                            getActivity().startActivity(
                                                    notificationIntent);

                                           // return;
                                        } else if (which == 1) {
                                            // 删除全部
                                            boolean istrue = oper.deleteAll();
                                            Log.d(TAG,"fuck");
                                            Log.d(TAG,"fuck");
                                            Log.d(TAG,"fuck");
                                            if (!istrue) {
                                                Toast.makeText(
                                                        getActivity(),
                                                        getActivity()
                                                                .getResources()
                                                                .getString(
                                                                        R.string.friend_loc_del_fail),
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                            }else {
                                                list.clear();
                                                adapter.notifyDataSetChanged();
                                            }
                                            oper.close();
                                           // return;
                                        } else if (which==2){
                                            // 从数据库中删除数据
                                            Map<String, Object> map = list
                                                    .get(index);
                                            String id = String.valueOf(map
                                                    .get("F_ID"));
                                            boolean istrue = oper.delete(Long
                                                    .valueOf(id));
                                            oper.close();
                                            if (istrue) {
                                                list.remove(index);
                                                adapter.notifyDataSetChanged();
                                                Toast.makeText(
                                                        getActivity(),
                                                        getActivity()
                                                                .getResources()
                                                                .getString(
                                                                        R.string.friend_loc_del_success),
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                            } else {
                                                Toast.makeText(
                                                        getActivity(),
                                                        getActivity()
                                                                .getResources()
                                                                .getString(
                                                                        R.string.friend_loc_del_fail),
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                           // return;
                                        }
                                    }
                                })
                        .setNegativeButton(
                                getActivity().getResources().getString(
                                        R.string.common_cancle_btn),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                    }
                                }).create();
                dialog.show();
                return false;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        //清除 notification
//		if (Utils.destoryNotification != null) {
//			Utils.destoryFriendLocationNotification(getActivity());
//		}
        Utils.destoryFriendLocationNotification(getActivity());

        final FriendsLocationDatabaseOperation oper = new FriendsLocationDatabaseOperation(
                getActivity());
        final List<Map<String, Object>> list = oper.getAllLocationList();
        oper.close();
        adapter = new FriendsLoctionAdapter(getActivity(), list);
        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {

                Log.d(TAG,"----------**********---------"+position);
                final int index = position;
                final String[] arrayFruit = getActivity().getResources()
                        .getStringArray(R.array.friend_loc_oper);
                Dialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(
                                getActivity()
                                        .getResources()
                                        .getString(
                                                R.string.title_activity_friends_location))
                        .setItems(arrayFruit,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        if (which == 0) {
                                            // 显示地图
                                            Intent notificationIntent = new Intent(
                                                    getActivity(),
                                                    NaviStudioActivity.class);
                                            Map<String, Object> map = list
                                                    .get(index);
                                            String id = String.valueOf(map
                                                    .get("F_ID"));
                                            // SharedPreferences
                                            // locationsharePrefs =
                                            // getActivity().getSharedPreferences("BD_FRIEND_LOCATION_PREF",0);
                                            // locationsharePrefs.edit().putInt("RERPORT_ROW_ID",
                                            // Integer.valueOf(id)).commit();
                                            notificationIntent.putExtra(
                                                    "RERPORT_ROW_ID",
                                                    Integer.valueOf(id));
                                            getActivity().startActivity(
                                                    notificationIntent);
                                        } else if (which==1){


                                            boolean isTrue = oper.deleteAll();
                                            if (!isTrue){
                                                Toast.makeText(
                                                        getActivity(),
                                                        getActivity()
                                                                .getResources()
                                                                .getString(
                                                                        R.string.friend_loc_del_fail),
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                            }else {

                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        list.clear();
                                                        adapter.notifyDataSetChanged();

                                                    }
                                                },1000);

                                            }



                                        }else if (which==2){
                                            // 从数据库中删除数据
                                            Map<String, Object> map = list
                                                    .get(index);
                                            String id = String.valueOf(map
                                                    .get("F_ID"));
                                            boolean istrue = oper.delete(Long
                                                    .valueOf(id));
                                            oper.close();
                                            if (istrue) {
                                                Toast.makeText(
                                                        getActivity(),
                                                        getActivity()
                                                                .getResources()
                                                                .getString(
                                                                        R.string.friend_loc_del_success),
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                            } else {
                                                Toast.makeText(
                                                        getActivity(),
                                                        getActivity()
                                                                .getResources()
                                                                .getString(
                                                                        R.string.friend_loc_del_fail),
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                            list.remove(index);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                })
                        .setNegativeButton(
                                getActivity().getResources().getString(
                                        R.string.common_cancle_btn),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                    }
                                }).create();
                dialog.show();
                return false;
            }
        });
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
