package com.example.fitquest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mydatabase.db";
    private static final int DATABASE_VERSION = 1;

    public MyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the "workouts" table with the specified fields
        db.execSQL("CREATE TABLE workouts (" +
                "_id INTEGER PRIMARY KEY," +
                "workout_type TEXT," +
                "duration INTEGER," +
                "steps INTEGER," +
                "calories INTEGER," +
                "distance REAL," +
                "date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the table if it exists and then recreate it
        db.execSQL("DROP TABLE IF EXISTS workouts");
        onCreate(db);
    }
}

