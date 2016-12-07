package com.novsky.map.main;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.novsky.map.util.DatabaseHelper;
import com.novsky.map.util.DatabaseHelper.InstructionNavColumns;
import com.novsky.map.util.Utils;

/**
 * 指令导航表操作类
 * @author steve
 */
public class BDInstructionNavOperation {
     
	public static final String KEY_TITLE = "title";
	public static final String KEY_BODY = "body";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_CREATED = "created";
	
	private DatabaseHelper databaseHelper;
	private Context context;
	private SQLiteDatabase sqliteDatabase;

	public BDInstructionNavOperation(Context mContext){
		this.context=mContext;
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase=databaseHelper.getReadableDatabase();
	}
	
	/**
	 * 增加北斗指令导航
	 * @param set
	 * @return
	 */
	public long insert(String lineId,String targetPointStr,String passPointStr,String evadePointStr){
		ContentValues contentValues=new ContentValues();
		contentValues.put(InstructionNavColumns.NAV_LINE_ID,lineId);
		contentValues.put(InstructionNavColumns.TARGET_POINT, targetPointStr);
		contentValues.put(InstructionNavColumns.PASS_POINT,passPointStr);
		contentValues.put(InstructionNavColumns.EVADE_POINT,evadePointStr);
		long id=sqliteDatabase.insert(InstructionNavColumns.TABLE_NAME, null, contentValues) ;
		return id;
	}

	/**
	 * 删除
	 * @param rowId
	 * @return
	 */
	public boolean delete(long rowId){
		boolean istrue=sqliteDatabase.delete(InstructionNavColumns.TABLE_NAME,KEY_ROWID +"="+rowId,null)>0;
		return istrue;
	}
	
	public boolean delete(String lineId){
		boolean istrue=sqliteDatabase.delete(InstructionNavColumns.TABLE_NAME,InstructionNavColumns.NAV_LINE_ID +"='"+lineId+"'",null)>0;
		return istrue;
	}
	
	/**
	 * 全部删除
	 * @param rowId
	 * @return
	 */
	public boolean delete(){
		boolean istrue=sqliteDatabase.delete(InstructionNavColumns.TABLE_NAME,null,null)>0;
		return istrue;
	}
	
	
	public BDInstructionNav get(long rowId) throws SQLException{
		//查询
		Cursor mCursor=sqliteDatabase.query(true,InstructionNavColumns.TABLE_NAME,
				new String[]{InstructionNavColumns._ID,InstructionNavColumns.NAV_LINE_ID ,InstructionNavColumns.TARGET_POINT,
				InstructionNavColumns.PASS_POINT,InstructionNavColumns.EVADE_POINT},KEY_ROWID + "=" + rowId, null, null,null,null,null);
		BDInstructionNav nav=new BDInstructionNav();   
		if(mCursor.moveToNext()){
			  nav.setRowId(mCursor.getLong(mCursor.getColumnIndex(InstructionNavColumns._ID)));
			  nav.setLineId(mCursor.getString(mCursor.getColumnIndex(InstructionNavColumns.NAV_LINE_ID)));
			  BDPoint targetPoint=new BDPoint();
			  String targetPointStr=mCursor.getString(mCursor.getColumnIndex(InstructionNavColumns.TARGET_POINT));
			  if(targetPointStr!=null&&!"".equals(targetPointStr)){
				  String temp[] =targetPointStr.split(",");
				  targetPoint.setLon(Utils.lonStr2Double(temp[0]));
				  targetPoint.setLonDirection(Utils.getLonDirection(temp[0]));
				  targetPoint.setLat(Utils.latStr2Double(temp[1]));
				  targetPoint.setLatDirection(Utils.getLatDirection(temp[1]));
			  }
			  nav.setTargetPoint(targetPoint);
			  String passPointStr=mCursor.getString(mCursor.getColumnIndex(InstructionNavColumns.PASS_POINT));
			  if(passPointStr!=null&&!"".equals(passPointStr)){
				  String temp[]=passPointStr.split(",");
				  List<BDPoint> passPoints=new ArrayList<BDPoint>();
				  for(int i=0;i<temp.length/2;i++){
					  BDPoint mBDPoint=new BDPoint();
					  mBDPoint.setLon(Utils.lonStr2Double(temp[i*2]));
					  mBDPoint.setLonDirection(Utils.getLonDirection(temp[i*2]));
					  mBDPoint.setLat(Utils.latStr2Double(temp[i*2+1]));
					  mBDPoint.setLatDirection(Utils.getLatDirection(temp[i*2+1]));
					  passPoints.add(mBDPoint);
				  }
				  nav.setPassPoints(passPoints);
			  }
			  String evadePointStr=mCursor.getString(mCursor.getColumnIndex(InstructionNavColumns.EVADE_POINT));
			  if(evadePointStr!=null&&!"".equals(evadePointStr)){
				  String temp[]=evadePointStr.split(",");
				  List<BDPoint> evadePoints=new ArrayList<BDPoint>();
				  for(int i=0;i<temp.length/2;i++){
					  BDPoint mBDPoint=new BDPoint();
					  mBDPoint.setLon(Utils.lonStr2Double(temp[i*2]));
					  mBDPoint.setLonDirection(Utils.getLonDirection(temp[i*2]));
					  mBDPoint.setLat(Utils.latStr2Double(temp[i*2+1]));
					  mBDPoint.setLatDirection(Utils.getLatDirection(temp[i*2+1]));
					  evadePoints.add(mBDPoint);
				  }
				  nav.setEvadePoints(evadePoints);
			  }
		}
		mCursor.close();
		return nav;
	}
	
	public List<BDInstructionNav> getAll() throws SQLException{
		Cursor mCursor=sqliteDatabase.query(true,InstructionNavColumns.TABLE_NAME,
				new String[]{InstructionNavColumns._ID,InstructionNavColumns.NAV_LINE_ID ,InstructionNavColumns.TARGET_POINT,
				InstructionNavColumns.PASS_POINT,InstructionNavColumns.EVADE_POINT},null, null, null,null,InstructionNavColumns._ID +" desc",null);
		
		List<BDInstructionNav> mInstructionNavs=new  ArrayList<BDInstructionNav>();
		
		while(mCursor.moveToNext()){
			  BDInstructionNav nav=new BDInstructionNav(); 
			  nav.setRowId(mCursor.getLong(mCursor.getColumnIndex(InstructionNavColumns._ID)));
			  nav.setLineId(mCursor.getString(mCursor.getColumnIndex(InstructionNavColumns.NAV_LINE_ID)));
			  BDPoint targetPoint=new BDPoint();
			  String targetPointStr=mCursor.getString(mCursor.getColumnIndex(InstructionNavColumns.TARGET_POINT));
			  if(targetPointStr!=null&&!"".equals(targetPointStr)){
				  String temp[] =targetPointStr.split(",");
				  targetPoint.setLon(Utils.lonStr2Double(temp[0]));
				  targetPoint.setLonDirection(Utils.getLonDirection(temp[0]));
				  targetPoint.setLat(Utils.latStr2Double(temp[1]));
				  targetPoint.setLatDirection(Utils.getLatDirection(temp[1]));
			  }
			  nav.setTargetPoint(targetPoint);
			  String passPointStr=mCursor.getString(mCursor.getColumnIndex(InstructionNavColumns.PASS_POINT));
			  if(passPointStr!=null&&!"".equals(passPointStr)){
				  String temp[]=passPointStr.split(",");
				  List<BDPoint> passPoints=new ArrayList<BDPoint>();
				  for(int i=0;i<temp.length/2;i++){
					  BDPoint mBDPoint=new BDPoint();
					  mBDPoint.setLon(Utils.lonStr2Double(temp[i*2]));
					  mBDPoint.setLonDirection(Utils.getLonDirection(temp[i*2]));
					  mBDPoint.setLat(Utils.latStr2Double(temp[i*2+1]));
					  mBDPoint.setLatDirection(Utils.getLatDirection(temp[i*2+1]));
					  passPoints.add(mBDPoint);
				  }
				  nav.setPassPoints(passPoints);
			  }
			  String evadePointStr=mCursor.getString(mCursor.getColumnIndex(InstructionNavColumns.EVADE_POINT));
			  if(evadePointStr!=null&&!"".equals(evadePointStr)){
				  String temp[]=evadePointStr.split(",");
				  List<BDPoint> evadePoints=new ArrayList<BDPoint>();
				  for(int i=0;i<temp.length/2;i++){
					  BDPoint mBDPoint=new BDPoint();
					  mBDPoint.setLon(Utils.lonStr2Double(temp[i*2]));
					  mBDPoint.setLonDirection(Utils.getLonDirection(temp[i*2]));
					  mBDPoint.setLat(Utils.latStr2Double(temp[i*2+1]));
					  mBDPoint.setLatDirection(Utils.getLatDirection(temp[i*2+1]));
					  evadePoints.add(mBDPoint);
				  }
				  nav.setEvadePoints(evadePoints);
			  }
			  mInstructionNavs.add(nav);
		}
		mCursor.close();
		return mInstructionNavs;
	}
	
	
	public int getSize() throws SQLException{
		//查询
		Cursor mCursor=sqliteDatabase.query(true,InstructionNavColumns.TABLE_NAME,
				new String[]{InstructionNavColumns._ID,InstructionNavColumns.NAV_LINE_ID ,InstructionNavColumns.TARGET_POINT,
				InstructionNavColumns.PASS_POINT,InstructionNavColumns.EVADE_POINT},null, null, null,null,null,null);
		return mCursor.getCount();
	}
	
	
	public void close(){
		databaseHelper.close();
		sqliteDatabase.close();
	}
}
