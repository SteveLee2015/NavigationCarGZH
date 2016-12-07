package com.novsky.map.main;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mapabc.android.activity.R;
import com.novsky.map.util.BDContactColumn;

/**
 * 增加北斗通讯录
 * @author steve
 */
public class UpdateContactActivity extends Activity {
	
	private static final String TAG="UpdateContactActivity";
	
	private Context mContext=this;
	
	private EditText mUserName=null;
	
	private EditText mCardNumber=null;
	
	private EditText mCardFrequency=null;
	
	private EditText mCardSerialNum=null;
	
	private EditText mUserAddress=null;
	
	private EditText mPhoneNumber=null;
	
	private EditText mRemark=null;
	
	
	private LinearLayout updateImageBtn=null;
	
	
	private long rowId=0;
	
	private Cursor cursor=null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_add_contact);
		mUserName=(EditText)this.findViewById(R.id.user_name);
		mCardNumber=(EditText)this.findViewById(R.id.card_number);
		mCardFrequency=(EditText)this.findViewById(R.id.card_frequency);
		mCardSerialNum=(EditText)this.findViewById(R.id.card_serial_num);
		mUserAddress=(EditText)this.findViewById(R.id.user_address);
		mPhoneNumber=(EditText)this.findViewById(R.id.user_phone);
		mRemark=(EditText)this.findViewById(R.id.remark);
		updateImageBtn=(LinearLayout)this.findViewById(R.id.add_contact);
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
			updateImageBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
				    String name=mUserName.getText().toString();
				    String num=mCardNumber.getText().toString();
				    String frequency=mCardFrequency.getText().toString();
				    String serialnum=mCardSerialNum.getText().toString();
				    String useraddress=mUserAddress.getText().toString();
				    String phone=mPhoneNumber.getText().toString();
				    String remark=mRemark.getText().toString();
				    if("".equals(name)){
				    	Toast.makeText(mContext, "请输入用户名称!", Toast.LENGTH_SHORT).show();
				    }else if("".equals(num)){
				    	Toast.makeText(mContext, "请输入北斗卡号!", Toast.LENGTH_SHORT).show();
				    }else{
				    	    ContentValues values=new ContentValues();
						    values.put(BDContactColumn.USER_NAME, name);
							values.put(BDContactColumn.CARD_NUM, num);
							values.put(BDContactColumn.CARD_LEVEL, "");
							values.put(BDContactColumn.CARD_SERIAL_NUM, serialnum);
							values.put(BDContactColumn.CARD_FREQUENCY, frequency);
							values.put(BDContactColumn.USER_ADDRESS, useraddress);
							values.put(BDContactColumn.PHONE_NUMBER, phone);
							values.put(BDContactColumn.USER_EMAIL, "");
							values.put(BDContactColumn.CHECK_CURRENT_NUM,"0");
							String userName=name.substring(0, 1);
							values.put(BDContactColumn.FIRST_LETTER_INDEX,"");
//								Utils.getIndexByLetter(Utils.stringArrayToString(Utils.getHeadByString(userName))));
							values.put(BDContactColumn.REMARK, remark);
							Uri mUri=ContentUris.withAppendedId(BDContactColumn.CONTENT_URI, rowId);
						    int count=mContext.getContentResolver().update(mUri, values, null, null);
						    if(count>0){
						    	Toast.makeText(mContext, "修改北斗联系人成功!", Toast.LENGTH_SHORT).show();
						    	UpdateContactActivity.this.finish();
						    }else{
						    	Toast.makeText(mContext, "修改北斗联系人失败!", Toast.LENGTH_SHORT).show();
						    }
				    }
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
