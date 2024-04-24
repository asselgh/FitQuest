package com.example.fitquest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class WalkingActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private TextView stepCountTextView, timerTextView, distanceTextView, caloriesTextView;
    private Button startButton, pauseButton, endButton;
    private Handler timerHandler = new Handler();
    private long startTime = 0;
    private long elapsedTime = 0;
    private boolean isRunning = false;
    private boolean isSensorPresent = false;
    private float stepsCounted = 0;
    private static final int ACTIVITY_RECOGNITION_PERMISSION = 1;
    private static final float STEP_LENGTH = 0.762f;  // Average step length in meters
    private static final float AVERAGE_WEIGHT_KG = 70.0f; // Average weight in kg for calorie calculation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking);

        // Initialize UI elements
        stepCountTextView = findViewById(R.id.stepsTextView);
        timerTextView = findViewById(R.id.timerTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        caloriesTextView = findViewById(R.id.caloriesTextView);
        startButton = findViewById(R.id.startButton);
        pauseButton = findViewById(R.id.pauseButton);
        endButton = findViewById(R.id.endButton);

        // Get sensor manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Request permission to access activity recognition
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, ACTIVITY_RECOGNITION_PERMISSION);
        } else {
            // If permission granted, initialize step counter
            initStepCounter();
        }

        // Set click listeners for buttons
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start step sensor and timer
                registerStepSensor();
                startTime = SystemClock.elapsedRealtime() - elapsedTime;
                timerHandler.postDelayed(updateTimerThread, 0);
                isRunning = true;
                startButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                endButton.setVisibility(View.VISIBLE);
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pause timer
                elapsedTime = SystemClock.elapsedRealtime() - startTime;
                timerHandler.removeCallbacks(updateTimerThread);
                isRunning = false;
                pauseButton.setVisibility(View.GONE);
                startButton.setVisibility(View.VISIBLE);
            }
        });

        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop sensor and timer, reset UI
                timerHandler.removeCallbacks(updateTimerThread);
                sensorManager.unregisterListener(WalkingActivity.this);
                elapsedTime = 0;
                timerTextView.setText("00:00:00");
                stepCountTextView.setText("Steps: 0");
                distanceTextView.setText("Distance: 0 m");
                caloriesTextView.setText("Calories Burned: 0 kcal");
                isRunning = false;
                startButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.GONE);
                endButton.setVisibility(View.GONE);
                stepsCounted = 0;
            }
        });
    }

    private void initStepCounter() {
        // Check if step counter sensor is available
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor != null) {
            isSensorPresent = true;
            Toast.makeText(this, "Step sensor is available!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Step sensor is not available!", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerStepSensor() {
        // Register step sensor listener
        if (isSensorPresent) {
            Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            boolean sensorRegistered = sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
            if (sensorRegistered) {
                Toast.makeText(WalkingActivity.this, "Step counter started.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(WalkingActivity.this, "Failed to start step counter.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Update step count and distance when sensor data changes
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if (stepsCounted == 0) {
                stepsCounted = event.values[0];
            }
            float steps = event.values[0] - stepsCounted;
            stepCountTextView.setText("Steps: " + (int) steps);
            updateDistance((int) steps);
            updateCalories((int) steps);
        }
    }

    private void updateDistance(int steps) {
        // Update distance based on step count
        float distance = steps * STEP_LENGTH;  // Calculate distance
        distanceTextView.setText("Distance: " + (int) distance + " m");
    }

    private void updateCalories(int steps) {
        // Calculate and update calories burned
        float caloriesBurned = steps * 0.04f;  // Simple estimation: 0.04 calories per step
        caloriesTextView.setText("Calories Burned: " + String.format("%.1f", caloriesBurned) + " kcal");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // This method is required by the SensorEventListener interface; you can leave it empty if you are not using it.
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Handle permission request result
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTIVITY_RECOGNITION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initStepCounter();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
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
                timerHandler.postDelayed(this, 500); // Corrected line here
            }
        }
    };


    @Override
    protected void onPause() {
        // Pause sensor and timer when activity is paused
        super.onPause();
        if (isSensorPresent) {
            sensorManager.unregisterListener(this);
        }
        if (isRunning) {
            elapsedTime = SystemClock.elapsedRealtime() - startTime;
            timerHandler.removeCallbacks(updateTimerThread);
        }
    }

    @Override
    protected void onResume() {
        // Resume sensor and timer when activity is resumed
        super.onResume();
        if (isRunning) {
            startTime = SystemClock.elapsedRealtime() - elapsedTime;
            timerHandler.postDelayed(updateTimerThread, 0);
        }
    }

}
