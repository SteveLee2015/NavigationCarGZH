package com.novsky.map.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.mapabc.android.activity.base.NaviStudioApplication;
import com.novsky.map.util.DatabaseHelper.CustomColumns;
import com.novsky.map.util.DatabaseHelper.CustomColumns4G;
import java.util.ArrayList;
import java.util.List;

public class DatabaseOperation {

    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_CREATED = "created";

    private DatabaseHelper databaseHelper;
    private Context context;
    private SQLiteDatabase writeSqlDatabase;
    private SQLiteDatabase readSqlDatabase;

    private static DatabaseOperation instance;

    public static DatabaseOperation getInstance() {
        if (instance == null) {
            instance = new DatabaseOperation(NaviStudioApplication.getContext());
        }
        return instance;
    }

    private DatabaseOperation(Context mContext) {
        this.context = mContext;
        databaseHelper = new DatabaseHelper(context);
        writeSqlDatabase = databaseHelper.getWritableDatabase();
        readSqlDatabase = databaseHelper.getReadableDatabase();
    }

    public long insert(BDMSG bd) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CustomColumns.COLUMNS_USER_ADDRESS, bd.getColumnsUserAddress());
        contentValues.put(CustomColumns.COLUMNS_MSG_TYPE, bd.getColumnsMsgType());
        contentValues.put(CustomColumns.COLUMNS_SEND_ADDRESS, bd.getColumnsSendAddress());
        contentValues.put(CustomColumns.COLUMNS_SEND_TIME, bd.getColumnsSendTime());
        contentValues.put(CustomColumns.COLUMNS_MSG_LEN, bd.getColumnsMsgLen());
        contentValues.put(CustomColumns.COLUMNS_MSG_CONTENT, bd.getColumnsMsgContent());
        contentValues.put(CustomColumns.COLUMNS_CRC, bd.getColumnsCrc());
        contentValues.put(CustomColumns.COLUMNS_FLAG, bd.getColumnsMsgFlag());
        long id = writeSqlDatabase.insert(CustomColumns.TABLE_NAME, null, contentValues);
        return id;
    }

    public long insert4G(BDMSG bd) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CustomColumns4G.COLUMNS_USER_ADDRESS, bd.getColumnsUserAddress());
        contentValues.put(CustomColumns4G.COLUMNS_MSG_TYPE, bd.getColumnsMsgType());
        contentValues.put(CustomColumns4G.COLUMNS_SEND_ADDRESS, bd.getColumnsSendAddress());
        contentValues.put(CustomColumns4G.COLUMNS_SEND_TIME, bd.getColumnsSendTime());
        contentValues.put(CustomColumns4G.COLUMNS_MSG_LEN, bd.getColumnsMsgLen());
        contentValues.put(CustomColumns4G.COLUMNS_MSG_CONTENT, bd.getColumnsMsgContent());
        contentValues.put(CustomColumns4G.COLUMNS_CRC, bd.getColumnsCrc());
        contentValues.put(CustomColumns4G.COLUMNS_FLAG, bd.getColumnsMsgFlag());
        long id = writeSqlDatabase.insert(CustomColumns4G.TABLE_NAME, null, contentValues);
        return id;
    }

    /**
     * 更新当前短信的状态(发件人,收件人，未读)
     *
     * @param rowId
     * @param flag
     * @return
     */
    public boolean updateMessageStatus(long rowId, int flag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CustomColumns.COLUMNS_FLAG, flag);
        return writeSqlDatabase.update(CustomColumns.TABLE_NAME, contentValues, CustomColumns._ID + "=" + rowId, null) > 0;
    }

    public boolean update4GMessageStatus(long rowId, int flag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CustomColumns4G.COLUMNS_FLAG, flag);
        return writeSqlDatabase.update(CustomColumns4G.TABLE_NAME, contentValues, CustomColumns4G._ID + "=" + rowId, null) > 0;
    }

    /**
     * 删除
     *
     * @param rowId
     * @return
     */
    public boolean delete(long rowId) {
        return writeSqlDatabase.delete(CustomColumns.TABLE_NAME, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public boolean delete4G(long rowId) {
        return writeSqlDatabase.delete(CustomColumns4G.TABLE_NAME, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor get(long rowId) throws SQLException {
        //查询
        Cursor mCursor = readSqlDatabase.query(true, CustomColumns.TABLE_NAME, new String[]{CustomColumns._ID, CustomColumns.COLUMNS_USER_ADDRESS,
                CustomColumns.COLUMNS_MSG_TYPE, CustomColumns.COLUMNS_SEND_ADDRESS,
                CustomColumns.COLUMNS_SEND_TIME, CustomColumns.COLUMNS_MSG_LEN,
                CustomColumns.COLUMNS_MSG_CONTENT, CustomColumns.COLUMNS_CRC,
                CustomColumns.COLUMNS_FLAG
        }, KEY_ROWID + "=" + rowId, null, null, null, null, null);

        return mCursor;
    }

    public Cursor get4GMsg(long rowId) throws SQLException {
        //查询
        Cursor mCursor = readSqlDatabase.query(true, CustomColumns4G.TABLE_NAME, new String[]{CustomColumns4G._ID, CustomColumns4G.COLUMNS_USER_ADDRESS,
                CustomColumns4G.COLUMNS_MSG_TYPE, CustomColumns4G.COLUMNS_SEND_ADDRESS,
                CustomColumns4G.COLUMNS_SEND_TIME, CustomColumns4G.COLUMNS_MSG_LEN,
                CustomColumns4G.COLUMNS_MSG_CONTENT, CustomColumns4G.COLUMNS_CRC,
                CustomColumns4G.COLUMNS_FLAG
        }, KEY_ROWID + "=" + rowId, null, null, null, null, null);

        return mCursor;
    }

    public Cursor getAll() throws SQLException {
        String orderBy = CustomColumns._ID + " desc";
        //查询
        Cursor mCursor = readSqlDatabase.query(true, CustomColumns.TABLE_NAME, new String[]{CustomColumns._ID, CustomColumns.COLUMNS_USER_ADDRESS,
                CustomColumns.COLUMNS_MSG_TYPE, CustomColumns.COLUMNS_SEND_ADDRESS,
                CustomColumns.COLUMNS_SEND_TIME, CustomColumns.COLUMNS_MSG_LEN,
                CustomColumns.COLUMNS_MSG_CONTENT, CustomColumns.COLUMNS_CRC,
                CustomColumns.COLUMNS_FLAG
        }, null, null, null, null, orderBy, null);
        return mCursor;
    }

    public Cursor getAll4GMsg() throws SQLException {
        String orderBy = CustomColumns4G._ID + " desc";
        //查询
        Cursor mCursor = readSqlDatabase.query(true, CustomColumns4G.TABLE_NAME, new String[]{CustomColumns4G._ID, CustomColumns4G.COLUMNS_USER_ADDRESS,
                CustomColumns4G.COLUMNS_MSG_TYPE, CustomColumns4G.COLUMNS_SEND_ADDRESS,
                CustomColumns4G.COLUMNS_SEND_TIME, CustomColumns4G.COLUMNS_MSG_LEN,
                CustomColumns4G.COLUMNS_MSG_CONTENT, CustomColumns4G.COLUMNS_CRC,
                CustomColumns4G.COLUMNS_FLAG
        }, null, null, null, null, orderBy, null);
        return mCursor;
    }

    /**
     * 收件箱
     *
     * @return
     * @throws SQLException
     */
    public Cursor getReceiveMessages() throws SQLException {
        String orderBy = CustomColumns._ID + " desc";
        //查询
        Cursor mCursor = readSqlDatabase.query(true, CustomColumns.TABLE_NAME, new String[]{CustomColumns._ID, CustomColumns.COLUMNS_USER_ADDRESS,
                CustomColumns.COLUMNS_MSG_TYPE, CustomColumns.COLUMNS_SEND_ADDRESS,
                CustomColumns.COLUMNS_SEND_TIME, CustomColumns.COLUMNS_MSG_LEN,
                CustomColumns.COLUMNS_MSG_CONTENT, CustomColumns.COLUMNS_CRC,
                CustomColumns.COLUMNS_FLAG
        }, CustomColumns.COLUMNS_FLAG + "=0", null, null, null, orderBy, null);
        return mCursor;
    }

    /**
     * 收件箱
     *
     * @return
     * @throws SQLException
     */
    public Cursor getReceive4GMessages() throws SQLException {
        String orderBy = CustomColumns4G._ID + " desc";
        //查询
        Cursor mCursor = readSqlDatabase.query(true, CustomColumns4G.TABLE_NAME, new String[]{CustomColumns4G._ID, CustomColumns4G.COLUMNS_USER_ADDRESS,
                CustomColumns4G.COLUMNS_MSG_TYPE, CustomColumns4G.COLUMNS_SEND_ADDRESS,
                CustomColumns4G.COLUMNS_SEND_TIME, CustomColumns4G.COLUMNS_MSG_LEN,
                CustomColumns4G.COLUMNS_MSG_CONTENT, CustomColumns4G.COLUMNS_CRC,
                CustomColumns4G.COLUMNS_FLAG
        }, CustomColumns4G.COLUMNS_FLAG + "=0", null, null, null, orderBy, null);
        return mCursor;
    }

    /**
     * 未读信息
     *
     * @return
     * @throws SQLException
     */
    public Cursor getUnReadMessages() throws SQLException {
        String orderBy = CustomColumns._ID + " desc";
        //查询
        Cursor mCursor = readSqlDatabase.query(true, CustomColumns.TABLE_NAME, new String[]{CustomColumns._ID, CustomColumns.COLUMNS_USER_ADDRESS,
                CustomColumns.COLUMNS_MSG_TYPE, CustomColumns.COLUMNS_SEND_ADDRESS,
                CustomColumns.COLUMNS_SEND_TIME, CustomColumns.COLUMNS_MSG_LEN,
                CustomColumns.COLUMNS_MSG_CONTENT, CustomColumns.COLUMNS_CRC,
                CustomColumns.COLUMNS_FLAG
        }, CustomColumns.COLUMNS_FLAG + "=3", null, null, null, orderBy, null);
        return mCursor;
    }

    public Cursor getUnRead4GMessages() throws SQLException {
        String orderBy = CustomColumns4G._ID + " desc";
        //查询
        Cursor mCursor = readSqlDatabase.query(true, CustomColumns4G.TABLE_NAME, new String[]{CustomColumns4G._ID, CustomColumns4G.COLUMNS_USER_ADDRESS,
                CustomColumns4G.COLUMNS_MSG_TYPE, CustomColumns4G.COLUMNS_SEND_ADDRESS,
                CustomColumns4G.COLUMNS_SEND_TIME, CustomColumns4G.COLUMNS_MSG_LEN,
                CustomColumns4G.COLUMNS_MSG_CONTENT, CustomColumns4G.COLUMNS_CRC,
                CustomColumns4G.COLUMNS_FLAG
        }, CustomColumns4G.COLUMNS_FLAG + "=3", null, null, null, orderBy, null);
        return mCursor;
    }

    /**
     * 删除收件箱所有的内容
     *
     * @return
     * @throws SQLException
     */
    public boolean deleteReceiveMessages() throws SQLException {
        int size = writeSqlDatabase.delete(CustomColumns.TABLE_NAME, CustomColumns.COLUMNS_FLAG + "=0", null);
        return size > 0;
    }

    public boolean deleteReceive4GMessages() throws SQLException {
        int size = writeSqlDatabase.delete(CustomColumns4G.TABLE_NAME, CustomColumns4G.COLUMNS_FLAG + "=0", null);
        return size > 0;
    }

    /**
     * 发件信息
     *
     * @return
     * @throws SQLException
     */
     public Cursor getSendMessages() throws SQLException {
        String orderBy = CustomColumns._ID + " desc";
        //查询
        Cursor mCursor = readSqlDatabase.query(true, CustomColumns.TABLE_NAME, new String[]{CustomColumns._ID, CustomColumns.COLUMNS_USER_ADDRESS,
                CustomColumns.COLUMNS_MSG_TYPE, CustomColumns.COLUMNS_SEND_ADDRESS,
                CustomColumns.COLUMNS_SEND_TIME, CustomColumns.COLUMNS_MSG_LEN,
                CustomColumns.COLUMNS_MSG_CONTENT, CustomColumns.COLUMNS_CRC,
                CustomColumns.COLUMNS_FLAG
        }, CustomColumns.COLUMNS_FLAG + "=1", null, null, null, orderBy, null);
        return mCursor;
    }

    public Cursor getSend4GMessages() throws SQLException {
        String orderBy = CustomColumns4G._ID + " desc";
        //查询
        Cursor mCursor = readSqlDatabase.query(true, CustomColumns4G.TABLE_NAME, new String[]{CustomColumns4G._ID, CustomColumns4G.COLUMNS_USER_ADDRESS,
                CustomColumns4G.COLUMNS_MSG_TYPE, CustomColumns4G.COLUMNS_SEND_ADDRESS,
                CustomColumns4G.COLUMNS_SEND_TIME, CustomColumns4G.COLUMNS_MSG_LEN,
                CustomColumns4G.COLUMNS_MSG_CONTENT, CustomColumns4G.COLUMNS_CRC,
                CustomColumns4G.COLUMNS_FLAG
        }, CustomColumns4G.COLUMNS_FLAG + "=1", null, null, null, orderBy, null);
        return mCursor;
    }


    /**
     * 删除发件箱所有的内容
     *
     * @return
     * @throws SQLException
     */
    public boolean deleteSendMessages() throws SQLException {
        int size = writeSqlDatabase.delete(CustomColumns.TABLE_NAME, CustomColumns.COLUMNS_FLAG + "=1", null);
        return size > 0;
    }


    /**
     * 删除发件箱所有的内容
     *
     * @return
     * @throws SQLException
     */
    public boolean deleteSend4GMessages() throws SQLException {
        int size = writeSqlDatabase.delete(CustomColumns4G.TABLE_NAME, CustomColumns4G.COLUMNS_FLAG + "=1", null);
        return size > 0;
    }


    /**
     * 短信标识  0-收件箱  1-发件箱  2-草稿箱
     *
     * @param Flag
     * @return
     * @throws SQLException
     */
    public Cursor getAllByType(int Flag) throws SQLException {
        //查询
        Cursor mCursor = readSqlDatabase.query(true, CustomColumns.TABLE_NAME, new String[]{CustomColumns.COLUMNS_USER_ADDRESS,
                CustomColumns.COLUMNS_MSG_TYPE, CustomColumns.COLUMNS_SEND_ADDRESS,
                CustomColumns.COLUMNS_SEND_TIME, CustomColumns.COLUMNS_MSG_LEN,
                CustomColumns.COLUMNS_MSG_CONTENT, CustomColumns.COLUMNS_CRC
        }, CustomColumns.COLUMNS_FLAG + "=?", new String[]{String.valueOf(Flag)}, null, null, null, null);
        return mCursor;
    }

    /**
     * 短信标识  0-收件箱  1-发件箱  2-草稿箱
     *
     * @param Flag
     * @return
     * @throws SQLException
     */
    public Cursor getAll4GByType(int Flag) throws SQLException {
        //查询
        Cursor mCursor = readSqlDatabase.query(true, CustomColumns4G.TABLE_NAME, new String[]{CustomColumns4G.COLUMNS_USER_ADDRESS,
                CustomColumns4G.COLUMNS_MSG_TYPE, CustomColumns4G.COLUMNS_SEND_ADDRESS,
                CustomColumns4G.COLUMNS_SEND_TIME, CustomColumns4G.COLUMNS_MSG_LEN,
                CustomColumns4G.COLUMNS_MSG_CONTENT, CustomColumns4G.COLUMNS_CRC
        }, CustomColumns4G.COLUMNS_FLAG + "=?", new String[]{String.valueOf(Flag)}, null, null, null, null);
        return mCursor;
    }

    public void close() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        if (readSqlDatabase != null) {
            readSqlDatabase.close();
        }
        if (writeSqlDatabase != null) {
            writeSqlDatabase.close();
        }
    }

    /**
     * 根据条件查询短信
     * @return
     * @throws SQLException
     */
    public Cursor getMessageByCond(String condition) throws SQLException {
        String orderBy = CustomColumns._ID + " desc";
        //查询
        Cursor mCursor = readSqlDatabase.query(false, CustomColumns.TABLE_NAME, new String[]{CustomColumns._ID, CustomColumns.COLUMNS_USER_ADDRESS,
                CustomColumns.COLUMNS_MSG_TYPE, CustomColumns.COLUMNS_SEND_ADDRESS,
                CustomColumns.COLUMNS_SEND_TIME, CustomColumns.COLUMNS_MSG_LEN,
                CustomColumns.COLUMNS_MSG_CONTENT, CustomColumns.COLUMNS_CRC, CustomColumns.COLUMNS_FLAG,
        }, CustomColumns.COLUMNS_USER_ADDRESS + " like ? or " + CustomColumns.COLUMNS_MSG_CONTENT + " like ? ", new String[]{"%" + condition + "%", "%" + condition + "%"}, null, null, orderBy, null);
        return mCursor;
    }

    /**
     * 根据条件查询短信
     * @return
     * @throws SQLException
     */
    public Cursor getMessage4GByCond(String condition) throws SQLException {
        String orderBy = CustomColumns4G._ID + " desc";
        //查询
        Cursor mCursor = readSqlDatabase.query(false, CustomColumns4G.TABLE_NAME, new String[]{CustomColumns4G._ID, CustomColumns4G.COLUMNS_USER_ADDRESS,
                CustomColumns4G.COLUMNS_MSG_TYPE, CustomColumns4G.COLUMNS_SEND_ADDRESS,
                CustomColumns4G.COLUMNS_SEND_TIME, CustomColumns4G.COLUMNS_MSG_LEN,
                CustomColumns4G.COLUMNS_MSG_CONTENT, CustomColumns4G.COLUMNS_CRC, CustomColumns.COLUMNS_FLAG,
        }, CustomColumns4G.COLUMNS_USER_ADDRESS + " like ? or " + CustomColumns4G.COLUMNS_MSG_CONTENT + " like ? ", new String[]{"%" + condition + "%", "%" + condition + "%"}, null, null, orderBy, null);
        return mCursor;
    }

    public boolean delete() {
        return writeSqlDatabase.delete(CustomColumns.TABLE_NAME, null, null) > 0;
    }

    public boolean delete4G() {
        return writeSqlDatabase.delete(CustomColumns4G.TABLE_NAME, null, null) > 0;
    }

    public long insertDistanceLoc(DistanceLoc loc) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.DistanceLocColumns.LOC_TEXT, loc.getLocText());
        contentValues.put(DatabaseHelper.DistanceLocColumns.LOC_TIME, loc.getLocTime());
        long id = writeSqlDatabase.insert(DatabaseHelper.DistanceLocColumns.TABLE_NAME, null, contentValues);
        return id;
    }

    public long insertDistanceTime(UpDisTime upDisTime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.UpdateDistanceTimeColumns.DISTANCE_NUM, upDisTime.getDistanceNum());
        contentValues.put(DatabaseHelper.UpdateDistanceTimeColumns.LOC_TIME, upDisTime.getLocTime());
        long id = writeSqlDatabase.insert(DatabaseHelper.UpdateDistanceTimeColumns.TABLE_NAME, null, contentValues);
        return id;
    }

    public List<UpDisTime> queryUpDisTimeList() {
        //查询
        Cursor mCursor = readSqlDatabase.query(false,
                DatabaseHelper.UpdateDistanceTimeColumns.TABLE_NAME,
                new String[]{DatabaseHelper.UpdateDistanceTimeColumns._ID,
                            DatabaseHelper.UpdateDistanceTimeColumns.LOC_TIME,
                            DatabaseHelper.UpdateDistanceTimeColumns.DISTANCE_NUM }

                , null, null, null, null, null, null);
        return  getUpDisTimes(mCursor);
    }

    public List<UpDisTime> queryUpDisTimeList(String time) {
        //查询
        Cursor mCursor = readSqlDatabase.query(false,
                DatabaseHelper.UpdateDistanceTimeColumns.TABLE_NAME,
                new String[]{DatabaseHelper.UpdateDistanceTimeColumns._ID,
                        DatabaseHelper.UpdateDistanceTimeColumns.LOC_TIME,
                        DatabaseHelper.UpdateDistanceTimeColumns.DISTANCE_NUM }

                , DatabaseHelper.UpdateDistanceTimeColumns.LOC_TIME +"= ?", new String[] {time}, null, null, null, null);
        return getUpDisTimes(mCursor);
    }

    @NonNull
    private List<UpDisTime> getUpDisTimes(Cursor mCursor) {
        List<UpDisTime> list = new ArrayList<>();
        try {
            while (mCursor.moveToNext()) {
                UpDisTime upDisTime = new UpDisTime();
                upDisTime.setId(mCursor.getLong(mCursor.getColumnIndex(DatabaseHelper.UpdateDistanceTimeColumns._ID)));
                upDisTime.setDistanceNum(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.UpdateDistanceTimeColumns.DISTANCE_NUM)));
                upDisTime.setLocTime(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.UpdateDistanceTimeColumns.LOC_TIME)));
                list.add(upDisTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCursor != null) {
                mCursor.close();
            }
        }
        return list;
    }

    public List<DistanceLoc> queryDistanceLocList(String time) {
        //查询
        Cursor mCursor = readSqlDatabase.query(false,
                DatabaseHelper.DistanceLocColumns.TABLE_NAME,
                new String[]{DatabaseHelper.DistanceLocColumns._ID,
                        DatabaseHelper.DistanceLocColumns.LOC_TIME,
                        DatabaseHelper.DistanceLocColumns.LOC_TEXT }

                , DatabaseHelper.DistanceLocColumns.LOC_TIME +" = ?", new String[] {time}, null, null, null, null);
        List<DistanceLoc> list = new ArrayList<>();

        try {
            while (mCursor.moveToNext()) {
                DistanceLoc distanceLoc = new DistanceLoc();
                distanceLoc.setId(mCursor.getLong(mCursor.getColumnIndex(DatabaseHelper.DistanceLocColumns._ID)));
                distanceLoc.setLocText(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.DistanceLocColumns.LOC_TEXT)));
                distanceLoc.setLocTime(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.DistanceLocColumns.LOC_TIME)));
                list.add(distanceLoc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCursor != null) {
                mCursor.close();
            }
        }
        return list;
    }

    public boolean deleteUpDisTime(long id) {
        return writeSqlDatabase.delete(DatabaseHelper.UpdateDistanceTimeColumns.TABLE_NAME,
                DatabaseHelper.UpdateDistanceTimeColumns._ID +" = ?", new String[] { String.valueOf(id) }) > 0;
    }

    public boolean deleteDistanceLoc(String time) {
        return writeSqlDatabase.delete(DatabaseHelper.DistanceLocColumns.TABLE_NAME,
                DatabaseHelper.DistanceLocColumns.LOC_TIME + " = ?",
                new String[] { String.valueOf(time) }) > 0;
    }

    public boolean updateUpDisTime(UpDisTime upDisTime) {
        if (upDisTime == null) {
            return false;
        }
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.UpdateDistanceTimeColumns.DISTANCE_NUM, upDisTime.getDistanceNum());
            Log.i("TEST" ,"============================> upDisTime.getDistanceNum =" +upDisTime.getDistanceNum());
            return writeSqlDatabase.update(DatabaseHelper.UpdateDistanceTimeColumns.TABLE_NAME, contentValues,
                    DatabaseHelper.UpdateDistanceTimeColumns._ID + "=" + upDisTime.getId(), null) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
