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

import com.novsky.map.util.DatabaseHelper.MsgWordColumns;

public class MessgeUsualOperation {

	public static final String KEY_TITLE = "title";
	public static final String KEY_BODY = "body";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_CREATED = "created";

	private DatabaseHelper databaseHelper;
	private Context context;
	private SQLiteDatabase sqliteDatabase;

	public MessgeUsualOperation(Context mContext) {
		this.context = mContext;
		databaseHelper=new DatabaseHelper(context);
		sqliteDatabase = databaseHelper.getWritableDatabase();
	}

	/**
	 * 增加友邻位置
	 * @param set
	 * @return
	 */
	public long insert(String wordText){
		ContentValues contentValues=new ContentValues();
		contentValues.put(MsgWordColumns.COLUMNS_WORD ,wordText);
		long id=sqliteDatabase.insert(MsgWordColumns.TABLE_NAME, null, contentValues) ;
		return id;
	}
	
	public boolean update(long rowId,String wordText){
		ContentValues contentValues=new ContentValues();
		contentValues.put(MsgWordColumns.COLUMNS_WORD ,wordText);
		long id=sqliteDatabase.update(MsgWordColumns.TABLE_NAME, contentValues, MsgWordColumns._ID+"="+rowId,null);
		return id>0;
	}

	public List<Map<String, Object>> getAll() throws SQLException {
		String orderBy = MsgWordColumns._ID + " desc";
		// 查询
		Cursor mCursor = sqliteDatabase
				.query(true, MsgWordColumns.TABLE_NAME, new String[] {
						MsgWordColumns._ID, MsgWordColumns.COLUMNS_WORD },
						null, null, null, null, null, null);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		int index=0;
		while (mCursor.moveToNext()) {
			    index++;
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("MESSAGE_WORD_ID", mCursor.getString(mCursor.getColumnIndex(MsgWordColumns._ID)));
				map.put("MESSAGE_WORD_TEXT", mCursor.getString(mCursor
						.getColumnIndex(MsgWordColumns.COLUMNS_WORD)));
				map.put("MESSAGE_WORD_CHECKED",false);
				map.put("MESSAGE_NUM", index);
				list.add(map);
		}
		mCursor.close();
		return list;
	}
	
	public String[] getAllMessagesArray() throws SQLException {
		String orderBy = MsgWordColumns._ID + " desc";
		// 查询
		Cursor mCursor = sqliteDatabase
				.query(true, MsgWordColumns.TABLE_NAME, new String[] {
						MsgWordColumns._ID, MsgWordColumns.COLUMNS_WORD },
						null, null, null, null, orderBy, null);
		String[] array=new String[mCursor.getCount()];
		int index=0;
		while (mCursor.moveToNext()) {
				array[index]=mCursor.getString(mCursor
						.getColumnIndex(MsgWordColumns.COLUMNS_WORD));
				index++;
		}
		mCursor.close();
		return array;
	}

	public Cursor getMessageWordById(long rowId) throws SQLException {
		// 查询
		Cursor mCursor = sqliteDatabase
				.query(true, MsgWordColumns.TABLE_NAME, new String[] {
						MsgWordColumns._ID, MsgWordColumns.COLUMNS_WORD },
						MsgWordColumns._ID + "=" + rowId, null, null, null,
						null, null);
		return mCursor;
	}

	/**
	 * 删除
	 * @param rowId
	 * @return
	 */
	public boolean delete(long rowId) {
		return sqliteDatabase.delete(MsgWordColumns.TABLE_NAME, KEY_ROWID + "="
				+ rowId, null) > 0;
	}
	/**
	 * 删除所有的
	 * @return
	 */
	public boolean deleteAll() {
		return sqliteDatabase.delete(MsgWordColumns.TABLE_NAME,null, null) > 0;
	}

	public void close() {
		if (databaseHelper != null) {
			databaseHelper.close();
		}
		if (sqliteDatabase != null) {
			sqliteDatabase.close();
		}
	}
}
