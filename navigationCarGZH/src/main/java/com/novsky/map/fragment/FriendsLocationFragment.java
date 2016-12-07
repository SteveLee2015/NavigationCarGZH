package com.novsky.map.fragment;

import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.mapabc.android.activity.NaviStudioActivity;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.utils.ToolsUtils;
import com.novsky.map.main.FriendsLoctionAdapter;
import com.novsky.map.util.FriendsLocationDatabaseOperation;
import com.novsky.map.util.Utils;

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
		View view = inflater.inflate(R.layout.activity_friends_location, null);
		listView = (ListView) view.findViewById(R.id.friends_loc_listview);
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		// 清除notification
		// 销毁 notification
//		if (Utils.destoryNotification != null) {
//			Utils.destoryFriendLocationNotification(getActivity());
//		}

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
										} else {
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
	public void onResume() {
		super.onResume();

		//清除 notification
		if (Utils.destoryNotification != null) {
			Utils.destoryFriendLocationNotification(getActivity());
		}

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
										} else {
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
}
