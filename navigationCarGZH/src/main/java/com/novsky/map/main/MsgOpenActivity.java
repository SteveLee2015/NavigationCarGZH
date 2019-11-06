package com.novsky.map.main;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapabc.android.activity.R;
import com.novsky.map.util.DatabaseHelper.CustomColumns;
import com.novsky.map.util.DatabaseOperation;

/**
 * 打开短信
 * @author steve
 */
public class MsgOpenActivity extends Activity {

	/*用户地址*/
	private EditText userAddress=null;
	
	/*发送短信内容*/
	private EditText msgContent=null;
	
	/*通讯录*/
	private ImageView addressBook=null;
	
	/*短信管理*/
	private ImageView manageMessage=null;
	
	/*发送短信按钮*/
	private ImageView  sendMsgBtn=null; 
	
	private TextView   bdMsgDate=null;
	
	/*布局*/
	private LinearLayout msgLayout=null;
	
	private Context mContext=this;
	
	private final int REQUEST_CONTACT = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_msg_open);
		Intent intent=getIntent();
		int index=Integer.valueOf(intent.getStringExtra("MSG_DEL_ID"));
		DatabaseOperation operation = DatabaseOperation.getInstance();
		Cursor cursor=operation.get(index);
		
		if(cursor.getCount()==0){
			return ;
		}
		userAddress=(EditText)this.findViewById(R.id.msg_user_address_edittxt);
	    msgContent=(EditText)this.findViewById(R.id.bd_msg_content_edittxt);
	    addressBook=(ImageView)this.findViewById(R.id.msg_user_address_book);
	    manageMessage=(ImageView)this.findViewById(R.id.msg_manager_imageview);
	    msgLayout=(LinearLayout)this.findViewById(R.id.bd_msg_send_layout);
	    sendMsgBtn=(ImageView)this.findViewById(R.id.bd_msg_send_btn);
	    bdMsgDate=(TextView)this.findViewById(R.id.bd_msg_date);
	    
	    addressBook.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//调用Android通讯录
				Intent intent = new Intent();
		        intent.setAction(Intent.ACTION_PICK);
		        intent.setData(ContactsContract.Contacts.CONTENT_URI);
		        startActivityForResult(intent, REQUEST_CONTACT);
			}
		});
	    
	    /*增加背景图片*/
	    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
		BitmapDrawable bd = new BitmapDrawable(bitmap);
		bd.setTileModeXY(TileMode.REPEAT , TileMode.REPEAT );
		bd.setDither(true);
		msgLayout.setBackgroundDrawable(bd); 
		String address="";
		String msg="";
		String time="";
		if(cursor.moveToFirst()){
			if(cursor.getString(cursor.getColumnIndex(CustomColumns.COLUMNS_SEND_ADDRESS))!=null){
				address=cursor.getString(cursor.getColumnIndex(CustomColumns.COLUMNS_SEND_ADDRESS));
			}
			if(cursor.getString(cursor.getColumnIndex(CustomColumns.COLUMNS_MSG_CONTENT))!=null){
				msg=cursor.getString(cursor.getColumnIndex(CustomColumns.COLUMNS_MSG_CONTENT));
			}
			if(cursor.getString(cursor.getColumnIndex(CustomColumns.COLUMNS_SEND_TIME))!=null){
				time=cursor.getString(cursor.getColumnIndex(CustomColumns.COLUMNS_SEND_TIME));
			}
		}
		cursor.close();
		userAddress.setText(address);
		msgContent.setText(msg);
		bdMsgDate.setText(time);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		 if (requestCode == REQUEST_CONTACT) {
             if (resultCode == RESULT_OK) {
                 if (data == null) {
                     return;
                 }    
                 Uri result = data.getData();
                 Cursor mCursor =this.getContentResolver().query(result, null, null,
 	            		null,null);
                 String name="";
                 String phoneNumber="";
 	            if (mCursor.moveToFirst()) {
 	                  name = mCursor.getString(mCursor.getColumnIndex(Phone.DISPLAY_NAME));
 	                  String contactId =mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts._ID));  
 	                  Cursor phones =getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,  
 	                        null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+contactId,  
 	                        null, null); 
 	                  
 	                  while (phones.moveToNext()) {  
 		                     phoneNumber+= phones.getString(phones.getColumnIndex(  
 		                          ContactsContract.CommonDataKinds.Phone.NUMBER));  
 	                  }  
 	                  phones.close();  
 	            }
 	            mCursor.close();
 	            userAddress.setText(name+"("+phoneNumber.replaceAll("-", "")+")");
 	            //Toast.makeText(mContext, name+"("+phoneNumber.replaceAll("-", "")+")",Toast.LENGTH_SHORT).show();
             }
         }
	}
	
	

}
