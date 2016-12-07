package com.novsky.map.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.novsky.map.util.DatabaseHelper.CustomColumns;

public class DatabaseOperation {
     
	public static final String KEY_TITLE = "title";
	public static final String KEY_BODY = "body";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_CREATED = "created";

	private DatabaseHelper databaseHelper;
	private Context context;
	private SQLiteDatabase sqliteDatabase;

	public DatabaseOperation(Context mContext){
		this.context=mContext;
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase = databaseHelper.getWritableDatabase();
	}
	
	public long insert(BDMSG bd){
		ContentValues contentValues=new ContentValues();
		contentValues.put(CustomColumns.COLUMNS_USER_ADDRESS, bd.getColumnsUserAddress());
		contentValues.put(CustomColumns.COLUMNS_MSG_TYPE, bd.getColumnsMsgType());
		contentValues.put(CustomColumns.COLUMNS_SEND_ADDRESS, bd.getColumnsSendAddress());
		contentValues.put(CustomColumns.COLUMNS_SEND_TIME, bd.getColumnsSendTime());
		contentValues.put(CustomColumns.COLUMNS_MSG_LEN, bd.getColumnsMsgLen());
		contentValues.put(CustomColumns.COLUMNS_MSG_CONTENT, bd.getColumnsMsgContent());
		contentValues.put(CustomColumns.COLUMNS_CRC, bd.getColumnsCrc());
		contentValues.put(CustomColumns.COLUMNS_FLAG, bd.getColumnsMsgFlag());
		long id=sqliteDatabase.insert(CustomColumns.TABLE_NAME, null, contentValues);
		return id;
	}

	
	/**
	 * 更新当前短信的状态(发件人,收件人，未读)
	 * @param rowId
	 * @param flag
	 * @return
	 */
	public boolean updateMessageStatus(long rowId,int flag){
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase=databaseHelper.getWritableDatabase();
		ContentValues contentValues=new ContentValues();
		contentValues.put(CustomColumns.COLUMNS_FLAG, flag);
		return sqliteDatabase.update(CustomColumns.TABLE_NAME, contentValues, CustomColumns._ID+"="+rowId, null)>0;
	}
	
	/**
	 * 删除
	 * @param rowId
	 * @return
	 */
	public boolean delete(long rowId){
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase = databaseHelper.getWritableDatabase();
		return sqliteDatabase.delete(CustomColumns.TABLE_NAME,KEY_ROWID +"="+rowId,null)>0;
	}
	
	public Cursor get(long rowId) throws SQLException{
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase=databaseHelper.getReadableDatabase();
		//查询
		Cursor mCursor=sqliteDatabase.query(true,CustomColumns.TABLE_NAME,new String[]{CustomColumns._ID,CustomColumns.COLUMNS_USER_ADDRESS,
				CustomColumns.COLUMNS_MSG_TYPE,CustomColumns.COLUMNS_SEND_ADDRESS,
				CustomColumns.COLUMNS_SEND_TIME,CustomColumns.COLUMNS_MSG_LEN,
				CustomColumns.COLUMNS_MSG_CONTENT,CustomColumns.COLUMNS_CRC,
				CustomColumns.COLUMNS_FLAG
				},KEY_ROWID + "=" + rowId, null, null,null,null,null);
		
		return mCursor;
	}
	
	public Cursor getAll() throws SQLException{
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase=databaseHelper.getReadableDatabase();
		String orderBy = CustomColumns._ID+" desc";
		//查询
		Cursor mCursor=sqliteDatabase.query(true,CustomColumns.TABLE_NAME,new String[]{CustomColumns._ID,CustomColumns.COLUMNS_USER_ADDRESS,
				CustomColumns.COLUMNS_MSG_TYPE,CustomColumns.COLUMNS_SEND_ADDRESS,
				CustomColumns.COLUMNS_SEND_TIME,CustomColumns.COLUMNS_MSG_LEN,
				CustomColumns.COLUMNS_MSG_CONTENT,CustomColumns.COLUMNS_CRC,
				CustomColumns.COLUMNS_FLAG
				},null, null, null,null,orderBy ,null);
		return mCursor;
	}
	
	/**
	 * 收件箱
	 * @return
	 * @throws SQLException
	 */
	public Cursor getReceiveMessages() throws SQLException{
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase=databaseHelper.getReadableDatabase();
		String orderBy = CustomColumns._ID+" desc";
		//查询
		Cursor mCursor=sqliteDatabase.query(true,CustomColumns.TABLE_NAME,new String[]{CustomColumns._ID,CustomColumns.COLUMNS_USER_ADDRESS,
				CustomColumns.COLUMNS_MSG_TYPE,CustomColumns.COLUMNS_SEND_ADDRESS,
				CustomColumns.COLUMNS_SEND_TIME,CustomColumns.COLUMNS_MSG_LEN,
				CustomColumns.COLUMNS_MSG_CONTENT,CustomColumns.COLUMNS_CRC,
				CustomColumns.COLUMNS_FLAG
				},CustomColumns.COLUMNS_FLAG+"=0", null, null,null,orderBy ,null);
		return mCursor;
	} 
	/**
	 * 未读信息
	 * @return
	 * @throws SQLException
	 */
	public Cursor getUnReadMessages() throws SQLException{
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase=databaseHelper.getReadableDatabase();
		String orderBy = CustomColumns._ID+" desc";
		//查询
		Cursor mCursor=sqliteDatabase.query(true,CustomColumns.TABLE_NAME,new String[]{CustomColumns._ID,CustomColumns.COLUMNS_USER_ADDRESS,
				CustomColumns.COLUMNS_MSG_TYPE,CustomColumns.COLUMNS_SEND_ADDRESS,
				CustomColumns.COLUMNS_SEND_TIME,CustomColumns.COLUMNS_MSG_LEN,
				CustomColumns.COLUMNS_MSG_CONTENT,CustomColumns.COLUMNS_CRC,
				CustomColumns.COLUMNS_FLAG
		},CustomColumns.COLUMNS_FLAG+"=3", null, null,null,orderBy ,null);
		return mCursor;
	} 
	/**
	 * 删除收件箱所有的内容
	 * @return
	 * @throws SQLException
	 */
	public boolean deleteReceiveMessages() throws SQLException{
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase=databaseHelper.getReadableDatabase();
		int size=sqliteDatabase.delete(CustomColumns.TABLE_NAME, CustomColumns.COLUMNS_FLAG+"=0", null);
		return size>0;
	} 
	
	
	/**
	 * 发件信息
	 * @return
	 * @throws SQLException
	 */
	public Cursor getSendMessages() throws SQLException{
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase=databaseHelper.getReadableDatabase();
		String orderBy = CustomColumns._ID+" desc";
		//查询
		Cursor mCursor=sqliteDatabase.query(true,CustomColumns.TABLE_NAME,new String[]{CustomColumns._ID,CustomColumns.COLUMNS_USER_ADDRESS,
				CustomColumns.COLUMNS_MSG_TYPE,CustomColumns.COLUMNS_SEND_ADDRESS,
				CustomColumns.COLUMNS_SEND_TIME,CustomColumns.COLUMNS_MSG_LEN,
				CustomColumns.COLUMNS_MSG_CONTENT,CustomColumns.COLUMNS_CRC,
				CustomColumns.COLUMNS_FLAG
				},CustomColumns.COLUMNS_FLAG+"=1", null, null,null,orderBy ,null);
		return mCursor;
	}
	
	/**
	 * 删除发件箱所有的内容
	 * @return
	 * @throws SQLException
	 */
	public boolean deleteSendMessages() throws SQLException{
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase=databaseHelper.getReadableDatabase();
		int size=sqliteDatabase.delete(CustomColumns.TABLE_NAME, CustomColumns.COLUMNS_FLAG+"=1", null);
		return size>0;
	} 
	
	
	/**
	 * 短信标识  0-收件箱  1-发件箱  2-草稿箱 
	 * @param Flag
	 * @return
	 * @throws SQLException
	 */
	public Cursor getAllByType(int Flag) throws SQLException{
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase=databaseHelper.getReadableDatabase();
		//查询
		Cursor mCursor=sqliteDatabase.query(true,CustomColumns.TABLE_NAME,new String[]{CustomColumns.COLUMNS_USER_ADDRESS,
				CustomColumns.COLUMNS_MSG_TYPE,CustomColumns.COLUMNS_SEND_ADDRESS,
				CustomColumns.COLUMNS_SEND_TIME,CustomColumns.COLUMNS_MSG_LEN,
				CustomColumns.COLUMNS_MSG_CONTENT,CustomColumns.COLUMNS_CRC
				},CustomColumns.COLUMNS_FLAG+"=?", new String[]{String.valueOf(Flag)}, null,null,null,null);
		return mCursor;
	}
	
	public void close(){
		if(databaseHelper!=null){
			databaseHelper.close();
		}
		if(sqliteDatabase!=null){
			sqliteDatabase.close();
		}
	}
	
	/**
	 * 根据条件查询短信
	 * @param Flag
	 * @return
	 * @throws SQLException
	 */
	public Cursor getMessageByCond(String condition) throws SQLException{
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase=databaseHelper.getReadableDatabase();
		String orderBy = CustomColumns._ID+" desc";
		//查询
		Cursor mCursor=sqliteDatabase.query(false,CustomColumns.TABLE_NAME,new String[]{CustomColumns._ID,CustomColumns.COLUMNS_USER_ADDRESS,
				CustomColumns.COLUMNS_MSG_TYPE,CustomColumns.COLUMNS_SEND_ADDRESS,
				CustomColumns.COLUMNS_SEND_TIME,CustomColumns.COLUMNS_MSG_LEN,
				CustomColumns.COLUMNS_MSG_CONTENT,CustomColumns.COLUMNS_CRC,CustomColumns.COLUMNS_FLAG,
				},CustomColumns.COLUMNS_USER_ADDRESS+" like ? or "+CustomColumns.COLUMNS_MSG_CONTENT +" like ? ", new String[]{"%"+condition+"%","%"+condition+"%"}, null,null,orderBy,null);
		return mCursor;
	}
	
	public boolean delete(){
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase = databaseHelper.getWritableDatabase();
		return sqliteDatabase.delete(CustomColumns.TABLE_NAME,null,null)>0;
	}
	
}
