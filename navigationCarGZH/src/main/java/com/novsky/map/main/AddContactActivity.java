package com.novsky.map.main;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
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
public class AddContactActivity extends Activity {
	
	private static final String TAG="AddContactActivity";
	
	private Context mContext=this;
	
	private EditText mUserName=null;
	
	private EditText mCardNumber=null;
	
	private EditText mCardFrequency=null;
	
	private EditText mCardSerialNum=null;
	
	private EditText mUserAddress=null;
	
	private EditText mPhoneNumber=null;
	
	private EditText mRemark=null;
	
	private LinearLayout addedImageBtn=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_add_contact);
		//this.setRequestedOrientation(JsNavi.horizon?Configuration.ORIENTATION_LANDSCAPE:Configuration.ORIENTATION_PORTRAIT);
		mUserName=(EditText)this.findViewById(R.id.user_name);
		mCardNumber=(EditText)this.findViewById(R.id.card_number);
		mCardFrequency=(EditText)this.findViewById(R.id.card_frequency);
		mCardSerialNum=(EditText)this.findViewById(R.id.card_serial_num);
		mUserAddress=(EditText)this.findViewById(R.id.user_address);
		mPhoneNumber=(EditText)this.findViewById(R.id.user_phone);
		mRemark=(EditText)this.findViewById(R.id.remark);
		addedImageBtn=(LinearLayout)this.findViewById(R.id.add_contact);
		
		addedImageBtn.setOnClickListener(new OnClickListener(){
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
//						Utils.getIndexByLetter(Utils.stringArrayToString(Utils.getHeadByString(userName))));
					values.put(BDContactColumn.REMARK, remark);
				    Uri uri=mContext.getContentResolver().insert(BDContactColumn.CONTENT_URI, values);
				    if(uri!=null){
				    	Toast.makeText(mContext, "增加北斗联系人成功!", Toast.LENGTH_SHORT).show();
				        AddContactActivity.this.finish(); 
				    	//System.exit(0);
				    }else{
				    	Toast.makeText(mContext, "增加北斗联系人失败!", Toast.LENGTH_SHORT).show();
				    }
			    }
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.add_contact, menu);
		return true;
	}

}
