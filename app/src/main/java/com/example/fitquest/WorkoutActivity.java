package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class WorkoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        // Find the buttons in the layout
        Button startCyclingBtn = findViewById(R.id.startCyclingBtn);
        Button startRunningBtn = findViewById(R.id.startRunningBtn);
        Button startWalkingBtn = findViewById(R.id.startWalkingBtn);

        // Set OnClickListener for the buttons
        startCyclingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the CyclingActivity
                Intent intent = new Intent(WorkoutActivity.this, CyclingActivity.class);
                startActivity(intent);
            }
        });

        startRunningBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the RunningActivity
                Intent intent = new Intent(WorkoutActivity.this, RunningActivity.class);
                startActivity(intent);
            }
        });

        startWalkingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the WalkingActivity
                Intent intent = new Intent(WorkoutActivity.this, WalkingActivity.class);
                startActivity(intent);
            }
        });
    }
}
