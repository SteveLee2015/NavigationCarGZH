package com.novsky.map.main;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.mapabc.android.activity.R;

/**
 * 短信的Adapter
 * @author steve
 */
public class SendMsgAdapter extends BaseAdapter {

	private ViewHolder viewHolder=null;
	private Context mContext=null;
	private LayoutInflater mInflater=null; 
	//private List<Map<String, Object>> list=null;
	
	public List<Item> items;
	public List<Item> toRemoveItems;
	
	/**
	 * 步骤2
	 */
	private OnCheckBoxClickLinstener onCheckBoxClickLinstener;
	
	/**
	 * 步骤3
	 * @param onCheckBoxClickLinstener
	 */
	public void setOnCheckBoxClickLinstener(
			OnCheckBoxClickLinstener onCheckBoxClickLinstener) {
		this.onCheckBoxClickLinstener = onCheckBoxClickLinstener;
	}

	/**
	 * 构造方法
	 * @param mContext
	 * @param list
	 */
	public SendMsgAdapter(Context mContext,List<Item> items){
          this.mContext=mContext;
          this.items=items;
          this.mInflater=LayoutInflater.from(mContext);
	}
	
	/**
	 * 获得总长度
	 */
	@Override
	public int getCount() {
		return items.size();
	}
     
	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}
    /**
     * 每个Item的View
     */
	@Override
	public View getView(final int position, View contentView, ViewGroup parent) {
		if(contentView==null){
				viewHolder=new ViewHolder();
				contentView=mInflater.inflate(R.layout.item_bdsend_msg, null);
				viewHolder.sendId=(TextView)contentView.findViewById(R.id.msg_send_id);
				viewHolder.content=(TextView)contentView.findViewById(R.id.msg_send_content);
				viewHolder.date=(TextView)contentView.findViewById(R.id.msg_send_date);
				viewHolder.flag=(ImageView)contentView.findViewById(R.id.msg_flag_icon);
				viewHolder.cbx_checked=(CheckBox)contentView.findViewById(R.id.cbx_checked); 
				contentView.setTag(viewHolder);
		}else{
				viewHolder=(ViewHolder)contentView.getTag();
		}
		viewHolder.sendId.setText(String.valueOf(items.get(position).send_id));
		viewHolder.content.setText(String.valueOf(items.get(position).send_content));
		viewHolder.date.setText(String.valueOf(items.get(position).send_date));
		viewHolder.flag.setBackgroundDrawable((Drawable)items.get(position).message_flag);
		
		viewHolder.cbx_checked.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				items.get(position).checked  =isChecked;
				
				//44444444调用一把
				if (onCheckBoxClickLinstener!=null) {
					onCheckBoxClickLinstener.onCheckBoxClicked(position,items);
				}
			}
		});
		
		viewHolder.cbx_checked.setChecked(items.get(position).checked);
		return contentView;
	}
	
	/**
	 * 删除List中所有的短信内容
	 */
	public void deleteAllMessageData(){
		if(items!=null){
		   items.clear();
		}
	}
	
	
	public static class ViewHolder{
	    /**
	     * 发送ID
	     */
		TextView sendId;
		/**
		 * 信息大小
		 */
	    TextView size;
		/**
		 * 内容
		 */
		TextView content;
		/**
		 * 日期
		 */
		TextView date;
		 /**
		   * 图标
		   */
		  ImageView  flag;
		  /**
		   * 向下的图标
		   * */
		  ImageView downIcon;
		  /**
		   * 转发短信
		   */
		  LinearLayout  zhuanFaMsg;
		 /**
		  * 删除短信
		  */
		  LinearLayout  delMsg;
		  /**
		   * 是否可见
		   */
		  LinearLayout  visibleOper;
		  
		  /**
		   * checkedbox
		   */
		  CheckBox cbx_checked;
	}
	
	
	/**
	 * 1 定义一个接口
	 * @author Administrator
	 *
	 */
	public interface OnCheckBoxClickLinstener {
		
		void onCheckBoxClicked(int positon,List<Item> items);
		
	}

}
