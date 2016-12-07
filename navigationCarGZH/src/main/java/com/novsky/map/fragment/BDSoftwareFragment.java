package com.novsky.map.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Fragment;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mapabc.android.activity.R;
import com.novsky.map.util.Utils;

/**
 * 软件 about 说明
 * @author Administrator
 *
 */
public class BDSoftwareFragment extends Fragment {

	private TextView versionTextView;
	private Button updateBtn;
	private TextView mapVersionTextView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view=inflater.inflate(R.layout.activity_bdsoftware,null);
		
		versionTextView=(TextView)view.findViewById(R.id.project_version_textview);
		versionTextView.setText("软件版本:"+getAppVersion(getActivity()));
		
		//地图版本
		mapVersionTextView=(TextView)view.findViewById(R.id.map_version_textview);
		mapVersionTextView.setText("地图版本:"+getMapVersion(getActivity()));
				
		updateBtn=(Button)view.findViewById(R.id.update_version_btn); 
		updateBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//检查当前外置SD卡中是否有升级包，如果有则进行升级
				File extfile=new File("/mnt/extsd");
				File[] list=extfile.listFiles();
				if(list!=null&&list.length>0){
					final List<String> names=new ArrayList<String>();
					for(File file:list){
					   if(!file.isDirectory()){
						   if(file.getName().startsWith("Navi")&&file.getName().endsWith(".apk")){
							   names.add(file.getName());
						   }
					   }
					}
					if(names.size()<=0){
						AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
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
						AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
						builder.setTitle("提示");
						builder.setMessage("是否把当前应用更新为"+names.get(0)+"?");
						builder.setPositiveButton("升级", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								String apkPath="/mnt/extsd/"+names.get(0);
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
						AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
						builder.setTitle("提示");
						builder.setMessage("外置SD卡中有多个升级包,请只保留一个！");
						builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
							}
						});
						builder.create().show();
					}
				}else{
					AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
					builder.setTitle("提示");
					builder.setMessage("请装载带有升级包的SD卡!");
					builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
						}
					});
					builder.create().show();
				}
			}
		});
		
		
		return view;
	}
	
	
	/**
	 * 获取地图版本信息
	 * io从SD卡读取配置文件  获取版本号
	 * 地图版本信息请放到MapAbc目录下的 version.properties 中
	 * MapAbc放到sd卡根目录
	 * @param bdSoftwareActivity
	 * @return
	 */
	private String getMapVersion(Context context) {
		// TODO 
		String readFile = Utils.readFile_ExtSDcard("version.properties");
		String value="";
		if (readFile!=null) {
			
			String[] split = readFile.split("\\=");
			String key = split[0];
			value = split[1];
		}
		
		return value;
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
