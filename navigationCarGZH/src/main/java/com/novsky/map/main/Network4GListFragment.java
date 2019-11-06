package com.novsky.map.main;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mapabc.android.activity.R;
import com.novsky.map.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class Network4GListFragment extends ListFragment{
	
	private Callbacks mCallbacks;
	private List<MessageItem> items=new ArrayList<MessageItem>();
    private FragmengAdapter fragmengAdapter=null;
    
	
	public interface Callbacks
	{
		public void onItemSelected(Integer id);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		/*组建数据*/
		MessageItem newMessageItem=new MessageItem();
		newMessageItem.setId(1);
		newMessageItem.setName("新建短信");
		newMessageItem.setCheck(true);
		items.add(newMessageItem);
		
		MessageItem listMessageItem=new MessageItem();
		listMessageItem.setId(2);
		listMessageItem.setName("短信列表");
		listMessageItem.setCheck(false);
		items.add(listMessageItem);
		
		MessageItem usalWordItem=new MessageItem();
		usalWordItem.setId(3);
		usalWordItem.setName("常用短语");
		usalWordItem.setCheck(false);
		items.add(usalWordItem);
		
/*		MessageItem naviItem=new MessageItem();
		naviItem.setId(4);
		naviItem.setName("指令导航");
		naviItem.setCheck(false);
		items.add(naviItem);*/
		
/*		MessageItem lineItem=new MessageItem();
		lineItem.setId(5);
		lineItem.setName("路线导航");
		lineItem.setCheck(false);
		items.add(lineItem);*/
		
		/*MessageItem friendLocationItem=new MessageItem();
		friendLocationItem.setId(6);
		friendLocationItem.setName("友邻位置");
		friendLocationItem.setCheck(false);
		items.add(friendLocationItem);
		*/
		fragmengAdapter=new FragmengAdapter(getActivity(), items);
		setListAdapter(fragmengAdapter);
	}
	
	public class FragmengAdapter extends BaseAdapter{

		private Context mContext=null;
		
		private List<MessageItem> list=null;
		
		private ViewHolder viewHolder=null;
		
		public FragmengAdapter(Context mContext,List<MessageItem> list){
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
			return 0;
		}

		@Override
		public View getView(int position, View contentView, ViewGroup arg2) {
			if(contentView==null){
				viewHolder=new ViewHolder();
				LayoutInflater mInflater=LayoutInflater.from(mContext);
				contentView=mInflater.inflate(R.layout.bd_message_list, null);
				viewHolder.itemLayout=(LinearLayout)contentView.findViewById(R.id.list_item_layout);
				viewHolder.name=(TextView)contentView.findViewById(R.id.list_item_textview);
				contentView.setTag(viewHolder);
			}else{
				viewHolder=(ViewHolder)contentView.getTag();
			}
			if(list!=null){
				MessageItem item=list.get(position);
				if(item.isCheck()){
					viewHolder.itemLayout.setBackgroundColor(mContext.getResources().getColor(R.color.msg_btn_bg_color));
					viewHolder.name.setTextColor(Color.WHITE);
				}else{
					viewHolder.name.setTextColor(Color.BLACK);
					viewHolder.itemLayout.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
				}
				viewHolder.name.setText(item.getName());
			}
			
			return contentView;
		}
	}
	
	public  class ViewHolder{
		LinearLayout itemLayout;
		TextView name;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		for(int i=0;i<items.size();i++){
			if(i==Utils.BD_MESSAGE_PAGER_INDEX-1){
			  items.get(i).setCheck(true);	
			}else{
			  items.get(i).setCheck(false);
			}
		}
		fragmengAdapter.notifyDataSetChanged();
		mCallbacks.onItemSelected(Utils.BD_MESSAGE_PAGER_INDEX);
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		if (!(activity instanceof Callbacks))
		{
			throw new IllegalStateException(
				"BDMessageListFragment Callbacks异常");
		}
		mCallbacks = (Callbacks)activity;
	}
	
	@Override
	public void onDetach()
	{
		super.onDetach();
		mCallbacks = null;
	}
	
	@Override
	public void onListItemClick(ListView listView
		, View view, int position, long id){
		super.onListItemClick(listView, view, position, id);
		for(int i=0;i<items.size();i++){
			MessageItem item=items.get(i);
			if(i==position){
				item.setCheck(true);
			}else{
				item.setCheck(false);
			}
		}
		fragmengAdapter.notifyDataSetChanged();
		mCallbacks.onItemSelected(items.get(position).id);
	}

	public void setActivateOnItemClick(boolean activateOnItemClick){
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	public class MessageItem{
		public int id;
		
		public String name;
		
		public boolean check;

		public boolean isCheck() {
			return check;
		}

		public void setCheck(boolean check) {
			this.check = check;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}

}
