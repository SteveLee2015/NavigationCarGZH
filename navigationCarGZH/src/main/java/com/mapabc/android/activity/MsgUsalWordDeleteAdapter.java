package com.mapabc.android.activity;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

/**
 * 友邻位置的Adapter
 * @author steve
 */
public class MsgUsalWordDeleteAdapter extends BaseAdapter {
	
	private ViewHolder viewHolder=null;
	private Context mContext=null;
	public List<Map<String,Object>> list=null;
	private LayoutInflater mInflater=null; 
	
	/**
	 * 构造方法
	 * @param mContext
	 * @param list
	 */
	public MsgUsalWordDeleteAdapter(Context mContext,List<Map<String,Object>> list){
		this.mContext=mContext;
		this.list=list;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View contentView, ViewGroup parent) {
		if(contentView==null){
			viewHolder=new ViewHolder();
			mInflater=LayoutInflater.from(mContext);
			contentView=mInflater.inflate(R.layout.item_message_delete_word, null);
			viewHolder.word_text=(TextView)contentView.findViewById(R.id.messge_word_text);
			viewHolder.chexkBox=(CheckBox)contentView.findViewById(R.id.all_select_checkbox);
			contentView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder)contentView.getTag();
		}
		if(list!=null){
			viewHolder.word_text.setText(String.valueOf(list.get(position).get("MESSAGE_WORD_TEXT")));
			final int index=position;
			viewHolder.chexkBox.setChecked(Boolean.valueOf(String.valueOf(list.get(position).get("MESSAGE_WORD_CHECKED"))));
		    viewHolder.chexkBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				      list.get(index).put("MESSAGE_WORD_CHECKED", arg1);
				}
		    });
		}
		return contentView;
	}
	
	public static class ViewHolder{
		TextView word_text;
		CheckBox chexkBox;
	}
}
