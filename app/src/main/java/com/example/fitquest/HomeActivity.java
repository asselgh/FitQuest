package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Handle click on workoutImageView
        ImageView workoutImageView = findViewById(R.id.workoutImageView);
        workoutImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the WorkoutActivity when the workoutImageView is clicked
                Intent intent = new Intent(HomeActivity.this, WorkoutActivity.class);
                startActivity(intent);
            }
        });

        // Handle click on StatsImageView
        ImageView statsImageView = findViewById(R.id.statusImageView);
        statsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the StatsActivity when the statsImageView is clicked
                Intent intent = new Intent(HomeActivity.this, StatusActivity.class);
                startActivity(intent);
            }
        });


    }


}
