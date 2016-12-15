package com.novsky.map.main;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.novsky.map.util.DatabaseHelper;
import com.novsky.map.util.DatabaseHelper.LineNavColumns;
import com.novsky.map.util.Utils;

/**
 * 指令导航表操作类
 * @author steve
 */
public class BDLineNavOperation {
     
	public static final String KEY_TITLE = "title";
	public static final String KEY_BODY = "body";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_CREATED = "created";
	
	private DatabaseHelper databaseHelper;
	private Context context;
	private SQLiteDatabase sqliteDatabase;

	public BDLineNavOperation(Context mContext){
		this.context=mContext;
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase=databaseHelper.getReadableDatabase();
	}
	
	/**
	 * 增加北斗路线导航
	 * @param set
	 * @return
	 */
	public long insert(String lineId,String currentIndex,String totalNum,String passPointStr){
		ContentValues contentValues=new ContentValues();
		contentValues.put(LineNavColumns.NAV_LINE_ID,lineId);
		contentValues.put(LineNavColumns.CURRENT_INDEX,currentIndex);
		contentValues.put(LineNavColumns.TOTAL_NUMBER,totalNum);
		contentValues.put(LineNavColumns.PASS_POINT,passPointStr);
		long id=sqliteDatabase.insert(LineNavColumns.TABLE_NAME, null, contentValues) ;
		return id;
	}

	/**
	 * 增加北斗路线导航
	 * @param set
	 * @return
	 */
	public long update(String lineId,String currentIndex,String totalNum,String passPointStr){
		//先删除 上次数据
		delete(lineId);
		ContentValues contentValues=new ContentValues();
		contentValues.put(LineNavColumns.NAV_LINE_ID,lineId);
		contentValues.put(LineNavColumns.CURRENT_INDEX,currentIndex);
		contentValues.put(LineNavColumns.TOTAL_NUMBER,totalNum);
		contentValues.put(LineNavColumns.PASS_POINT,passPointStr);
		long id=sqliteDatabase.insert(LineNavColumns.TABLE_NAME, null, contentValues) ;
		return id;
	}



	/**
	 * 删除
	 * @param rowId
	 * @return
	 */
	public boolean delete(long rowId){
		boolean istrue=sqliteDatabase.delete(LineNavColumns.TABLE_NAME,KEY_ROWID +"="+rowId,null)>0;
		return istrue;
	}
	
	public boolean delete(String lineId){
		boolean istrue=sqliteDatabase.delete(LineNavColumns.TABLE_NAME,LineNavColumns.NAV_LINE_ID +"='"+lineId+"'",null)>0;
		return istrue;
	}
	
	/**
	 * 全部删除
	 * @param rowId
	 * @return
	 */
	public boolean delete(){
		boolean istrue=sqliteDatabase.delete(LineNavColumns.TABLE_NAME,null,null)>0;
		return istrue;
	}
	
	/**
	 * 判断当前lineID的线路是否接收完
	 * @return
	 */
	public boolean checkLineNavComplete(String lineID){
		Cursor mCursor=sqliteDatabase.query(true,LineNavColumns.TABLE_NAME,
				new String[]{LineNavColumns._ID,LineNavColumns.NAV_LINE_ID ,LineNavColumns.CURRENT_INDEX,
				LineNavColumns.TOTAL_NUMBER,LineNavColumns.PASS_POINT},LineNavColumns.NAV_LINE_ID  + "='"+lineID+"'", null, null,null,null,null);
		boolean isture=false;
		while(mCursor.moveToNext()){
			String index=mCursor.getString(mCursor.getColumnIndex(LineNavColumns.CURRENT_INDEX));
			String total=mCursor.getString(mCursor.getColumnIndex(LineNavColumns.TOTAL_NUMBER));
			//由于序列号是从0开始的.
			if(Integer.valueOf(index).intValue()+1==Integer.valueOf(total).intValue()){
				isture=true;
			}
		}
		mCursor.close();
		return isture;
	}
	
	/**
	 * 1.首先根据lineID进行分组
	 * @param rowId
	 * @return
	 * @throws SQLException
	 */
	public BDLineNav  get(String lineID) throws SQLException{
		//查询
		Cursor mCursor=sqliteDatabase.query(true,LineNavColumns.TABLE_NAME,
				new String[]{LineNavColumns._ID,LineNavColumns.NAV_LINE_ID ,LineNavColumns.CURRENT_INDEX,
				LineNavColumns.TOTAL_NUMBER,LineNavColumns.PASS_POINT},LineNavColumns.NAV_LINE_ID  + "='"+lineID+"'", null, null,null,null,null);
		BDLineNav mBDLineNav=new BDLineNav();
		mBDLineNav.setLineId(lineID);
		List<BDPoint> passPoints=new ArrayList<BDPoint>();
		while(mCursor.moveToNext()){
			  String passPointStr=mCursor.getString(mCursor.getColumnIndex(LineNavColumns.PASS_POINT));
			  if(passPointStr!=null&&!"".equals(passPointStr)){
				  String temp[]=passPointStr.split(",");
				  for(int i=0;i<temp.length/2;i++){
					  BDPoint mBDPoint=new BDPoint();
					  mBDPoint.setLon(Utils.lonStr2Double(temp[i*2]));
					  mBDPoint.setLonDirection(Utils.getLonDirection(temp[i*2]));
					  mBDPoint.setLat(Utils.latStr2Double(temp[i*2+1]));
					  mBDPoint.setLatDirection(Utils.getLatDirection(temp[i*2+1]));
					  passPoints.add(mBDPoint);
				  }
			  }
		}
		mBDLineNav.setPassPoints(passPoints);
		mCursor.close();
		return mBDLineNav;
	}


	/**
	 * 获取当前条序号
	 * @param lineID
	 * @return
	 * @throws SQLException
     */
	public String  getCurrentLineNum(String lineID) throws SQLException{
		//查询
		Cursor mCursor=sqliteDatabase.query(true,LineNavColumns.TABLE_NAME,
				new String[]{LineNavColumns._ID,LineNavColumns.NAV_LINE_ID ,LineNavColumns.CURRENT_INDEX,
						LineNavColumns.TOTAL_NUMBER,LineNavColumns.PASS_POINT},LineNavColumns.NAV_LINE_ID  + "='"+lineID+"'", null, null,null,null,null);
		String currentLineNum = null;
		while(mCursor.moveToNext()){
			currentLineNum=mCursor.getString(mCursor.getColumnIndex(LineNavColumns.CURRENT_INDEX));
		}
		mCursor.close();
		return currentLineNum;
	}

	/**
	 * 获取所有的passPoint
	 * @param lineID
	 * @return
	 * @throws SQLException
	 */
	public List<String> getPassPointStr(String lineID) throws SQLException{
		//查询
		Cursor mCursor=sqliteDatabase.query(true,LineNavColumns.TABLE_NAME,
				new String[]{LineNavColumns._ID,LineNavColumns.NAV_LINE_ID ,LineNavColumns.CURRENT_INDEX,
						LineNavColumns.TOTAL_NUMBER,LineNavColumns.PASS_POINT},LineNavColumns.NAV_LINE_ID  + "='"+lineID+"'", null, null,null,null,null);
		String passPointStr = null;
		List<String> passPoints=new ArrayList<>();
		while(mCursor.moveToNext()){
			 passPointStr=mCursor.getString(mCursor.getColumnIndex(LineNavColumns.PASS_POINT));

			if(passPointStr!=null&&!"".equals(passPointStr)){
				String temp[]=passPointStr.split(",");
				for(int i=0;i<temp.length;i++){

					String mBDPoint = temp[i];
					passPoints.add(mBDPoint);
				}
			}
		}
		mCursor.close();
		return passPoints;
	}



	/**
	 * @param rowId
	 * @return
	 * @throws SQLException
	 */
	public List<BDLineNav>  getNavLineList() throws SQLException{
		//查询
		Cursor mCursor=sqliteDatabase.query(true,LineNavColumns.TABLE_NAME,
				new String[]{LineNavColumns._ID,LineNavColumns.NAV_LINE_ID ,LineNavColumns.CURRENT_INDEX,
				LineNavColumns.TOTAL_NUMBER,LineNavColumns.PASS_POINT},null, null,LineNavColumns.NAV_LINE_ID,null,null,null);
		
		List<BDLineNav> navList=new ArrayList<BDLineNav>();
		List<BDPoint> passPoints=new ArrayList<BDPoint>();
		BDLineNav mBDLineNav=new BDLineNav();
		
		while(mCursor.moveToNext()){
			 /**
			  * 路线ID
			  */
			 String lineID=mCursor.getString(mCursor.getColumnIndex(LineNavColumns.NAV_LINE_ID));
			 mBDLineNav.setLineId(lineID);
			 /**
			  * 当前索引
			  */
			 int currentIndex=mCursor.getInt(mCursor.getColumnIndex(LineNavColumns.CURRENT_INDEX));
			 /**
			  * 总数目
			  */
			 int totalCount=mCursor.getInt(mCursor.getColumnIndex(LineNavColumns.TOTAL_NUMBER));
			 /**
			  * 获得所有的必经点
			  */
			 String passPointStr=mCursor.getString(mCursor.getColumnIndex(LineNavColumns.PASS_POINT));
			 if(passPointStr!=null&&!"".equals(passPointStr)){
				  String temp[]=passPointStr.split(",");
				  for(int i=0;i<temp.length/2;i++){
					  BDPoint mBDPoint=new BDPoint();
					  mBDPoint.setLon(Utils.lonStr2Double(temp[i*2]));
					  mBDPoint.setLonDirection(Utils.getLonDirection(temp[i*2]));
					  mBDPoint.setLat(Utils.latStr2Double(temp[i*2+1]));
					  mBDPoint.setLatDirection(Utils.getLatDirection(temp[i*2+1]));
					  passPoints.add(mBDPoint);
				  }
			 }
			 if((currentIndex+1)==totalCount){
				 //标识当前是最后一条
				 //输入该条语句
				 mBDLineNav.setPassPoints(passPoints);
				 navList.add(mBDLineNav);
				 //重新创建对象
				 passPoints=new ArrayList<BDPoint>();
				 mBDLineNav=new BDLineNav();
			 }else {
				 //标识当前不是最后一条
				 //添加
				 //passPoints.add(mBDPoint);

			 }
		}
		mCursor.close();
		return navList;
	}
	
	public int getSize() throws SQLException{
		//查询
		Cursor mCursor=sqliteDatabase.query(true,LineNavColumns.TABLE_NAME,
				new String[]{LineNavColumns._ID,LineNavColumns.NAV_LINE_ID ,LineNavColumns.CURRENT_INDEX,
				LineNavColumns.TOTAL_NUMBER,LineNavColumns.PASS_POINT},null, null, null,null,null,null);
		return mCursor.getCount();
	}
	
	
	public void close(){
		databaseHelper.close();
		sqliteDatabase.close();
	}
}
