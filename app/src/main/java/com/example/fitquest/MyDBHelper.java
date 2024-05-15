package com.example.fitquest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
        // Create the "user" table with the specified fields and constraint
        db.execSQL("CREATE TABLE user (" +
                "email TEXT PRIMARY KEY," +
                "name TEXT," +
                "points INTEGER DEFAULT 0," +
                "height INTEGER DEFAULT 180," +
                "weight INTEGER DEFAULT 75," +
                "age INTEGER DEFAULT 25," +
                "date_time DATE DEFAULT (strftime('%Y-%m-%d', 'now'))," + // Date of last update
                "CONSTRAINT unique_name UNIQUE (name)," + // Constraint to ensure unique name
                "CONSTRAINT unique_date UNIQUE (date_time)" + // Constraint to ensure update once a day
                ")");

        // Create the "workouts" table with the specified fields and foreign key
        db.execSQL("CREATE TABLE workouts (" +
                "_id INTEGER PRIMARY KEY," +
                "workout_type TEXT," +
                "duration INTEGER," +
                "steps INTEGER," +
                "calories INTEGER," +
                "distance FLOAT," +
                "date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "user_email text," + // Foreign key referencing the user table
                "FOREIGN KEY (user_email) REFERENCES user(email)" + // Define foreign key constraint
                ")");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the tables if they exist and then recreate them
        db.execSQL("DROP TABLE IF EXISTS workouts");
        db.execSQL("DROP TABLE IF EXISTS user");
        onCreate(db);
    }

    // Method to insert a new user into the user table
    public long insertUser(String name, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email); // Adding email to ContentValues
        // You can set default values for other fields here if needed
        long newRowId = db.insert("user", null, values);
        db.close();
        return newRowId;
    }

    // Method to get the total sum of calories for a user
    public int getTotalCalories(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(calories) FROM workouts WHERE user_email = ?", new String[]{userEmail});
        int totalCalories = 0;
        if (cursor.moveToFirst()) {
            totalCalories = cursor.getInt(0);
        }
        cursor.close();
        return totalCalories;
    }

    // Method to get the total sum of steps for a user
    public int getTotalSteps(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(steps) FROM workouts WHERE user_email = ?", new String[]{userEmail});
        int totalSteps = 0;
        if (cursor.moveToFirst()) {
            totalSteps = cursor.getInt(0);
        }
        cursor.close();
        return totalSteps;
    }

    // Method to get the total sum of distance for a user
    public float getTotalDistance(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(distance) FROM workouts WHERE user_email = ?", new String[]{userEmail});
        float totalDistance = 0;
        if (cursor.moveToFirst()) {
            totalDistance = cursor.getFloat(0);
        }
        cursor.close();
        return totalDistance;
    }

    public String getUserName(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM user WHERE email = ?", new String[]{userEmail});
        String userName = null;
        if (cursor.moveToFirst()) {
            userName = cursor.getString(0);
        }
        cursor.close();
        return userName;
    }


}

