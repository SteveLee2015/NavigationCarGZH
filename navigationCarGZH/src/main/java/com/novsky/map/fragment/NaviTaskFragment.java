package com.novsky.map.fragment;

import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bd.comm.protocal.BDCommManager;
import com.bd.comm.protocal.BDRNSSLocation;
import com.bd.comm.protocal.BDRNSSLocationListener;
import com.mapabc.android.activity.NaviStudioActivity;
import com.mapabc.android.activity.R;
import com.novsky.map.main.BDInstructionNav;
import com.novsky.map.main.BDInstructionNavOperation;

/**
 * 导航任务
 * @author steve
 */
public class NaviTaskFragment extends Fragment{
	/**
	 *日志标识
	 */
	private static final String TAG="NaviTaskFragment";
	/**
	 *导航任务的ListView对象
	 */
	private ListView listView=null;
	/**
	 *绑定在ListView组件的所有短信数据适配器对象  
	 */
	private NaviTaskAdapter adapter=null;
	
	
	private List<Map<String,Object>> list=null;
	/**
	 *当前没有指定导航信息
	 */
    private TextView noNavTastPrompt=null;
    
    /**
     * 获得定位数据
     */
	private BDRNSSLocation mRnssLocation=null;
	
	
	private List<BDInstructionNav> navs =null;
	/**
	 * 数据库操作对象
	 */
	private BDInstructionNavOperation operation=null;
	
    private long  newNavId=0l;
	
	
    private BDRNSSLocationListener mBDRNSSLocationListener=new BDRNSSLocationListener(){
		@Override
		public void onLocationChanged(BDRNSSLocation arg0) {
			mRnssLocation=arg0;
		}
		@Override
		public void onProviderDisabled(String arg0){}

		@Override
		public void onProviderEnabled(String arg0) {}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2){}
	};

	private BroadcastReceiver receiver=new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			long rowId=intent.getLongExtra("BDINSTRLINEID",0);
			BDInstructionNav navi=operation.get(rowId);
			newNavId=navi.getRowId();
			navs.add(0,navi);
			adapter.notifyDataSetChanged();
		}
	};
	
	private BDCommManager mBDCommManager=null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBDCommManager=BDCommManager.getInstance(getActivity().getApplicationContext());
		/*1.从数据库中查询所有的短信数据,如果数据库没有数据则发送指令请求最新插入的数据*/
		operation=new BDInstructionNavOperation(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
		View rootView=inflater.inflate(R.layout.nav_list_adpater, null);
		listView=(ListView)rootView.findViewById(R.id.nav_list_listview);
		listView.setCacheColorHint(0);
		noNavTastPrompt=(TextView)rootView.findViewById(R.id.nav_message_prompt);
		navs = operation.getAll();
		if(navs.size()>1000){
			AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
			builder.setTitle("提示");
			builder.setMessage("指令导航数量超过1000条,请删除不必要的指令导航信息!");
			builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
		}
        if(navs.size()>0){
			/*2.把数据转换成List*/
			adapter=new NaviTaskAdapter(getActivity(), navs);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,int position, long id){
					final BDInstructionNav bdInstructionNav=navs.get(position);
					AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
					builder.setTitle("查看指令导航信息");
					builder.setMessage("指令ID:"+bdInstructionNav.getLineId()+"\r\n"+"目标点:"+bdInstructionNav.getTargetPoint().getLat()+","+bdInstructionNav.getTargetPoint().getLon()+",\n途经点:"+bdInstructionNav.getPassPointsString()+",\n规避点:"+bdInstructionNav.getEvadePointsString());
					builder.setNegativeButton("取消",new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.create().show();
				}
			});
			listView.setOnItemLongClickListener(new OnItemLongClickListener(){
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,final int index, long arg3){
					AlertDialog.Builder alert=new AlertDialog.Builder(getActivity());
					alert.setTitle("删除短信");
					alert.setMessage("是否删除该条短信?");
					alert.setPositiveButton("删除", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int index1) {
							boolean istrue=operation.delete(navs.get(index).getRowId());
							if(istrue){
							  navs.remove(index);
							  adapter.notifyDataSetChanged();
						      Toast.makeText(getActivity(), "删除指令导航成功!", Toast.LENGTH_SHORT).show();  
							}else{
							   Toast.makeText(getActivity(),"删除指令导航失败!", Toast.LENGTH_SHORT).show();   
							}
						}
					});
					alert.setNeutralButton("全部删除", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							boolean istrue=operation.delete();
							if(istrue){
							   navs.clear();
							   adapter.notifyDataSetChanged();
							   Toast.makeText(getActivity(), "全部删除指令导航成功!", Toast.LENGTH_SHORT).show();
							}else{
							   Toast.makeText(getActivity(), "全部删除指令导航失败!", Toast.LENGTH_SHORT).show();
							}
						}
					});
					alert.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
						}
					});
					alert.show();
					return true;
				}
			});
		}else{
			noNavTastPrompt.setText("当前没有指令导航！");
			noNavTastPrompt.setVisibility(View.VISIBLE);
		}
		return rootView;
	}
	
	public void onResume() {
		super.onResume();
		try {
			mBDCommManager.addBDEventListener(mBDRNSSLocationListener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
		IntentFilter filter=new IntentFilter("com.bd.action.BD_INSTR_LINE_ACTION");
		getActivity().registerReceiver(receiver, filter);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(receiver);
		try {
			mBDCommManager.removeBDEventListener(mBDRNSSLocationListener);
		} catch (BDParameterException e) {
			e.printStackTrace();
		} catch (BDUnknownException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(operation!=null){
			operation.close();
		}
	}

	public class NaviTaskAdapter extends BaseAdapter {
		
		private ViewHolder viewHolder=null;
		private Context mContext=null;
		private LayoutInflater mInflater=null; 
		private List<BDInstructionNav> list=null;
	
		/**
		 * 构造方法
		 * @param mContext
		 * @param list
		 */
		public NaviTaskAdapter(Context mContext,List<BDInstructionNav> list){
	          this.mContext=mContext;
	          this.list=list;
	          this.mInflater=LayoutInflater.from(mContext); 
		}
		
		
		public int getCount() {
			return list.size();
		}
	     
		
		public Object getItem(int arg0) {
			return list.get(arg0);
		}

		
		public long getItemId(int arg0) {
			return 0;
		}
		
		public View getView(final int position, View contentView, ViewGroup parent) {
			if(contentView==null){
					viewHolder=new ViewHolder();
					contentView=mInflater.inflate(R.layout.item_navi_task_msg, null);
					viewHolder.sendId=(TextView)contentView.findViewById(R.id.msg_send_id);				
					viewHolder.content=(TextView)contentView.findViewById(R.id.msg_send_content);
					viewHolder.date=(TextView)contentView.findViewById(R.id.msg_send_date);
					viewHolder.naviBtn=(Button)contentView.findViewById(R.id.start_navi_btn);
					viewHolder.imageView=(ImageView)contentView.findViewById(R.id.navi_task_imageview);
					contentView.setTag(viewHolder);
			}else{
					viewHolder=(ViewHolder)contentView.getTag();
			}
			final BDInstructionNav nav=list.get(position);
			if(newNavId==nav.getRowId()){
				Bitmap bm=BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.message_not_read);
				viewHolder.imageView.setImageBitmap(bm);
			}else{
				Bitmap bm=BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.message_received_box);
				viewHolder.imageView.setImageBitmap(bm);
			}
			viewHolder.sendId.setText("指令ID:"+String.valueOf(nav.getLineId()));
			viewHolder.content.setText("目标点:"+nav.getTargetPoint().getLat()+","+nav.getTargetPoint().getLon()+",途经点:"+nav.getPassPointsString()+",规避点:"+nav.getEvadePointsString());
			viewHolder.naviBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent notificationIntent = new Intent(mContext,NaviStudioActivity.class);
					notificationIntent.putExtra("NAVI_ID",nav.getRowId());
					startActivity(notificationIntent);
				}
			});
			return contentView;
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
		 * 导航按钮
		 */
		Button naviBtn;
		
		ImageView imageView;
	}
}
