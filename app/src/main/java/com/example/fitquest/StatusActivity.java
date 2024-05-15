package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StatusActivity extends AppCompatActivity {

    private String userEmail;
    private MyDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statues);

        // Retrieve the user's email from the intent extras
        userEmail = getIntent().getStringExtra("user_email");

        // Initialize DBHelper
        dbHelper = new MyDBHelper(this);

        // Fetch and display sum of calories
        TextView tvCaloriesValue = findViewById(R.id.tvCaloriesValue);
        int totalCalories = dbHelper.getTotalCalories(userEmail);
        tvCaloriesValue.setText("Your total burned calories is " + totalCalories);

        // Fetch and display sum of steps
        TextView tvStepsValue = findViewById(R.id.tvstepsValue);
        int totalSteps = dbHelper.getTotalSteps(userEmail);
        tvStepsValue.setText("Your total steps is " + totalSteps);

        // Fetch and display sum of distance
        TextView tvDistanceValue = findViewById(R.id.tvDistanceValue);
        float totalDistance = dbHelper.getTotalDistance(userEmail);
        tvDistanceValue.setText("Your total distance is " + totalDistance + " km");

        // Initialize ImageView for sharing calories
        ImageView imgShareCalories = findViewById(R.id.imgShareCalories);
        imgShareCalories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareContent("I've burned " + totalCalories + " calories today! #FitnessTracker");
            }
        });

        // Initialize ImageView for sharing milestones
        ImageView imgShareMilestones = findViewById(R.id.imgShareMilestones);
        imgShareMilestones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareContent("I've taken " + totalSteps + " steps! #FitnessTracker");
            }
        });

        // Initialize ImageView for sharing distance
        ImageView imgShareDistance = findViewById(R.id.imgShareDistance);
        imgShareDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareContent("I've covered " + totalDistance + " distance! #HealthyLiving");
            }
        });
    }


    private void shareContent(String message) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    @Override
    protected void onDestroy() {
        dbHelper.close(); // Close the database connection when the activity is destroyed
        super.onDestroy();
    }
}
