package com.mapabc.android.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.novsky.map.main.BDSendMsgPortActivity;
import com.novsky.map.util.MessgeUsualOperation;
import com.novsky.map.util.TabSwitchActivityData;

public class MsgUsalWordDeleteActivity extends Activity {

	private Context mContext = this;
	private ListView listView = null;
	private LinearLayout mLinerLayout = null;
    private MessgeUsualOperation oper=null;
	private MsgUsalWordDeleteAdapter adapter=null;
	private CheckBox allCheckBox=null;
	private Button  deleteButton=null;
	private Button  cancleButton=null;
	private Map<Integer,Boolean> map=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_msg_usal_delete_word);
		allCheckBox=(CheckBox)this.findViewById(R.id.all_select_checkbox);
		deleteButton=(Button)this.findViewById(R.id.delete_btn);
		cancleButton=(Button)this.findViewById(R.id.cancle_btn);
		map=new HashMap<Integer,Boolean>();
		listView = (ListView) this.findViewById(R.id.msg_word_delete_listview);
		mLinerLayout = (LinearLayout) this.findViewById(R.id.msg_word_layout);
		/* 增加背景 */
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.bg);
		BitmapDrawable bd = new BitmapDrawable(bitmap);
		bd.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		bd.setDither(true);
		mLinerLayout.setBackgroundDrawable(bd);

		oper = new MessgeUsualOperation(this);
		final List<Map<String, Object>> list = oper.getAll();
		oper.close();

		adapter = new MsgUsalWordDeleteAdapter(this, list);
		listView.setAdapter(adapter);
		
		allCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				for(int index=0;index<list.size();index++){
					adapter.list.get(index).put("MESSAGE_WORD_CHECKED", arg1);
				}
				adapter.notifyDataSetChanged();
			}
		});
		deleteButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				int length=adapter.list.size();
				for(int index=0;index<length;index++){
				    Map<String,Object> map=adapter.list.get(index);
				    if(Boolean.valueOf(String.valueOf(map.get("MESSAGE_WORD_CHECKED")))){
				    	boolean istrue=oper.delete(Long.valueOf(String.valueOf(map.get("MESSAGE_WORD_ID"))));
				    	adapter.list.remove(map);
				    }
				}
				adapter.notifyDataSetChanged();
			}
		});
		cancleButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				TabSwitchActivityData mInstance = TabSwitchActivityData
						.getInstance();
				mInstance.setTabFlag(3);
			    Intent intent=new Intent();
			    intent.setClass(mContext, BDSendMsgPortActivity.class);
			    startActivity(intent);     
			}
		});
	}
}
