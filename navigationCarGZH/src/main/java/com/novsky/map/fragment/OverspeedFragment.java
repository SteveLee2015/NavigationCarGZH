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
 * 超速报警发送短报文
 * 
 * @author GP
 */
public class OverspeedFragment extends Fragment {

	private Context mContext;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View contentView;
		mContext = getActivity();
		contentView = View.inflate(mContext,
				R.layout.fragment_overspeed_manager, null);
		initUI(contentView);
		return contentView;
	}

	/**
	 * 初始化UI
	 */
	public void initUI(View contentView) {

		final SharedPreferences overSpeedNum = mContext.getSharedPreferences(
				"BD_OVER_SPEED_NUM", Context.MODE_PRIVATE);
		final SharedPreferences overSpeedMax = mContext.getSharedPreferences(
				"BD_OVER_SPEED_MAX", Context.MODE_PRIVATE);
		final EditText edit = (EditText) contentView
				.findViewById(R.id.overspeed_num);
		final EditText edit2 = (EditText) contentView
				.findViewById(R.id.overspeed_max);
		// 数据回显
		String overSpeedSee = overSpeedNum.getString("BD_OVER_SPEED_NUM",
				"");
		String overSpeedSeemax = overSpeedMax.getString("BD_OVER_SPEED_MAX",
				"");
		edit.setText(overSpeedSee);
		edit2.setText(overSpeedSeemax);
		// 确认按钮
		Button enter = (Button) contentView
				.findViewById(R.id.overspeed_num_btn);
		enter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (edit.getText().toString().trim().isEmpty()) {
					Toast.makeText(mContext, "请填写北斗卡号！", Toast.LENGTH_SHORT).show();
					return;
				}
				if (edit2.getText().toString().trim().isEmpty()) {
					Toast.makeText(mContext, "请填写限制速度！", Toast.LENGTH_SHORT).show();
					return;
				}
				if (!edit.getText().toString().trim().isEmpty()
						&& !edit2.getText().toString().trim().isEmpty()) {

					// 往sp中存放数据
					overSpeedNum
							.edit()
							.putString("BD_OVER_SPEED_NUM",
									edit.getText().toString().trim()).commit();
					overSpeedMax
					.edit()
					.putString("BD_OVER_SPEED_MAX",
							edit2.getText().toString().trim()).commit();
					String aa= overSpeedNum.getString("BD_OVER_SPEED_NUM", "");
					String bb =overSpeedMax.getString("BD_OVER_SPEED_MAX", "");
					Toast.makeText(mContext, aa +"超速报告设置成功!"+bb, Toast.LENGTH_SHORT)
					.show();
				} else {
					// 填入的数据为空
					
				}
			}
		});
		Button clear = (Button) contentView
				.findViewById(R.id.overspeed_num_clear_btn);
		clear.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				edit.setText("");
				edit2.setText("");
				overSpeedNum
				.edit()
				.putString("BD_OVER_SPEED_NUM",
						null).commit();
		overSpeedMax
				.edit()
				.putString("BD_OVER_SPEED_MAX",
						null).commit();
		String aa = overSpeedNum.getString("BD_OVER_SPEED_NUM", "");
		String bb = overSpeedMax.getString("BD_OVER_SPEED_MAX", "");
		Toast.makeText(mContext, aa +"已关闭超速报告"+bb, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void onClick(View view) {
	}
}
