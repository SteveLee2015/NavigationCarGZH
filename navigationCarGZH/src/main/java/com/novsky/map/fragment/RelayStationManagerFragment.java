package com.novsky.map.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.mapabc.android.activity.R;

/**
 * 中继站设置相关界面
 * 
 * @author llg
 */
public class RelayStationManagerFragment extends Fragment {

	private Context mContext;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View contentView;
		mContext = getActivity();
		contentView = View.inflate(mContext,
				R.layout.fragment_relay_station_manager, null);
		initUI(contentView);
		return contentView;
	}

	/**
	 * 初始化UI
	 */
	public void initUI(View contentView) {

		final SharedPreferences relayStation = mContext.getSharedPreferences(
				"BD_RELAY_STATION_PREF", Context.MODE_PRIVATE);
		final EditText edit = (EditText) contentView
				.findViewById(R.id.relayStationNum);
		// 数据回显
		String relayStationNum = relayStation.getString("BD_RELAY_STATION_NUM",
				"");
		edit.setText(relayStationNum);
		// 确认按钮
		Button enter = (Button) contentView
				.findViewById(R.id.btn_enter_relayStation);
		enter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!edit.getText().toString().trim().isEmpty()) {

					// 往sp中存放数据
					relayStation
							.edit()
							.putString("BD_RELAY_STATION_NUM",
									edit.getText().toString().trim()).commit();
					Toast.makeText(mContext, "中继站号码设置成功!", Toast.LENGTH_SHORT)
					.show();
				} else {
					// 填入的数据为空
					Toast.makeText(mContext, "填入的数据不能为空!", Toast.LENGTH_SHORT)
							.show();
				}
			}
		});
		// 取消按钮
		Button cancel = (Button) contentView
				.findViewById(R.id.btn_cancel_relayStation);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 清空
				edit.setText("");
			}
		});

	}

	public void onClick(View view) {
	}
}
