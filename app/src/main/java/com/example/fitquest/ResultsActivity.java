package com.example.fitquest;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class ResultsActivity extends AppCompatActivity {

    private MyDBHelper dbHelper;
    private String workoutType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        dbHelper = new MyDBHelper(this);

        // Retrieve the workoutType from intent extra
        workoutType = getIntent().getStringExtra("workout_type");

        TextView durationTextView = findViewById(R.id.durationTextView);
        TextView stepsTextView = findViewById(R.id.stepsTextView);
        TextView caloriesTextView = findViewById(R.id.caloriesTextView);
        TextView distanceTextView = findViewById(R.id.distanceTextView);

        // Get the last stored workout from the SQLite database
        Map<String, Object> lastWorkout = getLastStoredWorkout();

        if (lastWorkout != null) {
            // Display the results in TextViews
            // Get the values from the workout data map
            int durationSeconds = (int) lastWorkout.get("duration");
            int steps = (int) lastWorkout.get("steps");
            float calories = (float) lastWorkout.get("calories");
            float distance = (float) lastWorkout.get("distance");

            // Format duration as "00:00" format
            String formattedDuration = String.format("%02d:%02d", durationSeconds / 60, durationSeconds % 60);

            // Set text for TextViews with prefixed labels
            durationTextView.setText("Duration: " + formattedDuration);
            // Update stepsTextView based on workout_type
            if (!workoutType.equals("Cycling")) {
                stepsTextView.setText("Steps: " + steps);
            } else {
                stepsTextView.setText(""); // Empty text if workout_type is "Cycling"
            }
            caloriesTextView.setText("Calories Burned: " + calories);
            distanceTextView.setText("Distance: " + distance);
        }
    }

    private Map<String, Object> getLastStoredWorkout() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("workouts", null, "workout_type = ?", new String[]{workoutType}, null, null, "_id DESC", "1");

        if (cursor != null && cursor.moveToFirst()) {
            // Create a Map to store workout data
            Map<String, Object> workoutData = new HashMap<>();

            // Extract workout data from the cursor
            workoutData.put("duration", cursor.getInt(cursor.getColumnIndex("duration")));
            workoutData.put("steps", cursor.getInt(cursor.getColumnIndex("steps")));
            workoutData.put("calories", cursor.getFloat(cursor.getColumnIndex("calories")));
            workoutData.put("distance", cursor.getFloat(cursor.getColumnIndex("distance")));

            // Close the cursor and database
            cursor.close();
            db.close();

            // Return the Map containing workout data
            return workoutData;
        } else {
            // No workout found
            if (cursor != null) {
                cursor.close();
            }
            db.close();
            return null;
        }
    }
}
