package com.novsky.map.main;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mapabc.android.activity.R;
import com.novsky.map.util.DatabaseOperation;

public class MsgDeleteActivity extends Activity {

	private LinearLayout mLayout=null;
	private Button  delBtn=null;
	private Button  cancleBtn=null;
	private Context mContext=this;
	private int index=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_msg_delete);
		delBtn=(Button)this.findViewById(R.id.msg_del_btn);
		cancleBtn=(Button)this.findViewById(R.id.msg_del_cancle_btn);
		Intent intent=getIntent();
		index=Integer.valueOf(intent.getStringExtra("MSG_DEL_ID"));
		
		delBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/*从数据库中删除*/
				  DatabaseOperation operation=new DatabaseOperation(mContext.getApplicationContext());
				  boolean istrue=operation.delete(index);	
				  if(istrue){
					 Toast.makeText(mContext, mContext.getResources().getString(R.string.bd_msg_del_success), Toast.LENGTH_SHORT).show();  
				  }else{
					  Toast.makeText(mContext,mContext.getResources().getString(R.string.bd_msg_del_fail), Toast.LENGTH_SHORT).show();   
				  }
				  finish();
			}
		});
		/*
		 * 取消按钮
		 * */
		cancleBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

}
