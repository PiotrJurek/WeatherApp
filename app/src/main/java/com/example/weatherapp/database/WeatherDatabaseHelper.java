package com.example.weatherapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WeatherDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weather.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_WEATHER = "weather";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_DATA = "data";
    public static final String COLUMN_LAST_UPDATED = "last_updated";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_WEATHER + " (" +
                    COLUMN_CITY + " TEXT PRIMARY KEY, " +
                    COLUMN_DATA + " TEXT, " +
                    COLUMN_LAST_UPDATED + " INTEGER" +
                    ");";

    public WeatherDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_WEATHER + " ADD COLUMN " + COLUMN_LAST_UPDATED + " INTEGER");
        }
    }
}
