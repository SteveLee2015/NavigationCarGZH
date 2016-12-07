package com.novsky.map.main;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapabc.android.activity.R;

public class SoftAboutActivity extends Activity {

	private LinearLayout mLayout;
	
	private TextView   softVersion=null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//AgentApp.getInstance().addActivity(this);
		setContentView(R.layout.activity_soft_about);
		mLayout=(LinearLayout)this.findViewById(R.id.soft_about_layout);
		softVersion=(TextView)this.findViewById(R.id.soft_version_date);
		/*增加背景*/
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
		BitmapDrawable bd = new BitmapDrawable(bitmap);
		bd.setTileModeXY(TileMode.REPEAT , TileMode.REPEAT);
		bd.setDither(true);
		mLayout.setBackgroundDrawable(bd); 
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
}
