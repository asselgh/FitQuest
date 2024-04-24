package com.example.fitquest;

import android.Manifest;
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

public class CyclingActivity extends AppCompatActivity implements LocationListener {

    private TextView timerTextView, distanceTextView, caloriesTextView;
    private Button startPauseButton, pauseButton, endButton;
    private LocationManager locationManager;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private long startTime = 0;
    private long timeInMilliseconds = 0;
    private boolean isRunning = false;
    private float totalDistance = 0;
    private Location lastLocation = null;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final float CALORIES_PER_KM = 50; // Average calories burned per kilometer for cycling

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

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        checkLocationPermission();

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
        startTime = SystemClock.elapsedRealtime();
        timerHandler.postDelayed(timerRunnable, 0);
        lastLocation = null;
        totalDistance = 0;
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
        resetUI();
    }

    private void resetUI() {
        timerTextView.setText("00:00:00");
        distanceTextView.setText("Distance: 0 km");
        caloriesTextView.setText("Calories Burned: 0 kcal");
        startPauseButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
        endButton.setVisibility(View.GONE);
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

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

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
