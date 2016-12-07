package com.novsky.map.main;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.novsky.map.util.DatabaseHelper;
import com.novsky.map.util.DatabaseHelper.FriendLocationNavColumns;

/**
 * 友邻位置 深圳海力特 fuck
 * @author llg
 */
public class BDFriendLocationOperation {
     
	public static final String KEY_TITLE = "title";
	public static final String KEY_BODY = "body";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_CREATED = "created";
	public static final String RECEIVE_TIME = "RECEIVE_TIME";
	
	private DatabaseHelper databaseHelper;
	private Context context;
	private SQLiteDatabase sqliteDatabase;

	public BDFriendLocationOperation(Context mContext){
		this.context=mContext;
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase=databaseHelper.getReadableDatabase();
	}
	
	/**
	 * 增加友邻位置
	 * @param set
	 * @return
	 */
	public long insert(String friendCount,String receiveTime,String currentID,String friendID,String friendLon,String friendLonDir,String friendLat,String friendLatDir){
		ContentValues contentValues=new ContentValues();
		contentValues.put(FriendLocationNavColumns.RECEIVE_TIME,receiveTime);
		contentValues.put(FriendLocationNavColumns.FRIEND_COUNT,friendCount);
		contentValues.put(FriendLocationNavColumns.FRIEND_CURRENT_ID,currentID);
		contentValues.put(FriendLocationNavColumns.FRIEND_ID,friendID);
		contentValues.put(FriendLocationNavColumns.FRIEND_LON,friendLon);
		contentValues.put(FriendLocationNavColumns.FRIEND_LON_DIR,friendLonDir);
		contentValues.put(FriendLocationNavColumns.FRIEND_LAT,friendLat);
		contentValues.put(FriendLocationNavColumns.FRIEND_LAT_DIR,friendLatDir);
		long id=sqliteDatabase.insert(FriendLocationNavColumns.TABLE_NAME, null, contentValues) ;
		return id;
	}
	/**
	 * 增加友邻位置
	 * @param set
	 * @return
	 */
	public long insert(FriendBDPoint friendBDPoint){
		ContentValues contentValues=new ContentValues();
		contentValues.put(FriendLocationNavColumns.RECEIVE_TIME,friendBDPoint.getReceiveTime());
		contentValues.put(FriendLocationNavColumns.FRIEND_COUNT,friendBDPoint.getFriendCount());
		contentValues.put(FriendLocationNavColumns.FRIEND_CURRENT_ID,friendBDPoint.getCurrentID());
		contentValues.put(FriendLocationNavColumns.FRIEND_ID,friendBDPoint.getFriendID());
		contentValues.put(FriendLocationNavColumns.FRIEND_LON,friendBDPoint.getLon());
		contentValues.put(FriendLocationNavColumns.FRIEND_LON_DIR,friendBDPoint.getLonDirection());
		contentValues.put(FriendLocationNavColumns.FRIEND_LAT,friendBDPoint.getLat());
		contentValues.put(FriendLocationNavColumns.FRIEND_LAT_DIR,friendBDPoint.getLatDirection());
		long id=sqliteDatabase.insert(FriendLocationNavColumns.TABLE_NAME, null, contentValues) ;
		return id;
	}

	/**
	 * 删除
	 * @param rowId
	 * @return
	 */
	public boolean delete(long rowId){
		boolean istrue=sqliteDatabase.delete(FriendLocationNavColumns.TABLE_NAME,KEY_ROWID +"="+rowId,null)>0;
		return istrue;
	}
	
	public boolean delete(String receiveTime){
		boolean istrue=sqliteDatabase.delete(FriendLocationNavColumns.TABLE_NAME,FriendLocationNavColumns.RECEIVE_TIME +" = ? ",new String[]{receiveTime})>0;
		return istrue;
	}
	
	/**
	 * 全部删除
	 * @param rowId
	 * @return
	 */
	public boolean delete(){
		boolean istrue=sqliteDatabase.delete(FriendLocationNavColumns.TABLE_NAME,null,null)>0;
		return istrue;
	}
	
	
	/**
	 * 根据_id 查询
	 * @param rowId
	 * @return
	 * @throws SQLException
	 */
	public FriendBDPoint getByRowID(long rowId) throws SQLException{
		//查询
		Cursor mCursor=sqliteDatabase.query(true,FriendLocationNavColumns.TABLE_NAME,
				new String[]{
				FriendLocationNavColumns._ID,
				FriendLocationNavColumns.RECEIVE_TIME,
				FriendLocationNavColumns.FRIEND_COUNT,
				FriendLocationNavColumns.FRIEND_CURRENT_ID,
				FriendLocationNavColumns.FRIEND_ID,
				FriendLocationNavColumns.FRIEND_LON,
				FriendLocationNavColumns.FRIEND_LAT},
				KEY_ROWID + "=" + rowId, null, null,null,null,null);
		FriendBDPoint nav=new FriendBDPoint();   
		if(mCursor.moveToNext()){
			  nav.setRowId(mCursor.getLong(mCursor.getColumnIndex(FriendLocationNavColumns._ID)));
			  nav.setReceiveTime(mCursor.getString(mCursor.getColumnIndex(FriendLocationNavColumns.RECEIVE_TIME)));
			  nav.setFriendCount(mCursor.getString(mCursor.getColumnIndex(FriendLocationNavColumns.FRIEND_COUNT)));
			  nav.setCurrentID(mCursor.getString(mCursor.getColumnIndex(FriendLocationNavColumns.FRIEND_CURRENT_ID)));
			  nav.setFriendID(mCursor.getString(mCursor.getColumnIndex(FriendLocationNavColumns.FRIEND_ID)));
			  nav.setLon(mCursor.getString(mCursor.getColumnIndex(FriendLocationNavColumns.FRIEND_LON)));
			  nav.setLonDirection(mCursor.getString(mCursor.getColumnIndex(FriendLocationNavColumns.FRIEND_LON_DIR)));
			  nav.setLat(mCursor.getString(mCursor.getColumnIndex(FriendLocationNavColumns.FRIEND_LAT)));
			  nav.setLatDirection(mCursor.getString(mCursor.getColumnIndex(FriendLocationNavColumns.FRIEND_LAT_DIR)));
			  
		}
		mCursor.close();
		return nav;
	}
	
	
	/**
	 * 根据接收时间 查询
	 * @param rowId
	 * @return
	 * @throws SQLException
	 */
	public List<FriendBDPoint> getByReceiveTime(String receiveTime) throws SQLException{
		//查询
		Cursor mCursor=sqliteDatabase.query(true,FriendLocationNavColumns.TABLE_NAME,
				new String[]{
				FriendLocationNavColumns._ID,
				FriendLocationNavColumns.RECEIVE_TIME,
				FriendLocationNavColumns.FRIEND_COUNT,
				FriendLocationNavColumns.FRIEND_CURRENT_ID,
				FriendLocationNavColumns.FRIEND_ID,
				FriendLocationNavColumns.FRIEND_LON,
				FriendLocationNavColumns.FRIEND_LON_DIR,
				FriendLocationNavColumns.FRIEND_LAT_DIR,
				FriendLocationNavColumns.FRIEND_LAT},
				FriendLocationNavColumns.RECEIVE_TIME + "= ? ", new String[]{receiveTime}, null,null,null,null);
		List<FriendBDPoint> list = new ArrayList<FriendBDPoint>();
		int count = mCursor.getCount();
		for (int i = 0; i < count; i++) {
			
			if (mCursor.moveToNext()) {
				FriendBDPoint nav = new FriendBDPoint();
				nav.setRowId(mCursor.getLong(mCursor
						.getColumnIndex(FriendLocationNavColumns._ID)));
				nav.setReceiveTime(mCursor.getString(mCursor
						.getColumnIndex(FriendLocationNavColumns.RECEIVE_TIME)));
				nav.setFriendCount(mCursor.getString(mCursor
						.getColumnIndex(FriendLocationNavColumns.FRIEND_COUNT)));
				nav.setCurrentID(mCursor.getString(mCursor
						.getColumnIndex(FriendLocationNavColumns.FRIEND_CURRENT_ID)));
				nav.setFriendID(mCursor.getString(mCursor
						.getColumnIndex(FriendLocationNavColumns.FRIEND_ID)));
				nav.setLon(mCursor.getString(mCursor
						.getColumnIndex(FriendLocationNavColumns.FRIEND_LON)));
				nav.setLonDirection(mCursor.getString(mCursor
						.getColumnIndex(FriendLocationNavColumns.FRIEND_LON_DIR)));
				nav.setLat(mCursor.getString(mCursor
						.getColumnIndex(FriendLocationNavColumns.FRIEND_LAT)));
				nav.setLatDirection(mCursor.getString(mCursor
						.getColumnIndex(FriendLocationNavColumns.FRIEND_LAT_DIR)));
				
				list.add(nav);
			}
		}
		
		mCursor.close();
		return list;
	}
	
	public List<FriendBDPoint> getAll() throws SQLException{
		Cursor mCursor=sqliteDatabase.query(true,FriendLocationNavColumns.TABLE_NAME,
				new String[]{
				FriendLocationNavColumns._ID,
				FriendLocationNavColumns.RECEIVE_TIME,
				FriendLocationNavColumns.FRIEND_COUNT,
				FriendLocationNavColumns.FRIEND_CURRENT_ID,
				FriendLocationNavColumns.FRIEND_ID,
				FriendLocationNavColumns.FRIEND_LON,
				FriendLocationNavColumns.FRIEND_LON_DIR,
				FriendLocationNavColumns.FRIEND_LAT_DIR,
				FriendLocationNavColumns.FRIEND_LAT},null, null, null,null,FriendLocationNavColumns._ID +" desc",null);
		
		List<FriendBDPoint> mFriendLoctionNavs=new  ArrayList<FriendBDPoint>();
		
		while (mCursor.moveToNext()) {
			FriendBDPoint nav = new FriendBDPoint();

			nav.setRowId(mCursor.getLong(mCursor
					.getColumnIndex(FriendLocationNavColumns._ID)));
			nav.setReceiveTime(mCursor.getString(mCursor
					.getColumnIndex(FriendLocationNavColumns.RECEIVE_TIME)));
			nav.setFriendCount(mCursor.getString(mCursor
					.getColumnIndex(FriendLocationNavColumns.FRIEND_COUNT)));
			nav.setCurrentID(mCursor.getString(mCursor
					.getColumnIndex(FriendLocationNavColumns.FRIEND_CURRENT_ID)));
			nav.setFriendID(mCursor.getString(mCursor
					.getColumnIndex(FriendLocationNavColumns.FRIEND_ID)));
			nav.setLon(mCursor.getString(mCursor
					.getColumnIndex(FriendLocationNavColumns.FRIEND_LON)));
			nav.setLat(mCursor.getString(mCursor
					.getColumnIndex(FriendLocationNavColumns.FRIEND_LAT)));
			nav.setLatDirection(mCursor.getString(mCursor
					.getColumnIndex(FriendLocationNavColumns.FRIEND_LAT_DIR)));
			nav.setLonDirection(mCursor.getString(mCursor
					.getColumnIndex(FriendLocationNavColumns.FRIEND_LON_DIR)));

			mFriendLoctionNavs.add(nav);
		}
		mCursor.close();
		return mFriendLoctionNavs;
	}
	
	/**
	 * 获取所有的上报时间
	 * @return
	 */
	public List<String> getReceiveTime() {

		Cursor mCursor = sqliteDatabase.query(true,
				FriendLocationNavColumns.TABLE_NAME, new String[] {
						FriendLocationNavColumns._ID,
						FriendLocationNavColumns.RECEIVE_TIME,
						FriendLocationNavColumns.FRIEND_COUNT,
						FriendLocationNavColumns.FRIEND_CURRENT_ID,
						FriendLocationNavColumns.FRIEND_ID,
						FriendLocationNavColumns.FRIEND_LON,
						FriendLocationNavColumns.FRIEND_LAT }, null, null,
				null, null, FriendLocationNavColumns._ID + " desc", null);

		List<String> mReceiveTimeList = new ArrayList<String>();

		while (mCursor.moveToNext()) {

			String receive = mCursor.getString(mCursor
					.getColumnIndex(FriendLocationNavColumns.RECEIVE_TIME));
			mReceiveTimeList.add(receive);
		}
		mCursor.close();
		return mReceiveTimeList;
	}
	
	public int getSize() throws SQLException{
		//查询
		Cursor mCursor=sqliteDatabase.query(true,FriendLocationNavColumns.TABLE_NAME,
				new String[]{
				FriendLocationNavColumns._ID,
				FriendLocationNavColumns.RECEIVE_TIME,
				FriendLocationNavColumns.FRIEND_COUNT,
				FriendLocationNavColumns.FRIEND_CURRENT_ID,
				FriendLocationNavColumns.FRIEND_ID,
				FriendLocationNavColumns.FRIEND_LON,
				FriendLocationNavColumns.FRIEND_LAT},null, null, null,null,null,null);
		return mCursor.getCount();
	}
	
	
	public void close(){
		databaseHelper.close();
		sqliteDatabase.close();
	}
}
