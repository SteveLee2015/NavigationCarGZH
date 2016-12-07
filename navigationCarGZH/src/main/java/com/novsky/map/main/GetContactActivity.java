package com.novsky.map.main;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.mapabc.android.activity.R;
import com.novsky.map.util.BDContactColumn;

/**
 * 增加北斗通讯录
 * @author steve
 */
public class GetContactActivity extends Activity {
	
	private static final String TAG="GetContactActivity";
	
	
	private Context mContext=this;
	
	private EditText mUserName=null;
	
	private EditText mCardNumber=null;
	
	private EditText mCardFrequency=null;
	
	private EditText mCardSerialNum=null;
	
	private EditText mUserAddress=null;
	
	private EditText mPhoneNumber=null;
	
	private EditText mRemark=null;
	
	
	private LinearLayout returnView=null;
	
	
	private long rowId=0;
	
	private Cursor cursor=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_get_contact);
		mUserName=(EditText)this.findViewById(R.id.user_name);
		mCardNumber=(EditText)this.findViewById(R.id.card_number);
		mCardFrequency=(EditText)this.findViewById(R.id.card_frequency);
		mCardSerialNum=(EditText)this.findViewById(R.id.card_serial_num);
		mUserAddress=(EditText)this.findViewById(R.id.user_address);
		mPhoneNumber=(EditText)this.findViewById(R.id.user_phone);
		mRemark=(EditText)this.findViewById(R.id.remark);
		returnView=(LinearLayout)this.findViewById(R.id.return_view);
		rowId=this.getIntent().getLongExtra("UPDATE_BD_CONTACT_ID",0);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Uri queryUri=ContentUris.withAppendedId(BDContactColumn.CONTENT_URI, rowId);
		cursor=mContext.getContentResolver().query(queryUri, BDContactColumn.COLUMNS, null, null, null);
		if(cursor.moveToFirst()){
			String name=cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.USER_NAME));
			String cardnum=cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.CARD_NUM));
			String cardserialnum=cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.CARD_SERIAL_NUM));
			int cardfrequency=cursor.getInt(cursor.getColumnIndexOrThrow(BDContactColumn.CARD_FREQUENCY));
			String userAddress=cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.USER_ADDRESS));
			String phoneNum=cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.PHONE_NUMBER));
			String remark=cursor.getString(cursor.getColumnIndexOrThrow(BDContactColumn.REMARK));
			mUserName.setText(name);
			mCardNumber.setText(cardnum);
			mCardFrequency.setText(cardfrequency+"");
			mCardSerialNum.setText(cardserialnum+"");
			mUserAddress.setText(userAddress);
			mPhoneNumber.setText(phoneNum);
			mRemark.setText(remark);
			returnView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
				    Intent intent=new Intent();
				    intent.setClass(mContext, BDContactActivity.class);
				    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				    startActivity(intent);
				    GetContactActivity.this.finish();
				}
			});
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(cursor!=null){
			cursor.close();
		}
	}
}
