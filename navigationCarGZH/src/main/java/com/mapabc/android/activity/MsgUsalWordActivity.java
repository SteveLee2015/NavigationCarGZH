package com.mapabc.android.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.novsky.map.util.MessgeUsualOperation;

public class MsgUsalWordActivity extends Activity{

	private Context mContext = this;
	private ListView listView = null;
	private LinearLayout mLinerLayout = null;
    private MessgeUsualOperation oper=null;
	private MsgUsalWordAdapter adapter=null;
	private Button addBtn,delBtn;
	private TextView noMsgWordPrompt=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_msg_usal_word);
		listView = (ListView) this.findViewById(R.id.msg_word_listview);
		mLinerLayout = (LinearLayout) this.findViewById(R.id.msg_word_layout);
		noMsgWordPrompt=(TextView)this.findViewById(R.id.no_msg_word_prompt);
		/* 增加背景 */
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.bg);
		BitmapDrawable bd = new BitmapDrawable(bitmap);
		bd.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		bd.setDither(true);
		mLinerLayout.setBackgroundDrawable(bd);
		oper = new MessgeUsualOperation(this);
		final List<Map<String, Object>> list = oper.getAll();
		if(list.size()==0){
			noMsgWordPrompt.setVisibility(View.VISIBLE);
		}
		adapter = new MsgUsalWordAdapter(this, list);
		listView.setAdapter(adapter);
		listView.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int position, long arg3){
				Dialog dialog = new AlertDialog.Builder(mContext)
		        .setTitle("修改常用短语")
				.setItems(new String[]{"修改短语","删除短语"},new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							final long rowId=Long.parseLong(String.valueOf(list.get(position).get("MESSAGE_WORD_ID")));
							final EditText editText=new EditText(mContext);
							editText.setText(String.valueOf(list.get(position).get("MESSAGE_WORD_TEXT")));
							Dialog dialog1 = new AlertDialog.Builder(mContext)
							        .setTitle("修改常用短语")
									.setView(editText)
									.setPositiveButton(
											mContext.getResources().getString(R.string.common_save_str),
											new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog,
														int which) {
													boolean isTrue=oper.update(rowId, editText.getText().toString());
													if(isTrue){
														list.get(position).put("MESSAGE_WORD_TEXT", editText.getText().toString());
														Toast.makeText(mContext, "更新成功!", Toast.LENGTH_LONG).show();
														adapter.notifyDataSetChanged();
													}else{
														Toast.makeText(mContext, "更新失败!", Toast.LENGTH_LONG).show();
													}
													dialog.dismiss();
												}
									}).setNegativeButton(mContext.getResources().getString(R.string.common_cancle_btn),
											new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface arg0, int arg1) {
											arg0.dismiss();
										}
									}).create();
							dialog1.setCancelable(false);
							dialog1.show();
							break;
						case 1:
							AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
							builder.setTitle("提示");
							builder.setMessage("是否删除该条常用短语?");
							builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Map<String,Object> map=list.get(position);
									boolean istrue=oper.delete(Long.valueOf(String.valueOf(map.get("MESSAGE_WORD_ID"))));
									if(istrue){
									  list.remove(map);
									  adapter.notifyDataSetChanged();
									}else{
										Toast.makeText(mContext, "删除失败!", Toast.LENGTH_LONG).show();
									}
								}
							});
							builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									
								}
							});
							builder.create().show();
							break;
						default:
							break;
						}
					}
				}).create();
				dialog.show();
				return false;
			}
		});
		addBtn=(Button)this.findViewById(R.id.add_usaul_word_btn);
		delBtn=(Button)this.findViewById(R.id.del_usaul_word_btn);
		addBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				final EditText editText=new EditText(mContext);
				Dialog dialog = new AlertDialog.Builder(mContext).setTitle("增加常用短语").setView(editText).
					    setPositiveButton("增加",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,int which){
										long rowId=oper.insert(editText.getText().toString());
										if(rowId>0){
											Toast.makeText(mContext, "常用短语增加成功!",Toast.LENGTH_SHORT).show();
										}else{
											Toast.makeText(mContext, "常用短语增加失败!",Toast.LENGTH_SHORT).show();
										}
										Map<String,Object> map=new HashMap<String,Object>();
										map.put("MESSAGE_WORD_ID", rowId);
										map.put("MESSAGE_WORD_TEXT", editText.getText().toString());
										map.put("MESSAGE_WORD_CHECKED", editText.getText().toString());
										map.put("MESSAGE_NUM", list.size()+1);
										list.add(map);
										adapter.notifyDataSetChanged();
										noMsgWordPrompt.setVisibility(View.GONE);
									}
								})
						.setNegativeButton(
								mContext.getResources().getString(
										R.string.common_cancle_btn),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								}).create();
				dialog.show();
			}
		});
		
		delBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
				builder.setTitle("提示");
				builder.setMessage("是否删除全部常用短语?");
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						boolean istrue=oper.deleteAll();
						if(istrue){
							list.clear();
							adapter.notifyDataSetChanged();
						}else{
							Toast.makeText(mContext, "删除失败!", Toast.LENGTH_LONG).show();
						}
					}
				});
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				builder.create().show();
			}
		});
	}
}
