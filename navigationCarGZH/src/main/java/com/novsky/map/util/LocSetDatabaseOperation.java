package com.novsky.map.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.novsky.map.util.DatabaseHelper.LocationSetColumns;

/**
 * 定位设置数据库管理类
 * @author steve
 */
public class LocSetDatabaseOperation {
     
	public static final String KEY_TITLE = "title";
	public static final String KEY_BODY = "body";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_CREATED = "created";
	
	private DatabaseHelper databaseHelper;
	private Context context;
	private SQLiteDatabase sqliteWriteDatabase;
	private SQLiteDatabase sqliteReadDatabase;

	public LocSetDatabaseOperation(Context mContext){
		this.context=mContext;
		databaseHelper=new DatabaseHelper(context);
		sqliteWriteDatabase = databaseHelper.getWritableDatabase();
		sqliteReadDatabase=databaseHelper.getReadableDatabase();
	}
	
	/**
	 * 增加定位设置
	 * @param set
	 * @return
	 */
	public boolean insert(LocationSet locationSet){
		ContentValues contentValues=new ContentValues();
		contentValues.put(LocationSetColumns.LOCATION_FEQ,locationSet.getLocationFeq());
		contentValues.put(LocationSetColumns.HEIGHT_TYPE, locationSet.getHeightType());
		contentValues.put(LocationSetColumns.HEIGHT_VALUE,locationSet.getHeightValue());
		contentValues.put(LocationSetColumns.TIANXI_VALUE,locationSet.getTianxianValue());
		long id=sqliteWriteDatabase.insert(LocationSetColumns.TABLE_NAME, null, contentValues) ;
		return id>0;
	}

	/**
	 * 删除
	 * @param rowId
	 * @return
	 */
	public boolean delete(long rowId){
		boolean istrue=sqliteWriteDatabase.delete(LocationSetColumns.TABLE_NAME,KEY_ROWID +"="+rowId,null)>0;
		return istrue;
	}
	/**
	 * 删除
	 * @param rowId
	 * @return
	 */
	public boolean delete(){
		boolean istrue=sqliteWriteDatabase.delete(LocationSetColumns.TABLE_NAME,null,null)>0;
		return istrue;
	}
	
	public Cursor get(long rowId) throws SQLException{
		
		Cursor mCursor=sqliteReadDatabase.query(true,LocationSetColumns.TABLE_NAME,
				new String[]{LocationSetColumns.LOCATION_FEQ},KEY_ROWID + "=" + rowId, null, null,null,null,null);
		return mCursor;
	}
	/**
	 * 得到数据
	 * @return
	 * @throws SQLException
	 */
	public LocationSet getFirst() throws SQLException{
		//查询
		Cursor mCursor=sqliteReadDatabase.query(true,LocationSetColumns.TABLE_NAME,
				new String[]{LocationSetColumns._ID,LocationSetColumns.LOCATION_FEQ,LocationSetColumns.HEIGHT_TYPE,
				 LocationSetColumns.HEIGHT_VALUE
				,LocationSetColumns.TIANXI_VALUE},null, null, null,null,null,null);
		boolean istrue=mCursor.moveToFirst();
		if(istrue){
			LocationSet set=new LocationSet();
			set.setId(mCursor.getInt(mCursor.getColumnIndex(LocationSetColumns._ID)));
			set.setLocationFeq(mCursor.getString(mCursor.getColumnIndex(LocationSetColumns.LOCATION_FEQ)));
			set.setHeightType(mCursor.getString(mCursor.getColumnIndex(LocationSetColumns.HEIGHT_TYPE)));
			set.setHeightValue(mCursor.getString(mCursor.getColumnIndex(LocationSetColumns.HEIGHT_VALUE)));
			set.setTianxianValue(mCursor.getString(mCursor.getColumnIndex(LocationSetColumns.TIANXI_VALUE)));
			mCursor.close();
			return set;
		}else{
			return null;
		}
		
	}
	public int getSize() throws SQLException{
		//查询
		Cursor mCursor=sqliteReadDatabase.query(true,LocationSetColumns.TABLE_NAME,
				new String[]{LocationSetColumns.LOCATION_FEQ,LocationSetColumns.HEIGHT_TYPE,
				 LocationSetColumns.HEIGHT_VALUE
				,LocationSetColumns.TIANXI_VALUE},null, null, null,null,null,null);
		
		return mCursor.getCount();
	}
	/**
	 * 更新
	 * @param set
	 * @return
	 */
	public boolean update(LocationSet set){
		ContentValues contentValues=new ContentValues();
		contentValues.put(LocationSetColumns.LOCATION_FEQ,set.getLocationFeq());
		contentValues.put(LocationSetColumns.HEIGHT_TYPE, set.getHeightType());
		contentValues.put(LocationSetColumns.HEIGHT_VALUE,set.getHeightValue());
		contentValues.put(LocationSetColumns.TIANXI_VALUE,set.getTianxianValue());
		int index=sqliteWriteDatabase.update(LocationSetColumns.TABLE_NAME, contentValues, LocationSetColumns._ID+"=?", new String[]{String.valueOf(set.getId())});
		return index>0;
	}
	
	public void close(){
		databaseHelper.close();
		if(sqliteWriteDatabase!=null){
		   sqliteWriteDatabase.close();
		}
		if(sqliteReadDatabase!=null){
			sqliteReadDatabase.close();
		}
	}
}
