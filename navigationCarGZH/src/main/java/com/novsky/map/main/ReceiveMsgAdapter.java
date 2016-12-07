package com.novsky.map.main;

import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapabc.android.activity.R;
/**
 * 短讯的Adapter
 * @author steve
 */
public class ReceiveMsgAdapter extends BaseAdapter {

	private ViewHolder viewHolder=null;
	private Context mContext=null;
	private LayoutInflater mInflater=null; 
	private List<Map<String, Object>> list=null;
	
	/**
	 * 构造方法
	 * @param mContext
	 * @param list
	 */
	public ReceiveMsgAdapter(Context mContext,List<Map<String,Object>> list,AlertDialog.Builder dialog){
          this.mContext=mContext;
          this.list=list;
          this.mInflater=LayoutInflater.from(mContext);
	}
	
	/**
	 * 获得总长度
	 */
	@Override
	public int getCount() {
		return list.size();
	}
     
	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
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
				contentView.setTag(viewHolder);
		}else{
				viewHolder=(ViewHolder)contentView.getTag();
		}
		viewHolder.sendId.setText(String.valueOf(list.get(position).get("SEND_ID")));
		viewHolder.content.setText(String.valueOf(list.get(position).get("SEND_CONTENT")));
		viewHolder.date.setText(String.valueOf(list.get(position).get("SEND_DATE")));
		viewHolder.flag.setBackgroundDrawable((Drawable)list.get(position).get("MESSAGE_FLAG"));
		return contentView;
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
	}

}
