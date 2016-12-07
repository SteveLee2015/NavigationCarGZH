package com.novsky.map.main;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.novsky.map.util.BDContactColumn;
import com.novsky.map.util.DatabaseHelper;

/**
 * 北斗通讯录操作类
 * @author steve
 */
public class BDContactOperation {

	
	private DatabaseHelper databaseHelper=null;

	
	private SQLiteDatabase sqliteDatabase=null;
	
	
	private Context mContext=null;
	
	
	public BDContactOperation(Context context) {
		this.mContext=context;
	}
	
	public DatabaseHelper getDatabaseHelper(){
		if(databaseHelper==null){
			databaseHelper=new DatabaseHelper(mContext);
		}
		return databaseHelper;
	}
	
	/**
	 * 增加通讯录信息
	 * @param mContact
	 */
	public long insert(ContentValues values){
		databaseHelper=new DatabaseHelper(mContext);
		sqliteDatabase=databaseHelper.getWritableDatabase();
		long id=sqliteDatabase.insert(BDContactColumn.TABLE_NAME, null, values);
		if(sqliteDatabase!=null){
			sqliteDatabase.close();
		}
		if(databaseHelper!=null){
			databaseHelper.close();
		}
		return id;
	}
	

	/**
	 * 删除所有的通讯录信息
	 * @return
	 */
	public int delete(String where,String[] whereArgs){
		sqliteDatabase=databaseHelper.getWritableDatabase();
		int num=sqliteDatabase.delete(BDContactColumn.TABLE_NAME, where, whereArgs);
		if(sqliteDatabase!=null){
			sqliteDatabase.close();
		}
		if(databaseHelper!=null){
			databaseHelper.close();
		}
		return num;
	}
	

	public int update(ContentValues values,String where,String[] whereArgs){
		sqliteDatabase=databaseHelper.getWritableDatabase();
		int num=sqliteDatabase.update(BDContactColumn.TABLE_NAME, values, where, whereArgs);
		if(sqliteDatabase!=null){
			sqliteDatabase.close();
		}
		if(databaseHelper!=null){
			databaseHelper.close();
		}
		return num;
	}

	/**
	 * 查询所有的通讯录信息
	 * @return
	 */
	public Cursor query(String[] projection,String selection,String[] selectionArgs,String groupBy,String having,String oder){
		sqliteDatabase=databaseHelper.getReadableDatabase();
		Cursor mCursor=sqliteDatabase.query(BDContactColumn.TABLE_NAME, projection, selection, selectionArgs,groupBy ,having, oder);
		return mCursor;
	}
	
}
