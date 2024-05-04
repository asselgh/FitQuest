package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class StatusActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statues);

        // Initialize ImageView for sharing calories
        ImageView imgShareCalories = findViewById(R.id.imgShareCalories);
        imgShareCalories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareContent("I've burned XYZ calories today! #FitnessTracker");
            }
        });

        // Initialize ImageView for sharing milestones
        ImageView imgShareMilestones = findViewById(R.id.imgShareMilestones);
        imgShareMilestones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareContent("I've reached my milestones today! #FitnessGoals");
            }
        });

        // Initialize ImageView for sharing distance
        ImageView imgShareDistance = findViewById(R.id.imgShareDistance);
        imgShareDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareContent("I've covered XYZ distance today! #HealthyLiving");
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
}
