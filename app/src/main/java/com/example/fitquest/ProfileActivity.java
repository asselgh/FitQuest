package com.example.fitquest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView weight;
    private TextView height;
    private TextView age;
    private ImageView tenksteps;
    private ImageView twentykms;
    private String userEmail;
    private MyDBHelper dbHelper;
    private TextView rewards;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views after setContentView
        weight = findViewById(R.id.weight);
        height = findViewById(R.id.height);
        age = findViewById(R.id.age);
        tenksteps = findViewById(R.id.tenksteps);
        twentykms = findViewById(R.id.twentykms);
        rewards = findViewById(R.id.rewards);

        // Retrieve the user's email from the intent extras
        userEmail = getIntent().getStringExtra("user_email");

        // Initialize DBHelper
        dbHelper = new MyDBHelper(this);

        // Get total distance and total steps
        float totalDistance = dbHelper.getTotalDistance(userEmail);
        int totalSteps = dbHelper.getTotalSteps(userEmail);

        // Show or hide images based on conditions
        if (totalDistance > 1) {
            twentykms.setVisibility(View.VISIBLE);
        } else {
            twentykms.setVisibility(View.GONE);
        }

        if (totalSteps > 5) {
            tenksteps.setVisibility(View.VISIBLE);
        } else {
            tenksteps.setVisibility(View.GONE);
        }

        // Update reward counter
        int rewardCounter = calculateReward(totalDistance, totalSteps);
        rewards.setText("You have earned " + rewardCounter + " rewards!");

        // Assume the ImageView ID for the share button is imgShareProfile
        ImageView imgShareProfile = findViewById(R.id.imgShareAchievements);
        imgShareProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareProfileInfo(rewardCounter);
            }
        });
    }

    private void shareProfileInfo(int rewardCounter) {
        // Define the message you want to share
        String message = "I have achieved goals and earned " + rewardCounter + " badges! #FitQuest";

        // Create the intent to share content
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);

        // Start the chooser activity to let the user select an app to share through
        startActivity(Intent.createChooser(shareIntent, "Share your profile"));
    }

    // Method to calculate reward based on total distance and total steps
    private int calculateReward(float totalDistance, int totalSteps) {
        // You can define your own logic for calculating the reward based on distance and steps
        int reward = 0;
        if (totalDistance > 1) {
            reward++;
        }
        if (totalSteps > 1) {
            reward++;
        }
        return reward;
    }
}
