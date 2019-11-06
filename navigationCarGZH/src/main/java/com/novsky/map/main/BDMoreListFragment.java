package com.novsky.map.main;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mapabc.android.activity.R;
import com.novsky.map.util.Utils;

import java.util.ArrayList;
import java.util.List;


public class BDMoreListFragment extends ListFragment{
	
	private Callbacks mCallbacks;
	private List<MoreItem> items=new ArrayList<MoreItem>();
    private FragmengAdapter fragmengAdapter=null;
	public interface Callbacks{
		public void onItemSelected(Integer id);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addItem(1,"北斗Ⅰ 定位",false,R.drawable.rdss_location);
		addItem(2,"定位设置",false,R.drawable.rdss_location_setting);
		addItem(3,"北斗Ⅱ定位",false,R.drawable.system_bd2_status);
		addItem(4,"位置报告",false,R.drawable.location_report);
		//addItem(5,"友邻信息",false,R.drawable.system_warnning_set);
		addItem(5,"SOS参数设置",false,R.drawable.system_warnning_set);
		addItem(6,"本机信息",false,R.drawable.system_serial_set);
		addItem(7,"北斗Ⅰ 信号",false,R.drawable.system_bs_staus);
		addItem(8,"北斗Ⅱ信号",false,R.drawable.system_bd2_status);
		addItem(9,"GPS信号",false,R.drawable.system_gps_status);
		addItem(10,"北斗校时",false,R.drawable.location_report);
		//addItem(11,"作战时间",false,R.drawable.location_report);
		//addItem(12,"管理信息",false,R.drawable.system_manager_set);
		//addItem(13,"定位模式",false,R.drawable.system_local_set);
		//addItem(14,"中继站管理",false,R.drawable.system_cood_translate);
		//addItem(17 ,"手机卡信息" , false ,R.drawable.message_4g_card);
		//addItem(18 , "4G短信" , false ,R.drawable.message_4g);
		addItem(11,"关于",false,R.drawable.system_distance_calc);
		//addItem(16,"超速报告",false,R.drawable.system_warnning_set);
		fragmengAdapter=new FragmengAdapter(getActivity(), items);
		setListAdapter(fragmengAdapter);
	}

	
	public class FragmengAdapter extends BaseAdapter{

		private Context mContext=null;
		
		private List<MoreItem> list=null;
		
		private ViewHolder viewHolder=null;
		
		public FragmengAdapter(Context mContext,List<MoreItem> list){
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
				contentView=mInflater.inflate(R.layout.bd_more_list, null);
				viewHolder.itemLayout=(LinearLayout)contentView.findViewById(R.id.list_item_layout);
				viewHolder.name=(TextView)contentView.findViewById(R.id.list_item_textview);
				viewHolder.logoImage=(ImageView)contentView.findViewById(R.id.item_logo);
				contentView.setTag(viewHolder);
			}else{
				viewHolder=(ViewHolder)contentView.getTag();
			}
			if(list!=null){
				MoreItem item=list.get(position);
				if(item.isCheck()){
					viewHolder.itemLayout.setBackgroundColor(mContext.getResources().getColor(R.color.msg_btn_bg_color));
					viewHolder.name.setTextColor(Color.WHITE);
				}else{
					viewHolder.name.setTextColor(Color.BLACK);
					viewHolder.itemLayout.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
				}
				viewHolder.name.setText(item.getName());
				Bitmap bitmap=BitmapFactory.decodeResource(getActivity().getResources(), item.getImage());
				viewHolder.logoImage.setImageBitmap(bitmap);
			}
			
			return contentView;
		}
	}
	
	public void addItem(int id,String name,boolean isCheck,int image){
		/*组建数据*/
		MoreItem newItem=new MoreItem();
		newItem.setId(id);
		newItem.setName(name);
		newItem.setCheck(isCheck);
		newItem.setImage(image);
		items.add(newItem);
	}
	
	public  class ViewHolder{
		LinearLayout itemLayout;
		TextView name;
		ImageView logoImage;
	}
	
	
	@Override
	public void onStart() {
		super.onStart();
		for(int i=0;i<items.size();i++){
			if(i==Utils.BD_MANAGER_PAGER_INDEX-1){
			  items.get(i).setCheck(true);	
			}else{
			  items.get(i).setCheck(false);
			}
		}
		fragmengAdapter.notifyDataSetChanged();
		mCallbacks.onItemSelected(Utils.BD_MANAGER_PAGER_INDEX);
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
			MoreItem item=items.get(i);
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

	public class MoreItem{
		
		public int id;
		
		public String name;
		
		public boolean check;
		
		private int image;
		
		public int getImage() {
			return image;
		}

		public void setImage(int image) {
			this.image = image;
		}

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
