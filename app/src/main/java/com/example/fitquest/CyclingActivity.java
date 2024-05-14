package com.example.fitquest;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.database.sqlite.SQLiteDatabase;

public class CyclingActivity extends AppCompatActivity implements LocationListener {

    private TextView timerTextView, distanceTextView, caloriesTextView;
    private Button startPauseButton, pauseButton, endButton, finishButton;
    private LocationManager locationManager;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private long startTime = 0;
    private boolean isRunning = false;
    private float totalDistance = 0;
    private Location lastLocation = null;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final float CALORIES_PER_KM = 50; // Average calories burned per kilometer for cycling
    private String userEmail;
    private String workoutType = "Cycling";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cycling);

        timerTextView = findViewById(R.id.timerTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        caloriesTextView = findViewById(R.id.caloriesTextView);
        startPauseButton = findViewById(R.id.startPauseButton);
        pauseButton = findViewById(R.id.pauseButton);
        endButton = findViewById(R.id.endButton);
        finishButton = findViewById(R.id.finishButton);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        checkLocationPermission();

        userEmail = getIntent().getStringExtra("user_email");


        startPauseButton.setOnClickListener(v -> {
            if (!isRunning) {
                startCycling();
            } else {
                pauseCycling();
            }
        });

        pauseButton.setOnClickListener(v -> pauseCycling());

        endButton.setOnClickListener(v -> endCycling());

        timerRunnable = new Runnable() {
            public void run() {
                if (isRunning) {
                    long millis = SystemClock.elapsedRealtime() - startTime;
                    int seconds = (int) (millis / 1000);
                    int minutes = seconds / 60;
                    seconds = seconds % 60;
                    int hours = minutes / 60;
                    minutes = minutes % 60;

                    timerTextView.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

                    timerHandler.postDelayed(this, 500);
                }
            }
        };

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Store the workout data in the database
                storeWorkoutData();

                // Create an intent to start ResultsActivity
                Intent intent = new Intent(CyclingActivity.this, ResultsActivity.class);
                intent.putExtra("workout_type", workoutType);
                intent.putExtra("user_email", userEmail); // Pass userEmail to ResultsActivity
                startActivity(intent);

                // Optionally finish the current activity if you no longer need it
                finish();
            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, this);
        }
    }

    private void startCycling() {
        isRunning = true;
        startPauseButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
        endButton.setVisibility(View.VISIBLE);
        finishButton.setVisibility(View.VISIBLE);
        startTime = SystemClock.elapsedRealtime();
        timerHandler.postDelayed(timerRunnable, 0);
        lastLocation = null;
        totalDistance = 0;

        createStartNotification();
    }

    private void pauseCycling() {
        isRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
        startPauseButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
    }

    private void endCycling() {
        isRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
        locationManager.removeUpdates(this);
        createFinishNotification();
        resetUI();
    }

    private void resetUI() {
        timerTextView.setText("00:00:00");
        distanceTextView.setText("Distance: 0 km");
        caloriesTextView.setText("Calories Burned: 0 kcal");
        startPauseButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
        endButton.setVisibility(View.GONE);
        finishButton.setVisibility(View.GONE);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isRunning && lastLocation != null) {
            float distance = lastLocation.distanceTo(location) / 1000.0f; // Convert meters to kilometers
            totalDistance += distance;
            distanceTextView.setText(String.format("Distance: %.2f km", totalDistance));
            calculateCalories(totalDistance);
        }
        lastLocation = location;
    }

    private void calculateCalories(float distance) {
        float caloriesBurned = distance * CALORIES_PER_KM;
        caloriesTextView.setText(String.format("Calories Burned: %.1f kcal", caloriesBurned));
    }


    private void createStartNotification() {
        Notification.Builder builder = new Notification.Builder(this, "fitquest_channel")
                .setContentTitle("FitQuest")
                .setContentText("Your cycling session has started")
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    private void createFinishNotification() {
        String distanceString = String.format("%.2f", totalDistance); // Using the totalDistance variable
        String caloriesString = caloriesTextView.getText().toString(); // Using the text from the caloriesTextView

        String notificationMessage = "Finished cycling session, Distance: " + distanceString + " km, Calories: " + caloriesString;

        Notification.Builder builder = new Notification.Builder(this, "fitquest_channel")
                .setContentTitle("FitQuest")
                .setContentText(notificationMessage)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(2, builder.build());
    }

    private void storeWorkoutData() {
        MyDBHelper dbHelper = new MyDBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Extracting numbers from the text views
        String durationText = timerTextView.getText().toString();
        String caloriesText = caloriesTextView.getText().toString();
        String distanceText = distanceTextView.getText().toString();

        // Extracting numbers from the strings
        int duration = extractNumber(durationText);
        float calories = extractFloatNumber(caloriesText);
        float distance = extractFloatNumber(distanceText);

        // Inserting data into the "workouts" table
        ContentValues values = new ContentValues();
        values.put("workout_type", workoutType);
        values.put("duration", duration);
        values.put("calories", calories);
        values.put("distance", distance);
        long newRowId = db.insert("workouts", null, values);

        if (newRowId == -1) {
            // Insertion failed
            Toast.makeText(this, "Error storing workout data in the database", Toast.LENGTH_SHORT).show();
        } else {
            // Insertion successful
            Toast.makeText(this, "Workout data stored successfully", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

    // Helper method to extract integer from string
    private int extractNumber(String text) {
        String number = text.replaceAll("[^\\d]", ""); // Extract digits
        return Integer.parseInt(number);
    }

    // Helper method to extract float from string
    private float extractFloatNumber(String text) {
        String number = text.replaceAll("[^\\d.]", ""); // Extract digits and dot
        return Float.parseFloat(number);
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
            } else {
                Toast.makeText(this, "Location permission is needed for tracking your cycling activity", Toast.LENGTH_LONG).show();
            }
        }
    }
}
