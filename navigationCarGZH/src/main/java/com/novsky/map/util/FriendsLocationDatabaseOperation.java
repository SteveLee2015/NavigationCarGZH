package com.novsky.map.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.novsky.map.main.FriendLocation;
import com.novsky.map.util.DatabaseHelper.FriendsLocationColumns;

public class FriendsLocationDatabaseOperation {
     
	public static final String KEY_TITLE = "title";
	public static final String KEY_BODY = "body";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_CREATED = "created";
	
	private DatabaseHelper databaseHelper;
	private Context context;
	private SQLiteDatabase sqliteDatabase;

	public FriendsLocationDatabaseOperation(Context mContext){
		this.context=mContext;
	}
	
	/**
	 * 增加友邻位置
	 * @param set
	 * @return
	 */
	public boolean insert(FriendsLocation flag){
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase = databaseHelper.getWritableDatabase();
		ContentValues contentValues=new ContentValues();
		contentValues.put(FriendsLocationColumns.FRIENDS_ID,flag.getUserId());
		contentValues.put(FriendsLocationColumns.REPORT_TIME, flag.getReportTime());
		contentValues.put(FriendsLocationColumns.FRIENDS_LON,flag.getLon());
		contentValues.put(FriendsLocationColumns.FRIENDS_LAT,flag.getLat());
		contentValues.put(FriendsLocationColumns.FRIENDS_HEIGHT,flag.getHeight());
		long id=sqliteDatabase.insert(FriendsLocationColumns.TABLE_NAME, null, contentValues) ;
		return id>0;
	}

	/**
	 * 删除
	 * @param rowId
	 * @return
	 */
	public boolean delete(long rowId){
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase = databaseHelper.getWritableDatabase();
		boolean istrue=sqliteDatabase.delete(FriendsLocationColumns.TABLE_NAME,KEY_ROWID +"="+rowId,null)>0;
		return istrue;
	}
	/**
	 * 删除
	 * @param rowId
	 * @return
	 */
	public boolean deleteAll(){
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase = databaseHelper.getWritableDatabase();
		int count = sqliteDatabase.delete(FriendsLocationColumns.TABLE_NAME, null, null);
		boolean istrue=count>0;
		return istrue;
	}


	public Cursor get(long rowId) throws SQLException{
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase=databaseHelper.getReadableDatabase();
		//查询
		Cursor mCursor=sqliteDatabase.query(true,FriendsLocationColumns.TABLE_NAME,
				new String[]{FriendsLocationColumns._ID,FriendsLocationColumns.FRIENDS_ID ,FriendsLocationColumns.REPORT_TIME,
				             FriendsLocationColumns.FRIENDS_LON,FriendsLocationColumns.FRIENDS_LAT,
				             FriendsLocationColumns.FRIENDS_HEIGHT},KEY_ROWID + "=" + rowId, null, null,null,null,null);
		return mCursor;
	}
	/**
	 * 得到数据  同一用户连续发多条信息的时候,只显示最新的位置信息
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String ,Object>> getAllLastLocationList() throws SQLException{
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase=databaseHelper.getReadableDatabase();
		//查询
		Cursor mCursor=sqliteDatabase.query(true,FriendsLocationColumns.TABLE_NAME,
				new String[]{FriendsLocationColumns._ID,FriendsLocationColumns.FRIENDS_ID ,FriendsLocationColumns.REPORT_TIME,
	             FriendsLocationColumns.FRIENDS_LON,FriendsLocationColumns.FRIENDS_LAT,
	             FriendsLocationColumns.FRIENDS_HEIGHT},null, null, null,null,FriendsLocationColumns._ID+" desc",null);
		List<Map<String ,Object>> list=new ArrayList<Map<String,Object>>();
		
		Map<String,String> userAddressMap=new HashMap<String,String>();
		while(mCursor.moveToNext()){
			String userAddress=mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns.FRIENDS_ID));
			//判断是否包含  统一个友邻只保存最新的位置
			if(!userAddressMap.containsKey(userAddress)){
				Map<String,Object> map=new HashMap<String,Object>();
				map.put("F_ID",mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns._ID)) );
				map.put("FRIEND_ID",userAddress);
				map.put("FRIEND_REPORT_TIME", mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns.REPORT_TIME)));
				map.put("FRIEND_LON", mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns.FRIENDS_LON)));
				map.put("FRIEND_LAT", mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns.FRIENDS_LAT)));
				map.put("FRIEND_HEIGHT", mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns.FRIENDS_HEIGHT)));
				list.add(map); 
				userAddressMap.put(userAddress, "exists");
			}
	    }
		mCursor.close();
		return list;
	}
	
	
	/**
	 * 得到数据  同一用户连续发多条信息的时候,只显示最新的位置信息
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String ,Object>> getAllLocationList() throws SQLException{
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase=databaseHelper.getReadableDatabase();
		//查询
		Cursor mCursor=sqliteDatabase.query(true,FriendsLocationColumns.TABLE_NAME,
				new String[]{
				FriendsLocationColumns._ID,
				FriendsLocationColumns.FRIENDS_ID ,
				FriendsLocationColumns.REPORT_TIME,
	             FriendsLocationColumns.FRIENDS_LON,
	             FriendsLocationColumns.FRIENDS_LAT,
	             FriendsLocationColumns.FRIENDS_HEIGHT},null, null, null,null,FriendsLocationColumns._ID+" desc",null);
		List<Map<String ,Object>> list=new ArrayList<Map<String,Object>>();
		
		Map<String,String> userAddressMap=new HashMap<String,String>();
		while(mCursor.moveToNext()){
			String userAddress=mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns.FRIENDS_ID));
			//判断是否包含  统一个友邻只保存最新的位置
			if(true){
				Map<String,Object> map=new HashMap<String,Object>();
				map.put("F_ID",mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns._ID)) );
				map.put("FRIEND_ID",userAddress);
				map.put("FRIEND_REPORT_TIME", mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns.REPORT_TIME)));
				map.put("FRIEND_LON", mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns.FRIENDS_LON)));
				map.put("FRIEND_LAT", mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns.FRIENDS_LAT)));
				map.put("FRIEND_HEIGHT", mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns.FRIENDS_HEIGHT)));
				list.add(map); 
				userAddressMap.put(userAddress, "exists");
			}
	    }
		mCursor.close();
		return list;
	}
//	/**
//	 * 得到所有用户最新的一条数据
//	 * @return
//	 * @throws SQLException
//	 */
//	public List<Map<String ,Object>> getAllUserLastNewData() throws SQLException{
//		databaseHelper=new DatabaseHelper(context);
//		sqliteDatabase=databaseHelper.getReadableDatabase();
//		//查询
//		Cursor mCursor=sqliteDatabase.query(true,FriendsLocationColumns.TABLE_NAME,
//				new String[]{FriendsLocationColumns._ID,FriendsLocationColumns.FRIENDS_ID ,FriendsLocationColumns.REPORT_TIME,
//	             FriendsLocationColumns.FRIENDS_LON,FriendsLocationColumns.FRIENDS_LAT,
//	             FriendsLocationColumns.FRIENDS_HEIGHT}, null, null, null,null,null,null);
//		List<Map<String ,Object>> list=new ArrayList<Map<String,Object>>();
//		while(mCursor.moveToNext()){
//			Map<String,Object> map=new HashMap<String,Object>();
//			map.put("F_ID",mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns._ID)) );
//			map.put("FRIEND_ID", mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns.FRIENDS_ID)));
//			map.put("FRIEND_REPORT_TIME", mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns.REPORT_TIME)));
//			map.put("FRIEND_LON", mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns.FRIENDS_LON)));
//			map.put("FRIEND_LAT", mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns.FRIENDS_LAT)));
//			map.put("FRIEND_HEIGHT", mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns.FRIENDS_HEIGHT)));
//			list.add(map); 	
//		}
//		mCursor.close();
//		return list;
//	}
	public int getSize() throws SQLException{
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase=databaseHelper.getReadableDatabase();
		//查询
		Cursor mCursor=sqliteDatabase.query(true,FriendsLocationColumns.TABLE_NAME,
				new String[]{FriendsLocationColumns._ID,FriendsLocationColumns.FRIENDS_ID ,FriendsLocationColumns.REPORT_TIME,
	             FriendsLocationColumns.FRIENDS_LON,FriendsLocationColumns.FRIENDS_LAT,
	             FriendsLocationColumns.FRIENDS_HEIGHT},null, null, null,null,null,null);
		
		return mCursor.getCount();
	}
	
	public FriendLocation getById(long rowId) throws SQLException{
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase=databaseHelper.getReadableDatabase();
		FriendLocation mFriendLocation=new FriendLocation();
		//查询
		Cursor mCursor=sqliteDatabase.query(true,FriendsLocationColumns.TABLE_NAME,
				new String[]{FriendsLocationColumns._ID,FriendsLocationColumns.FRIENDS_ID ,FriendsLocationColumns.REPORT_TIME,
				             FriendsLocationColumns.FRIENDS_LON,FriendsLocationColumns.FRIENDS_LAT,
				             FriendsLocationColumns.FRIENDS_HEIGHT},KEY_ROWID + "=" + rowId, null, null,null,null,null);
		if(mCursor.moveToNext()){
			mFriendLocation.setId(rowId);
			mFriendLocation.setAddress(mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns.FRIENDS_ID)));
			mFriendLocation.setFriendsLon(mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns.FRIENDS_LON)) );
			mFriendLocation.setFriendsLat(mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns.FRIENDS_LAT)) );
			mFriendLocation.setFriendsHeight(mCursor.getString(mCursor.getColumnIndex(FriendsLocationColumns.FRIENDS_HEIGHT)) );
		}
		return mFriendLocation;
	}
	
	public void close(){
		databaseHelper.close();
		sqliteDatabase.close();
	}
}
