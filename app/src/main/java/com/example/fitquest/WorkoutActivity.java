package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class WorkoutActivity extends AppCompatActivity {

    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        // Retrieve the userEmail from intent extras
        userEmail = getIntent().getStringExtra("user_email");

        // Find the buttons in the layout
        Button startCyclingBtn = findViewById(R.id.startCyclingBtn);
        Button startRunningBtn = findViewById(R.id.startRunningBtn);
        Button startWalkingBtn = findViewById(R.id.startWalkingBtn);

        // Set OnClickListener for the buttons
        startCyclingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the CyclingActivity with userEmail extra
                startActivityWithUserEmail(CyclingActivity.class);
            }
        });

        startRunningBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the RunningActivity with userEmail extra
                startActivityWithUserEmail(RunningActivity.class);
            }
        });

        startWalkingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the WalkingActivity with userEmail extra
                startActivityWithUserEmail(WalkingActivity.class);
            }
        });
    }

    // Method to start activities with userEmail extra
    private void startActivityWithUserEmail(Class<?> cls) {
        Intent intent = new Intent(WorkoutActivity.this, cls);
        intent.putExtra("user_email", userEmail);
        startActivity(intent);
    }
}
