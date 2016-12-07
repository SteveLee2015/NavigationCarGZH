package com.novsky.map.main;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.mapabc.android.activity.BottomBaseActivity;
import com.mapabc.android.activity.R;
import com.novsky.map.fragment.FriendLocationTaskFragment;
import com.novsky.map.fragment.LineTaskFragment;
import com.novsky.map.fragment.MsgUsalWordFragment;
import com.novsky.map.fragment.NaviTaskFragment;
import com.novsky.map.fragment.SendMsgRequestFragment;
import com.novsky.map.fragment.SendedMsgFragment;

/**
 * 消息发送界面 横屏
 * @author Administrator
 *
 */
public class BDSendMsgLandScapeActivity extends BottomBaseActivity implements BDMessageListFragment.Callbacks{
	
	private static final String TAG="BDSendMsgLandScapeActivity";
	
	private Context mContext=this;
	/**
	 * Title的组件
	 */
	//private ImageView homeTitleImage = null;
	@Override
	protected int getContentView() {
		return R.layout.activity_bdsend_msg_land_scape;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setContentView(R.layout.activity_bdsend_msg_land_scape);
		title_name.setText("短信");
		//mReturnLayout=(LinearLayout)this.findViewById(R.id.bd_msg_return_layout);
		//返回键
		//homeTitleImage = (ImageView) this.findViewById(R.id.home_title_flag_img);
		back.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				BDSendMsgLandScapeActivity.this.finish();
//				JsNavi.mUi.mStaBar.doStatus(0);
			}
		});
	}

	@Override
	public void onItemSelected(Integer id) {
		if(id==1){
			//新建短信
			SendMsgRequestFragment fragment = new SendMsgRequestFragment();
			getFragmentManager().beginTransaction()
				.replace(R.id.message_detail_container, fragment)
				.commit();
		}else if(id==2){
			//短信列表
			SendedMsgFragment sendedMsgfragment = new SendedMsgFragment();
			getFragmentManager().beginTransaction()
				.replace(R.id.message_detail_container, sendedMsgfragment)
				.commit(); 
		}else if(id==3){
			//常用短信
			MsgUsalWordFragment  msgUsalWordFragment=new MsgUsalWordFragment();
			getFragmentManager().beginTransaction()
			.replace(R.id.message_detail_container, msgUsalWordFragment)
			.commit(); 
		}else if(id==4){
			//指令导航
			NaviTaskFragment naviTaskFragment=new NaviTaskFragment();
			getFragmentManager().beginTransaction()
			.replace(R.id.message_detail_container, naviTaskFragment)
			.commit();
		}else if(id==5){
			//路线导航
			LineTaskFragment listTaskFragment=new LineTaskFragment();
			getFragmentManager().beginTransaction()
			.replace(R.id.message_detail_container, listTaskFragment)
			.commit();
		}else if(id==6){
			//友邻位置
			FriendLocationTaskFragment friendLocationTaskFragment=new FriendLocationTaskFragment();
			getFragmentManager().beginTransaction()
			.replace(R.id.message_detail_container, friendLocationTaskFragment)
			.commit();
		}
	}
}
