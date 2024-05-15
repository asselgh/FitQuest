package com.example.fitquest;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

public class WalkingActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor stepCounterSensor, accelerometerSensor;
    private TextView stepCountTextView, timerTextView, distanceTextView, caloriesTextView;
    private Button startPauseButton, pauseButton, endButton, finishButton;
    private Handler timerHandler = new Handler();
    private long startTime = 0;
    private long elapsedTime = 0;
    private String userEmail;
    private boolean isRunning = false;
    private float stepsCounted = 0;
    private float lastX, lastY, lastZ;
    private static final float STEP_THRESHOLD = 10.0f; // Threshold for step detection
    private static final float STEP_LENGTH = 0.762f; // Average step length in meters
    private static final float CALORIES_PER_STEP = 0.04f; // Calorie burn rate per step

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking);

        createNotificationChannel();

        stepCountTextView = findViewById(R.id.stepsTextView);
        timerTextView = findViewById(R.id.timerTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        caloriesTextView = findViewById(R.id.caloriesTextView);
        startPauseButton = findViewById(R.id.startButton);
        pauseButton = findViewById(R.id.pauseButton);
        endButton = findViewById(R.id.endButton);
        finishButton = findViewById(R.id.finishButton);

        userEmail = getIntent().getStringExtra("user_email");

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (stepCounterSensor == null || accelerometerSensor == null) {
            Toast.makeText(this, "Required sensors are not available!", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if the sensors are not available
        }

        setButtonListeners();
    }

    private void setButtonListeners() {
        startPauseButton.setOnClickListener(v -> {
            if (!isRunning) {
                startSensors();
                startTimer();
                switchButtons(true);
                createNotification();
            }
        });

        pauseButton.setOnClickListener(v -> {
            stopSensors();
            stopTimer();
            switchButtons(false);
        });

        endButton.setOnClickListener(v -> {
            stopSensors();
            stopTimer();
            resetUI();
        });

        finishButton.setOnClickListener(v -> {
            storeWorkoutData();
            showResults();
            createFinishNotification();
            finish(); // Optionally finish the current activity
        });
    }

    private void startSensors() {
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
    }

    private void stopSensors() {
        sensorManager.unregisterListener(this);
    }

    private void startTimer() {
        startTime = SystemClock.elapsedRealtime() - elapsedTime;
        timerHandler.postDelayed(updateTimerThread, 0);
        isRunning = true;
    }

    private void stopTimer() {
        elapsedTime = SystemClock.elapsedRealtime() - startTime;
        timerHandler.removeCallbacks(updateTimerThread);
        isRunning = false;
    }

    private void switchButtons(boolean running) {
        if (running) {
            startPauseButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
            endButton.setVisibility(View.VISIBLE);
            finishButton.setVisibility(View.VISIBLE);
        } else {
            startPauseButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
            endButton.setVisibility(View.GONE);
            finishButton.setVisibility(View.GONE);

        }
    }

    private void showResults() {
        Intent intent = new Intent(WalkingActivity.this, ResultsActivity.class);
        intent.putExtra("user_email", userEmail);
        intent.putExtra("workout_type", "Walking");
        startActivity(intent);
    }

    private void resetUI() {
        elapsedTime = 0;
        stepsCounted = 0;
        timerTextView.setText("00:00:00");
        stepCountTextView.setText("Steps: 0");
        distanceTextView.setText("Distance: 0 km");
        caloriesTextView.setText("Calories Burned: 0 kcal");
        switchButtons(false);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            handleStepCounter(event);
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            handleAccelerometer(event);
        }
    }

    private void handleStepCounter(SensorEvent event) {
        if (stepsCounted == 0) {
            stepsCounted = event.values[0];
        }
        float steps = event.values[0] - stepsCounted;
        updateUI((int) steps);
    }

    private void handleAccelerometer(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        if (Math.abs(x - lastX) > STEP_THRESHOLD || Math.abs(y - lastY) > STEP_THRESHOLD || Math.abs(z - lastZ) > STEP_THRESHOLD) {
            stepsCounted++;
            updateUI(1); // Update UI for each detected step
        }

        lastX = x;
        lastY = y;
        lastZ = z;
    }

    private void updateUI(int steps) {
        stepCountTextView.setText("Steps: " + steps);
        updateDistanceAndCalories(steps);
    }

    private void updateDistanceAndCalories(int steps) {
        float distance = steps * STEP_LENGTH / 1000; // Distance in kilometers
        distanceTextView.setText("Distance: " + String.format("%.2f", distance) + " km");
        float caloriesBurned = steps * CALORIES_PER_STEP;
        caloriesTextView.setText("Calories Burned: " + String.format("%.1f", caloriesBurned) + " kcal");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used currently
    }

    private Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                long now = SystemClock.elapsedRealtime();
                long millis = now - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds %= 60;
                int hours = minutes / 60;
                minutes %= 60;
                timerTextView.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                timerHandler.postDelayed(this, 500);
            }
        }
    };

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "fitquest_channel",
                    "FitQuest Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Channel for FitQuest app notifications");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotification() {
        Notification.Builder builder = new Notification.Builder(this, "fitquest_channel")
                .setContentTitle("FitQuest")
                .setContentText("Your walking session has started")
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    private void createFinishNotification() {
        long elapsedMillis = SystemClock.elapsedRealtime() - startTime;
        int seconds = (int) (elapsedMillis / 1000);
        int minutes = seconds / 60;
        seconds %= 60;

        String timeString = String.format("%02d:%02d", minutes, seconds);
        String distanceString = distanceTextView.getText().toString();
        String caloriesString = caloriesTextView.getText().toString();
        String stepsString = stepCountTextView.getText().toString();

        String notificationMessage = "Finished walking session at " + timeString + ", " + distanceString + ", " + stepsString;

        Notification.Builder builder = new Notification.Builder(this, "fitquest_channel")
                .setContentTitle("FitQuest")
                .setContentText(notificationMessage)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(2, builder.build());
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopSensors();
        stopTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRunning) {
            startSensors();
            startTimer();
        }
    }

    // New method for storing workout data
    private void storeWorkoutData() {
        MyDBHelper dbHelper = new MyDBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Extract data from UI elements
        String durationText = timerTextView.getText().toString();
        String caloriesText = caloriesTextView.getText().toString();
        String distanceText = distanceTextView.getText().toString();
        int steps = Integer.parseInt(stepCountTextView.getText().toString().replaceAll("\\D", "")); // Extract steps


        // Extract numbers from text views
        int duration = extractNumber(durationText);
        float calories = extractFloatNumber(caloriesText);
        float distance = extractFloatNumber(distanceText);

        // Insert data into "workouts" table
        ContentValues values = new ContentValues();
        values.put("workout_type", "Walking");
        values.put("duration", duration);
        values.put("calories", calories);
        values.put("distance", distance);
        values.put("steps", steps);
        values.put("user_email", userEmail); // Add user email
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

    // Helper methods for extracting numbers (same as CyclingActivity)
    private int extractNumber(String text) {
        String number = text.replaceAll("[^\\d]", ""); // Extract digits
        return Integer.parseInt(number);
    }

    private float extractFloatNumber(String text) {
        String number = text.replaceAll("[^\\d.]", ""); // Extract digits and dot
        return Float.parseFloat(number);
    }
}

