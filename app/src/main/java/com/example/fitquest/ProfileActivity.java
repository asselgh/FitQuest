package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Assume the ImageView ID for the share button is imgShareProfile
        ImageView imgShareProfile = findViewById(R.id.imgShareAchievements);
        imgShareProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareProfileInfo();
            }
        });
    }

    private void shareProfileInfo() {
        // Define the message you want to share
        String message = "I have achieved XYZ goals and earned ABC badges! #FitQuest";

        // Create the intent to share content
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);

        // Start the chooser activity to let the user select an app to share through
        startActivity(Intent.createChooser(shareIntent, "Share your profile"));
    }
}
