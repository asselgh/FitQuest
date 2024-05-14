package com.example.fitquest;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class RunningActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private TextView stepCountTextView, timerTextView, distanceTextView, caloriesTextView;
    private Button startPauseButton, pauseButton, endButton, finishButton;
    private Handler timerHandler = new Handler();
    private long startTime = 0;
    private long elapsedTime = 0;
    private boolean isRunning = false;
    private float stepsCounted = 0;
    private static final float STEP_LENGTH = 0.762f; // Average step length in meters
    private static final float CALORIES_PER_STEP = 0.07f; // Adjusted calorie burn rate per step for running

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);

        // Initialize UI elements
        stepCountTextView = findViewById(R.id.stepsTextView);
        timerTextView = findViewById(R.id.timerTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        caloriesTextView = findViewById(R.id.caloriesTextView);
        startPauseButton = findViewById(R.id.startButton);
        pauseButton = findViewById(R.id.pauseButton);
        endButton = findViewById(R.id.endButton);
        finishButton = findViewById(R.id.finishButton);

        // Get sensor manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // Check step sensor availability
        if (stepSensor == null) {
            Toast.makeText(this, "Step sensor is not available!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set click listeners for buttons
        startPauseButton.setOnClickListener(v -> {
            if (!isRunning) {
                // Start step sensor and timer
                sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
                startTime = SystemClock.elapsedRealtime() - elapsedTime;
                timerHandler.postDelayed(updateTimerThread, 0);
                isRunning = true;
                startPauseButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                endButton.setVisibility(View.VISIBLE);
                finishButton.setVisibility(View.VISIBLE);
                createStartNotification();
            }
        });

        pauseButton.setOnClickListener(v -> {
            // Pause timer and sensor
            elapsedTime = SystemClock.elapsedRealtime() - startTime;
            timerHandler.removeCallbacks(updateTimerThread);
            sensorManager.unregisterListener(this);
            isRunning = false;
            pauseButton.setVisibility(View.GONE);
            startPauseButton.setVisibility(View.VISIBLE);
        });

        endButton.setOnClickListener(v -> {
            // Stop sensor and timer, reset UI
            sensorManager.unregisterListener(this);
            timerHandler.removeCallbacks(updateTimerThread);
            resetUI();
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to start ResultsActivity
                Intent intent = new Intent(RunningActivity.this, ResultsActivity.class);
                intent.putExtra("Duration", timerTextView.getText().toString());
                intent.putExtra("Steps", stepCountTextView.getText().toString());
                intent.putExtra("Calories", caloriesTextView.getText().toString());
                intent.putExtra("Distance", distanceTextView.getText().toString());
                startActivity(intent);

                // Create a notification for the finished running session
                createFinishNotification();
                // Optionally finish the current activity if you no longer need it
                finish();
            }
        });
    }

    private void resetUI() {
        elapsedTime = 0;
        stepsCounted = 0;
        timerTextView.setText("00:00:00");
        stepCountTextView.setText("Steps: 0");
        distanceTextView.setText("Distance: 0 km");
        caloriesTextView.setText("Calories Burned: 0 kcal");
        isRunning = false;
        startPauseButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
        endButton.setVisibility(View.GONE);
        finishButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if (stepsCounted == 0) {
                stepsCounted = event.values[0];
            }
            float steps = event.values[0] - stepsCounted;
            stepCountTextView.setText("Steps: " + (int) steps);
            updateDistanceAndCalories((int) steps);
        }
    }

    private void updateDistanceAndCalories(int steps) {
        float distance = steps * STEP_LENGTH / 1000; // Distance in kilometers
        distanceTextView.setText("Distance: " + String.format("%.2f", distance) + " km");
        float caloriesBurned = steps * CALORIES_PER_STEP;
        caloriesTextView.setText("Calories Burned: " + String.format("%.1f", caloriesBurned) + " kcal");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // This method is required by the SensorEventListener interface; can be left empty if not used.
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

    private void createStartNotification() {
        Notification.Builder builder = new Notification.Builder(this, "fitquest_channel")
                .setContentTitle("FitQuest")
                .setContentText("Your running session has started")
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
        int hours = minutes / 60;
        minutes %= 60;

        String timeString = String.format("%d:%02d:%02d", hours, minutes, seconds);
        String distanceString = distanceTextView.getText().toString();
        String caloriesString = caloriesTextView.getText().toString();

        String notificationMessage = "Finished running session at " + timeString +
                ", " + distanceString +
                ", " + caloriesString;

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
        if (isRunning) {
            sensorManager.unregisterListener(this);
            elapsedTime = SystemClock.elapsedRealtime() - startTime;
            timerHandler.removeCallbacks(updateTimerThread);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRunning) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_UI);
            startTime = SystemClock.elapsedRealtime() - elapsedTime;
            timerHandler.postDelayed(updateTimerThread, 0);
        }
    }
}
