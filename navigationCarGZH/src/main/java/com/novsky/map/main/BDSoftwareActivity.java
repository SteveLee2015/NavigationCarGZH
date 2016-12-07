package com.novsky.map.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.mapabc.android.activity.R;

public class BDSoftwareActivity extends Activity {

	private TextView versionTextView;
	private Button updateBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bdsoftware);
		versionTextView=(TextView)this.findViewById(R.id.project_version_textview);
		versionTextView.setText("软件版本:"+getAppVersion(this));
		updateBtn=(Button)this.findViewById(R.id.update_version_btn); 
		updateBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//检查当前外置SD卡中是否有升级包，如果有则进行升级
				File extfile=new File("/mnt/sdcard");
				File[] list=extfile.listFiles();
				File inetfile=new File("/mnt/sdcard1");
				File[] inetfileList=inetfile.listFiles();
				if(list!=null&&list.length>0&&inetfileList!=null&&inetfileList.length>0){
					final List<String> names=new ArrayList<String>();
					for(File file:list){
					   if(!file.isDirectory()){
						   if(file.getName().startsWith("Navi")&&file.getName().endsWith(".apk")){
							   names.add(file.getName());
						   }
					   }
					}
					if(names.size()<=0){
						AlertDialog.Builder builder=new AlertDialog.Builder(BDSoftwareActivity.this);
						builder.setTitle("提示");
						builder.setMessage("请在外置SD卡中放置升级包!");
						builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
							}
						});
						builder.create().show();
					}else if(names.size()==1){
						AlertDialog.Builder builder=new AlertDialog.Builder(BDSoftwareActivity.this);
						builder.setTitle("提示");
						builder.setMessage("是否把当前应用更新为"+names.get(0)+"?");
						builder.setPositiveButton("升级", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								String apkPath="/mnt/sdcard/"+names.get(0);
								//String version1=getUninstallAPKInfo(BDSoftwareActivity.this,apkPath);
								//Toast.makeText(BDSoftwareActivity.this, ""+version1, Toast.LENGTH_SHORT).show();
								//调用升级流程进行升级
								Intent intent = new Intent(Intent.ACTION_VIEW); 
								intent.setDataAndType(Uri.fromFile(new File(apkPath)), "application/vnd.android.package-archive"); 
								startActivity(intent);
							}
						});
						builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
							}
						});
						builder.create().show();
					}else{
						AlertDialog.Builder builder=new AlertDialog.Builder(BDSoftwareActivity.this);
						builder.setTitle("提示");
						builder.setMessage("外置SD卡中有多个升级包,请只保留一个!");
						builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
							}
						});
						builder.create().show();
					}
				}else{
					AlertDialog.Builder builder=new AlertDialog.Builder(BDSoftwareActivity.this);
					builder.setTitle("提示");
					builder.setMessage("请安装带有升级包的外置SD卡!");
					builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1){
							arg0.dismiss();
						}
					});
					builder.create().show();
				}
			}
		});
	}


	public static String getAppVersion(Context mContext){
		String versionName="";
		try {
			PackageManager manager=mContext.getPackageManager();
			PackageInfo pi=manager.getPackageInfo(mContext.getPackageName(), 0);
			versionName=pi.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionName;
	}
	
	/**
	 * 获得未安装APK的版本
	 * @param ctx
	 * @param archiveFilePath
	 * @return
	 */
	private String getUninstallAPKInfo(Context ctx,String archiveFilePath) {  
	    String versionName = null;  
	    String appName = null;  
	    String pakName = null;  
	    PackageManager pm=ctx.getPackageManager();  
	    PackageInfo pakinfo=pm.getPackageArchiveInfo(archiveFilePath,PackageManager.GET_ACTIVITIES);  
	    if (pakinfo!=null) {  
	        ApplicationInfo appinfo=pakinfo.applicationInfo;  
	        versionName=pakinfo.versionName; 
	        Drawable icon=pm.getApplicationIcon(appinfo);  
	        appName=(String) pm.getApplicationLabel(appinfo);  
	        pakName=appinfo.packageName;  
	    }  
	    return versionName;  
	}  
}
