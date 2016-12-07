/**
 * 
 */
package com.mapabc.android.activity.setting;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.mapabc.android.activity.R;
import com.mapabc.android.activity.base.BaseActivity;
import com.mapabc.android.activity.listener.BackListener;
import com.mapabc.android.activity.setting.adapter.SettingListAdapter;
import com.mapabc.android.activity.utils.SettingForLikeTools;
import com.mapabc.naviapi.MapAPI;

/**
 * desciption:更多下的地图设置
 */
public class SettingMapActivity extends BaseActivity implements
OnClickListener, OnItemClickListener{
	
	private static final String TAG = "SettingMapActivity";
	private ListView SettingMapListView;
	private SettingListAdapter itemAdapter;
	private static int TEMPINDEX = 0;// 临时保存系统参数索引
	public static boolean PRIORITYPOI[] = null;// 临时保存优先显示的POI值
	private static boolean isclick = false;
	private  String[] nameArray = null;
	private  int[] imgArray = null;
	public static ExecutorService executorService = Executors
			.newFixedThreadPool(10);
	public static final int REALTIMETRAFFIC = 201;// 加载实时交通
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settingforlike_main);
		initTop();
		nameArray=this.getResources().getStringArray(R.array.settingformap);
		imgArray=new int[]{
				    R.drawable.settingforlike_displaymodel,
					R.drawable.settingforlike_dayornight,
					R.drawable.settingforlike_powermodule
					};
		SettingMapListView = (ListView)findViewById(R.id.lv_settingForLikeItem);
		itemAdapter = new SettingListAdapter(this,nameArray,imgArray,"SettingMapActivity");
		SettingMapListView.setAdapter(itemAdapter);
		SettingMapListView.setOnItemClickListener(this);
	}
	
	/**
	 * 初使化顶部控件
	 */
	private void initTop() {
		ImageButton resetImageButton = (ImageButton) findViewById(R.id.ib_reset);
		resetImageButton.setOnClickListener(this);
		TextView topicTextView = (TextView) findViewById(R.id.tv_topic);
		topicTextView.setText(this.getResources().getStringArray(R.array.otherfunctionitems)[3]);
		ImageButton backImageButton = (ImageButton) findViewById(R.id.ib_menu_back);
		backImageButton.setOnClickListener(new BackListener(this, false, false));
	}

	/**
	 * 重置恢复出厂设置
	 */
	private void reset() {
		Log.e(TAG, "恢复出厂设置");
		try {
			SettingForLikeTools.setMapSettingState(SettingMapActivity.this, 1);
			SettingForLikeTools.resetPara(SettingMapActivity.this,"mapsetting");

		} catch (Exception ex) {
			Log.e(TAG, "ERROR", ex);
		}
		this.refresh();
	}

	/**
	 * 数据刷新
	 */
	public void refresh() {
		if (itemAdapter != null)
			itemAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.ib_reset:
			AlertDialog.Builder mBuilder = new AlertDialog.Builder(
					SettingMapActivity.this);
			mBuilder.setTitle(SettingMapActivity.this.getResources().getString(R.string.common_tip));
			mBuilder.setMessage(SettingMapActivity.this.getResources().getString(R.string.reset_tip));
			mBuilder.setPositiveButton(R.string.common_btn_positive,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int which) 
						{
							// TODO Auto-generated method stub
							SettingMapActivity.this.reset();
						}
					});
			mBuilder.setNegativeButton(R.string.common_btn_negative,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							// TODO Auto-generated method stub

						}
					});
			mBuilder.show();
			break;

		default:
			break;
		}
		
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		try {
			switch (position) {
				case 0:// 视图模式
					this.displayModelDialog(position);
					break;
				case 1:// 昼夜模式
					this.dayOrNightDialog(position);
					break;
				case 2:// 省电模式
					SettingForLikeTools.setPowerModel(this);
					this.refresh();
					break;
				default:
					break;
			}
		} catch (Exception ex) {
		}
	}
	
	/**
	 * 视图模式设置对话框
	 * 
	 * @param position
	 *            索引
	 * @return
	 */
	public void displayModelDialog(int position) {
		isclick = false;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(this.nameArray[position]);
		builder.setSingleChoiceItems(
				this.getResources().getStringArray(R.array.displaymodelitem),
				SettingForLikeTools.getMapModel(this),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface arg0, int pos) {
						SettingMapActivity.TEMPINDEX = pos;
						isclick = true;
					}

				});
		builder.setPositiveButton(R.string.common_confirm,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface arg0, int pos) {
						if (!isclick) {
							return;
						}
						try {
							Log.e(TAG, "=====视图模式======="
									+ SettingMapActivity.TEMPINDEX);
							SettingForLikeTools.setMapModel(
									SettingMapActivity.TEMPINDEX,
									SettingMapActivity.this);
							SettingMapActivity.this.refresh();

						} catch (Exception ex) {

						}
					}
				});
		builder.setNegativeButton(R.string.common_cancel, null);
		builder.show();
	}
	
	/**
	 * 昼夜模式对话框
	 * 
	 * @param position
	 *            索引
	 * @return
	 */
	public void dayOrNightDialog(int position) {
		isclick = false;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(this.nameArray[position]);
		builder.setSingleChoiceItems(
				this.getResources().getStringArray(R.array.dayornightitem),
				SettingForLikeTools.getDayOrNight(this),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int pos) {
						SettingMapActivity.TEMPINDEX = pos;
						isclick = true;
					}

				});
		builder.setPositiveButton(R.string.common_confirm,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface arg0, int pos) {
						if (!isclick) {
							return;
						}
						try {
							SettingForLikeTools.setDayOrNight(
									SettingMapActivity.this,
									SettingMapActivity.TEMPINDEX);
							SettingMapActivity.this.refresh();
						} catch (Exception ex) {
							Log.e(TAG, "ERROR", ex);
						}
					}
				});
		builder.setNegativeButton(R.string.common_cancel, null);
		builder.show();
	}
	
	/**
	 * 地图底图设置对话框
	 * 
	 * @param position
	 *            索引
	 * @return
	 */
	public void mapColorDialog(int position) {
		isclick = false;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(this.nameArray[position]);
		builder.setSingleChoiceItems(
				getResources().getStringArray(R.array.mapcoloritem),
				SettingForLikeTools.getIntSysPara(this,
						SettingForLikeTools.MAPPALETTE, 0),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface arg0, int pos) {
						SettingMapActivity.TEMPINDEX = pos;
						isclick = true;
					}
				});
		builder.setPositiveButton(R.string.common_confirm,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface arg0, int pos) {
						if (!isclick) {
							return;
						}
						try {
							SettingForLikeTools.setMapColor(TEMPINDEX,
									SettingMapActivity.this);
							SettingMapActivity.this.refresh();
							String mode = MapAPI.getInstance().getMapStyle();
							Log.e(TAG, "====MapStyle====" + mode);

						} catch (Exception ex) {

						}
					}
				});
		builder.setNegativeButton(R.string.common_cancel, null);
		builder.show();
	}
	

	
}
