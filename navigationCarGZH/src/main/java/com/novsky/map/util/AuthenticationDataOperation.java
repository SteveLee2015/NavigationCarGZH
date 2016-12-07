package com.novsky.map.util;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.novsky.map.util.DatabaseHelper.AuthenticationColumns;

public class AuthenticationDataOperation {
     
	public static final String KEY_TITLE = "title";
	public static final String KEY_BODY = "body";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_CREATED = "created";
	private DatabaseHelper databaseHelper;
	private Context context;
	private SQLiteDatabase sqliteDatabase;

	public AuthenticationDataOperation(Context mContext){
		this.context=mContext;
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase=databaseHelper.getReadableDatabase();
	}
	
	/**
	 * 增加授权
	 * @param set
	 * @return
	 */
	public boolean insert(String mUserDeviceNumber){
		ContentValues contentValues=new ContentValues();
		contentValues.put(AuthenticationColumns.USER_DEVICE_NUMBER,mUserDeviceNumber);
		long id=sqliteDatabase.insert(AuthenticationColumns.TABLE_NAME, null, contentValues) ;
		return id>0;
	}

	/**
	 * 删除授权
	 * @param rowId
	 * @return
	 */
	public boolean delete(long rowId){
		boolean istrue=sqliteDatabase.delete(AuthenticationColumns.TABLE_NAME,KEY_ROWID +"="+rowId,null)>0;
		return istrue;
	}
	
	/**
	 * 根据用户设备号码删除
	 * @param rowId
	 * @return
	 */
	public boolean delete(String mUserDeviceNumber){
		boolean istrue=sqliteDatabase.delete(AuthenticationColumns.TABLE_NAME,AuthenticationColumns.USER_DEVICE_NUMBER +"='"+mUserDeviceNumber+"'",null)>0;
		return istrue;
	}
	
	
	/**
	 * 删除
	 * @param rowId
	 * @return
	 */
	public boolean delete(){
		boolean istrue=sqliteDatabase.delete(AuthenticationColumns.TABLE_NAME,null,null)>0;
		return istrue;
	}
	
	/**
	 * 检查当前用户设备号码是否授权
	 * @param rowId
	 * @return
	 * @throws SQLException
	 */
	public boolean checkAccredit(String mUserDeviceNumber) throws SQLException{
		//查询
		Cursor mCursor=sqliteDatabase.query(true,AuthenticationColumns.TABLE_NAME,
				new String[]{AuthenticationColumns._ID,AuthenticationColumns.USER_DEVICE_NUMBER},
				AuthenticationColumns.USER_DEVICE_NUMBER +"='"+mUserDeviceNumber+"'", null,
				null,null,null,null);
		boolean istrue=(mCursor.getCount()>0);
		mCursor.close();
		return istrue;
	}
	
	/**
	 * 得到数据
	 * @return
	 * @throws SQLException
	 */
	public List<String> getAll() throws SQLException{
		//查询
		Cursor mCursor=sqliteDatabase.query(true,AuthenticationColumns.TABLE_NAME,
				new String[]{AuthenticationColumns._ID,AuthenticationColumns.USER_DEVICE_NUMBER},null, null, null,null,AuthenticationColumns._ID+" desc",null);
		List<String> list=new ArrayList<String>();
		while(mCursor.moveToNext()){
			String mUserDeviceNumber=mCursor.getString(mCursor.getColumnIndex(AuthenticationColumns.USER_DEVICE_NUMBER));
			list.add(mUserDeviceNumber);
	    }
		mCursor.close();
		return list;
	}
	
	public void close(){
		if(sqliteDatabase!=null){
			sqliteDatabase.close();
		}
		if(databaseHelper!=null){
		    databaseHelper.close();
		}
	}
}
