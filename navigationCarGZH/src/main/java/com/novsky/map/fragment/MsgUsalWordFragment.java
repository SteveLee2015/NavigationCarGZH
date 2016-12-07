package com.novsky.map.fragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mapabc.android.activity.R;
import com.novsky.map.util.MessgeUsualOperation;


/**
 * 常用短语管理功能
 * @author steve
 */
public class MsgUsalWordFragment extends Fragment {

	private ListView listView = null;
	private SimpleAdapter adapter=null;
	private TextView noMsgWordPrompt=null;
	private List<Map<String, Object>> list=null;
	private Button addBtn,delBtn;
	private MessgeUsualOperation oper =null;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView=inflater.inflate(R.layout.activity_msg_usal_word, null);
		initUI(rootView);
		list = oper.getAll();
		if(list.size()==0){
			noMsgWordPrompt.setVisibility(View.VISIBLE);
		}
		adapter=new SimpleAdapter(getActivity(),list,R.layout.update_item_usal_word,
				new String[]{"MESSAGE_NUM","MESSAGE_WORD_TEXT"},
				new int[]{R.id.messge_word_id,R.id.messge_word_text});
		listView.setAdapter(adapter);
		listView.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int position, long arg3){
				Dialog dialog = new AlertDialog.Builder(getActivity())
		        .setTitle("修改常用短语")
				.setItems(new String[]{"修改短语","删除短语"},new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							final long rowId=Long.parseLong(String.valueOf(list.get(position).get("MESSAGE_WORD_ID")));
							final EditText editText=new EditText(getActivity());
							editText.setText(String.valueOf(list.get(position).get("MESSAGE_WORD_TEXT")));
							Dialog dialog1 = new AlertDialog.Builder(getActivity())
							        .setTitle("修改常用短语")
									.setView(editText)
									.setPositiveButton(
											getActivity().getResources().getString(R.string.common_save_str),
											new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog,
														int which) {
													boolean isTrue=oper.update(rowId, editText.getText().toString());
													if(isTrue){
														list.get(position).put("MESSAGE_WORD_TEXT", editText.getText().toString());
														Toast.makeText(getActivity(), "更新成功!", Toast.LENGTH_LONG).show();
														adapter.notifyDataSetChanged();
													}else{
														Toast.makeText(getActivity(), "更新失败!", Toast.LENGTH_LONG).show();
													}
													dialog.dismiss();
												}
									}).setNegativeButton(getActivity().getResources().getString(R.string.common_cancle_btn),
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
							AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
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
										Toast.makeText(getActivity(), "删除失败!", Toast.LENGTH_LONG).show();
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
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	

	@Override
	public void onDetach() {
		super.onDetach();
	}

	public void initUI(View view){
		oper = new MessgeUsualOperation(getActivity());
		listView = (ListView) view.findViewById(R.id.msg_word_listview);
		listView.setCacheColorHint(0);
		noMsgWordPrompt=(TextView)view.findViewById(R.id.no_msg_word_prompt);
		addBtn=(Button)view.findViewById(R.id.add_usaul_word_btn);
		delBtn=(Button)view.findViewById(R.id.del_usaul_word_btn);
		addBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				final EditText editText=new EditText(getActivity());
				Dialog dialog = new AlertDialog.Builder(getActivity()).setTitle("增加常用短语").setView(editText).
					    setPositiveButton("增加",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,int which){
										long rowId=oper.insert(editText.getText().toString());
										if(rowId>0){
											Toast.makeText(getActivity(), "常用短语增加成功!",Toast.LENGTH_SHORT).show();
										}else{
											Toast.makeText(getActivity(), "常用短语增加失败!",Toast.LENGTH_SHORT).show();
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
								getActivity().getResources().getString(
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
				AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
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
							Toast.makeText(getActivity(), "删除失败!", Toast.LENGTH_LONG).show();
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
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		if(oper!=null){
		   oper.close();
		}
	}

}
