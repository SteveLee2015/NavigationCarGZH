package com.novsky.map.main;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.BDParameterException;
import android.location.BDRDSSManager;
import android.location.BDUnknownException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mapabc.android.activity.R;
import com.novsky.map.util.OnCustomListListener;
import com.novsky.map.util.Utils;

/**
 * 北斗通讯查询
 * 
 * @author steve
 */
public class BDComResearchActivity extends Activity implements
		OnTabActivityResultListener {

	private CustomListView condition = null;// 条件
	private LinearLayout bdcom_show_cond = null;
	private LinearLayout bdcom_research_layout = null;
	private ImageView myLinkerBtn = null; // 通讯录按钮
	private Button submitBtn = null;
	private EditText receivedAddress = null;
	private final int REQUEST_CONTACT = 1;
	private byte condition_type = 3;
	private Context mContext = this;
	private BDRDSSManager manager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bdcom_research);
		manager = BDRDSSManager.getDefault(this);
		myLinkerBtn = (ImageView) this.findViewById(R.id.bdcom_linker_btn); // 联系人
		receivedAddress = (EditText) this
				.findViewById(R.id.bdcom_received_address); // 接收地址
		condition = (CustomListView) this
				.findViewById(R.id.bdcom_select_control); // 查询条件
		condition.setData(mContext.getResources().getStringArray(
				R.array.msg_research_array));

		submitBtn = (Button) this.findViewById(R.id.submit_btn); // 确定
		bdcom_show_cond = (LinearLayout) this.findViewById(R.id.bdcom_control);
		bdcom_research_layout = (LinearLayout) this
				.findViewById(R.id.bdcom_research);
		condition.setOnCustomListener(new OnCustomListListener() {
			@Override
			public void onListIndex(int num) {
				if (num == 0) {
					condition_type = 3;
					bdcom_show_cond.setVisibility(View.VISIBLE);
				} else if (num == 1) {
					condition_type = 2;
					bdcom_show_cond.setVisibility(View.VISIBLE);
				} else if (num == 2) {
					condition_type = 1;
					bdcom_show_cond.setVisibility(View.GONE);
				}
			}
		});

		/* 联系人 */
		myLinkerBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_PICK);
				intent.setData(ContactsContract.Contacts.CONTENT_URI);
				BDComResearchActivity.this.getParent().startActivityForResult(
						intent, REQUEST_CONTACT);
			}
		});

		submitBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (Utils.getCardInfo().mSericeFeq == 9999) {
					Toast.makeText(
							mContext,
							mContext.getResources().getString(
									R.string.have_not_bd_sim),
							Toast.LENGTH_SHORT).show();
					return;
				}

				String address = receivedAddress.getText().toString();
				if (address == null || "".equals(address)) {
					Toast.makeText(
							mContext,
							mContext.getResources().getString(
									R.string.bd_address_no_content),
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (address.contains("(")) {
					address = address.substring(address.lastIndexOf("(") + 1,
							address.lastIndexOf(")"));
				}				
				try {
					manager.sendQueryReceiptCmdBDV21(1,condition_type,address);
				} catch (BDUnknownException e) {
					e.printStackTrace();
				} catch (BDParameterException e) {
					e.printStackTrace();
				}
			}
		});

	}

	@Override
	public void onTabActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CONTACT) {
			if (resultCode == RESULT_OK) {
				if (data == null) {
					return;
				}
				Uri result = data.getData();
				Cursor mCursor = this.getContentResolver().query(result, null,
						null, null, null);
				String name = "";
				String phoneNumber = "";
				if (mCursor.moveToFirst()) {
					name = mCursor.getString(mCursor
							.getColumnIndex(Phone.DISPLAY_NAME));
					String contactId = mCursor.getString(mCursor
							.getColumnIndex(ContactsContract.Contacts._ID));
					Cursor phones = getContentResolver().query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = " + contactId, null, null);

					while (phones.moveToNext()) {
						phoneNumber += phones
								.getString(phones
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					}
					phones.close();
				}
				mCursor.close();
				receivedAddress.setText(name + "("
						+ phoneNumber.replaceAll(" ", "") + ")");
			}
		}
	}
}
