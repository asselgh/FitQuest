package com.example.fitquest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String userEmail;
    private int totalCaloriesBurned = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase Database reference
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        // Retrieve the user's email from the intent extras
        userEmail = getIntent().getStringExtra("user_email");

        // Set the welcome message with the user's name fetched from the database
        final TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        final TextView stepsTextView = findViewById(R.id.stepsTextView);
        final TextView distanceTextView = findViewById(R.id.distanceTextView);
        final TextView caloriesTextView = findViewById(R.id.caloriesTextView);
        final TextView tasksTextView = findViewById(R.id.tasksTextView);
        final TextView pointsTextView = findViewById(R.id.pointsTextView);


        // Fetch user name from database
        mDatabase.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            welcomeTextView.setText("Welcome back, " + user.getName());
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });

        // Calculate steps left for today's goal
        int stepsLeft = calculateStepsLeftForToday();
        int caloriesLeft = calculateCaloriesLeftForToday();
        float distanceLeft = calculateDistanceLeftForToday();

        // if statement to modify tasks and points
        int tasks = 0;
        int points = 0;

        if (stepsLeft == 0) {
            tasks++;
            points += 25;
        }
        if (caloriesLeft == 0) {
            tasks++;
            points += 25;
        }
        if (distanceLeft == 0) {
            tasks++;
            points += 25;
        }

        calculateTasksAndPoints(points);

        tasksTextView.setText("You have completed " + tasks + " out of 3 tasks");
        pointsTextView.setText("You have earned " + points + " today");
        // Update stepsTextView
        stepsTextView.setText("You have " + stepsLeft + " steps left");
        caloriesTextView.setText("You have " + caloriesLeft + " calories left");
        distanceTextView.setText("You have " + distanceLeft + " km left");


        // Initialize bottom navigation view
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                // Currently in Home, no action needed
                return true;
            } else if (itemId == R.id.navigation_workout) {
                // Navigate to the WorkoutActivity with userEmail extra
                startActivityWithUserEmail(WorkoutActivity.class);
                return true;
            } else if (itemId == R.id.navigation_status) {
                // Navigate to the StatusActivity with userEmail extra
                startActivityWithUserEmail(StatusActivity.class);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // Navigate to the ProfileActivity with userEmail extra
                startActivityWithUserEmail(ProfileActivity.class);
                return true;
            }
            return false;
        });

        // Set the default selected item (if necessary)
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    private void calculateTasksAndPoints(int points) {
        if (points == 75) {
            // Initialize DBHelper
            MyDBHelper dbHelper = new MyDBHelper(this);

            // Initialize SQLiteDatabase
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            try {
                // Update statement for points and tasks
                String updateQuery = "UPDATE user SET points = ? WHERE email = ?";

                // Assuming 'db' is your SQLiteDatabase instance
                SQLiteStatement statement = db.compileStatement(updateQuery);

                // Bind parameters
                statement.bindLong(1, points);
                statement.bindString(2, userEmail);

                // Execute the update statement
                statement.executeUpdateDelete();

                // Close the statement
                statement.close();
            } catch (SQLiteConstraintException e) {
                // Handle constraint violation exception
                // For example, if you want to inform the user about the constraint violation:
                Log.e("SQL Update Error", "Constraint violation occurred: " + e.getMessage());
                // You can also handle it by rolling back the transaction or taking any other necessary action.
            }
        }
    }

    // Method to calculate steps left for today's goal
    private int calculateStepsLeftForToday() {

        // Initialize DBHelper
        MyDBHelper dbHelper = new MyDBHelper(this);

        // Initialize SQLiteDatabase
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query to get sum of steps for today's workouts
        String sqlQuery = "SELECT SUM(steps) FROM workouts WHERE user_email = ?";
        Cursor cursor = db.rawQuery(sqlQuery, new String[]{userEmail});
        // Get sum of steps
        int totalSteps = 0;
        if (cursor != null && cursor.moveToFirst()) {
            totalSteps = cursor.getInt(0);
            cursor.close();
        }

        // Close SQLiteDatabase
        db.close();

        // Calculate steps left
        int stepsLeft = 5000 - totalSteps;
        if (stepsLeft < 0) {
            stepsLeft = 0; // Ensure stepsLeft is not negative
        }

        return stepsLeft;
    }

    private int calculateCaloriesLeftForToday() {
        // Initialize DBHelper
        MyDBHelper dbHelper = new MyDBHelper(this);

        // Initialize SQLiteDatabase
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query to get sum of steps for today's workouts
// Construct the SQL query with the WHERE clause
        String sqlQuery = "SELECT SUM(calories) FROM workouts WHERE user_email = ?";
        Cursor cursor = db.rawQuery(sqlQuery, new String[]{userEmail});

        // Get sum of steps
        int calories = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                calories = cursor.getInt(0);
                Log.d("Calories", "Calories fetched from database: " + calories); // Logging the fetched calories
            } else {
                Log.d("Calories", "No calories found for the specified date");
            }
            cursor.close();
        } else {
            Log.e("Calories", "Cursor is null");
        }


        // Close SQLiteDatabase
        db.close();

        // Calculate steps left
        int caloriesLeft = 200 - calories;
        if (caloriesLeft < 0) {
            caloriesLeft = 0; // Ensure stepsLeft is not negative
        }

        return caloriesLeft;
    }

    private float calculateDistanceLeftForToday() {
        // Initialize DBHelper
        MyDBHelper dbHelper = new MyDBHelper(this);

        // Initialize SQLiteDatabase
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query to get sum of steps for today's workouts
        String sqlQuery = "SELECT SUM(distance) FROM workouts WHERE user_email = ?";
        Cursor cursor = db.rawQuery(sqlQuery, new String[]{userEmail});

        // Get sum of steps
        float distance = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                distance = cursor.getFloat(0);
                Log.d("Distance", "Distance fetched from database: " + distance); // Logging the fetched calories
            } else {
                Log.d("Distance", "No Distance found for the specified date");
            }
            cursor.close();
        } else {
            Log.e("Distance", "Cursor is null");
        }


        // Close SQLiteDatabase
        db.close();

        // Calculate steps left
        float distanceLeft = 5 - distance;
        if (distanceLeft < 0) {
            distanceLeft = 0; // Ensure stepsLeft is not negative
        }

        return distanceLeft;
    }

    // Method to start activities with userEmail extra
    private void startActivityWithUserEmail(Class<?> cls) {
        Intent intent = new Intent(HomeActivity.this, cls);
        intent.putExtra("user_email", userEmail);
        startActivity(intent);
    }
}
