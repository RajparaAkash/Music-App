package com.example.musicdemo.Utils.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.google.gson.Gson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String COL_0 = "date";
    private static final String COL_1 = "orderType";
    private static final String COL_2 = "status";
    private static final String COL_3 = "inputFilePath";
    private static final String COL_4 = "musicOutputPath";
    private static final String COL_5 = "duration";
    private static final String COL_6 = "cmd";
    public static final String LOG_TAG = "DatabaseHelper";
    private static final String ORDERS_TABLE_NAME = "converted_videos";
    private Context context;
    private List<DatabaseEventListener> databaseEventListeners;
    private Gson gson;

    
    public interface DatabaseEventListener {
        void onDatabaseInsert();

        void onDatabaseUpdate();
    }

    public void cleanOldOrders() {
    }

    public DatabaseHelper(Context context) {
        super(context, ORDERS_TABLE_NAME, (SQLiteDatabase.CursorFactory) null, 1);
        this.databaseEventListeners = new ArrayList();
        this.context = context;
        this.gson = new Gson();
    }

    public void setDatabaseEventListener(DatabaseEventListener databaseEventListener) {
        this.databaseEventListeners.add(databaseEventListener);
    }

    @Override
    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE converted_videos (date DATETIME DEFAULT CURRENT_TIMESTAMP, orderType INTEGER, status INTEGER, inputFilePath TEXT, musicOutputPath TEXT UNIQUE, duration INTEGER, cmd TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS converted_videos");
        onCreate(sQLiteDatabase);
    }

    public void cancelOpenOrders() {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_0, getDateTime());
        contentValues.put("status", (Integer) 3);
        writableDatabase.update(ORDERS_TABLE_NAME, contentValues, "status = ? OR status = ?", new String[]{Integer.toString(2), Integer.toString(1)});
        callListenersDbItemUpdate();
    }

    public boolean saveOrder(FFmpegConverter.ConversionOrder conversionOrder) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_0, getDateTime());
        contentValues.put(COL_1, Integer.valueOf(conversionOrder.type));
        contentValues.put("status", Integer.valueOf(conversionOrder.status));
        contentValues.put(COL_3, conversionOrder.inputFilePath);
        contentValues.put(COL_4, conversionOrder.musicResultPath);
        contentValues.put("duration", Long.valueOf(conversionOrder.duration));
        contentValues.put(COL_6, cmdToString(conversionOrder.cmd));
        long update = orderExists(conversionOrder) ? writableDatabase.update(ORDERS_TABLE_NAME, contentValues, "musicOutputPath = ?", new String[]{conversionOrder.musicResultPath}) : writableDatabase.insert(ORDERS_TABLE_NAME, null, contentValues);
        if (new File(conversionOrder.musicResultPath).exists()) {
            callListenersDbItemUpdate();
        } else {
            callListenersDbItemInsert();
        }
        return update != -1;
    }

    public List<FFmpegConverter.ConversionOrder> getOrders(boolean z) {
        ArrayList arrayList = new ArrayList();
        Cursor data = getData();
        if (data.moveToFirst()) {
            do {
                int i = data.getInt(1);
                int i2 = data.getInt(2);
                if (!z || i2 == 2 || i2 == 1) {
                    String string = data.getString(3);
                    String string2 = data.getString(4);
                    int i3 = data.getInt(5);
                    String string3 = data.getString(6);
                    FFmpegConverter.ConversionOrder conversionOrder = new FFmpegConverter.ConversionOrder();
                    conversionOrder.inputFilePath = string;
                    conversionOrder.musicResultPath = string2;
                    conversionOrder.duration = i3;
                    File file = new File(string);
                    File file2 = new File(string2);
                    conversionOrder.filename = file2.getName();
                    conversionOrder.status = 2;
                    conversionOrder.type = i;
                    conversionOrder.cmd = stringToCmdArray(string3);
                    if (file.exists()) {
                        arrayList.add(0, conversionOrder);
                    }
                    if (i2 == 1 && file2.exists()) {
                        file2.delete();
                        this.context.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(file2)));
                    }
                }
            } while (data.moveToNext());
            data.close();
        }
        return arrayList;
    }

    public FFmpegConverter.ConversionOrder getOrderFromOutputPath(String str) {
        Cursor rawQuery = getReadableDatabase().rawQuery("SELECT * FROM converted_videos WHERE musicOutputPath = ?", new String[]{str});
        FFmpegConverter.ConversionOrder conversionOrder = new FFmpegConverter.ConversionOrder();
        if (rawQuery.moveToFirst()) {
            do {
                int i = rawQuery.getInt(1);
                int i2 = rawQuery.getInt(2);
                String string = rawQuery.getString(3);
                String string2 = rawQuery.getString(4);
                int i3 = rawQuery.getInt(5);
                String string3 = rawQuery.getString(6);
                conversionOrder.inputFilePath = string;
                conversionOrder.musicResultPath = string2;
                conversionOrder.duration = i3;
                conversionOrder.filename = new File(string2).getName();
                conversionOrder.status = i2;
                conversionOrder.type = i;
                conversionOrder.cmd = stringToCmdArray(string3);
            } while (rawQuery.moveToNext());
            rawQuery.close();
        }
        return conversionOrder;
    }

    private String cmdToString(String[] strArr) {
        return this.gson.toJson(strArr);
    }

    private String[] stringToCmdArray(String str) {
        return (String[]) this.gson.fromJson(str, (Class) String[].class);
    }

    private String getDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    public int getVideoStatus(FFmpegConverter.ConversionOrder conversionOrder) {
        return getVideoStatus(conversionOrder.filename);
    }

    public int getVideoStatus(String str) {
        Cursor data = getData();
        if (data.moveToFirst()) {
            do {
                if (data.getInt(1) == 0 && str.equals(data.getString(3))) {
                    return data.getInt(2);
                }
            } while (data.moveToNext());
            data.close();
            return -1;
        }
        return -1;
    }

    private boolean orderExists(FFmpegConverter.ConversionOrder conversionOrder) {
        Cursor data = getData();
        if (data.moveToFirst()) {
            do {
                String string = data.getString(3);
                String string2 = data.getString(4);
                if (conversionOrder.inputFilePath.equals(string) && string2.equals(conversionOrder.musicResultPath)) {
                    return true;
                }
            } while (data.moveToNext());
            data.close();
            return false;
        }
        return false;
    }

    public Cursor getData() {
        return getReadableDatabase().rawQuery("SELECT * FROM converted_videos ORDER BY date DESC", null);
    }

    public void callListenersDbItemUpdate() {
        for (DatabaseEventListener databaseEventListener : this.databaseEventListeners) {
            databaseEventListener.onDatabaseUpdate();
        }
    }

    public void callListenersDbItemInsert() {
        for (DatabaseEventListener databaseEventListener : this.databaseEventListeners) {
            databaseEventListener.onDatabaseInsert();
        }
    }
}
