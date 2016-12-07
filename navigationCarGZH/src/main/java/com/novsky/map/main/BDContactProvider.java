package com.novsky.map.main;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.novsky.map.util.BDContactColumn;
import com.novsky.map.util.DatabaseHelper;

/**
 * 北斗通讯录
 * @author steve
 */
public class BDContactProvider extends ContentProvider {
    
	private static final String TAG="BDContactProvider";
	
	private static UriMatcher uriMatcher=null;
	
	
	private static Map<String,String> projection=null;
	
	
	private BDContactOperation operation=null;
	
	
	private final static int BD_CONTACT=0;
	
	
	private final static int BD_CONTACT_ID=1;
	
	static{
		uriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(BDContactColumn.AUTHORITY,"bdcontact", BD_CONTACT);
		uriMatcher.addURI(BDContactColumn.AUTHORITY,"bdcontact/#", BD_CONTACT_ID);
		projection=new HashMap<String,String>();
		projection.put(BDContactColumn._ID, BDContactColumn._ID);
		projection.put(BDContactColumn.USER_NAME, BDContactColumn.USER_NAME);
		projection.put(BDContactColumn.CARD_NUM, BDContactColumn.CARD_NUM);
		projection.put(BDContactColumn.CARD_LEVEL, BDContactColumn.CARD_LEVEL);
		projection.put(BDContactColumn.CARD_SERIAL_NUM, BDContactColumn.CARD_SERIAL_NUM);
		projection.put(BDContactColumn.CARD_FREQUENCY, BDContactColumn.CARD_FREQUENCY);
		projection.put(BDContactColumn.USER_ADDRESS, BDContactColumn.USER_ADDRESS);
		projection.put(BDContactColumn.PHONE_NUMBER, BDContactColumn.PHONE_NUMBER);
		projection.put(BDContactColumn.USER_EMAIL, BDContactColumn.USER_EMAIL);
		projection.put(BDContactColumn.CHECK_CURRENT_NUM, BDContactColumn.CHECK_CURRENT_NUM);
		projection.put(BDContactColumn.FIRST_LETTER_INDEX, BDContactColumn.FIRST_LETTER_INDEX);
		projection.put(BDContactColumn.REMARK, BDContactColumn.REMARK);
	}
	
	@Override
	public boolean onCreate() {
		operation=new BDContactOperation(this.getContext());
		return false;
	}
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int count=0;
		switch(uriMatcher.match(uri)){
			case BD_CONTACT_ID:
				String noteId=uri.getPathSegments().get(1);
				count=operation.delete(BDContactColumn._ID + "="+noteId+ (!TextUtils.isEmpty(where) ? " AND ("+where+")":""),whereArgs);
				break;
			case BD_CONTACT:
				count=operation.delete(where, whereArgs);
				break;
			default:
				break;
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
   }

	
	@Override
	public String getType(Uri uri) {
		switch(uriMatcher.match(uri)){
			case BD_CONTACT_ID:
				return BDContactColumn.CONTENT_ITEM_TYPE;
			case BD_CONTACT:
                return BDContactColumn.CONTENT_TYPE	;			
			default:
				throw new IllegalArgumentException("错误的URI:"+uri);
		}
	}

	
	@Override
	public Uri insert(Uri uri, ContentValues initValues) {
		if(uriMatcher.match(uri)!=BD_CONTACT){
			throw new IllegalArgumentException("错误的URI:"+uri);
		}
		ContentValues values=null;
		if(initValues!=null){
			values=initValues;
		}else{
			values=new ContentValues();
		}
		long rowId=operation.insert(values);
		if(rowId>0){
			Uri contactUri=ContentUris.withAppendedId(BDContactColumn.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(contactUri, null);
			return contactUri;
		}
		throw new SQLException("数据库插入失败!"+uri);
	}
	

	@Override
	public Cursor query(Uri uri, String[] project, String selection, String[] selectionArgs,String sortOrder) {
        SQLiteQueryBuilder qb=new SQLiteQueryBuilder();
		switch(uriMatcher.match(uri)){
			case BD_CONTACT:
				qb.setTables(BDContactColumn.TABLE_NAME);
				qb.setProjectionMap(projection);
				break;
			case BD_CONTACT_ID:
				qb.setTables(BDContactColumn.TABLE_NAME);
				qb.setProjectionMap(projection);
				qb.appendWhere(BDContactColumn._ID+"="+uri.getPathSegments().get(1));
				break;
			default:
				throw new IllegalArgumentException("uri错误!"+uri);
		}
		String orderBy="";
		if(TextUtils.isEmpty(sortOrder)){
			orderBy=BDContactColumn.DEFAULT_SORT_ORDER;
		}else{
			orderBy=sortOrder;
		}
		DatabaseHelper databaseHelper=operation.getDatabaseHelper();
		SQLiteDatabase db=databaseHelper.getReadableDatabase();
		Cursor mCursor=qb.query(db, project, selection, selectionArgs, null, null, orderBy);
		mCursor.setNotificationUri(getContext().getContentResolver(), uri);
		return mCursor;
	}
	
	
	@Override
	public int update(Uri uri, ContentValues initValues, String where, String[] whereArgs) {
		int count=0;
		switch(uriMatcher.match(uri)){
			case BD_CONTACT:
				count=operation.update(initValues, where, whereArgs);
				break;
			case BD_CONTACT_ID:
				String noteId=uri.getPathSegments().get(1);
				count=operation.update(initValues, BDContactColumn._ID+"="+noteId+(!TextUtils.isEmpty(where)? " AND ("+where+")":""), whereArgs);
				break;
			default:
				throw new IllegalArgumentException("错误的URI:"+uri);
		}
        getContext().getContentResolver().notifyChange(uri, null);
		return count;
   }
	
}
