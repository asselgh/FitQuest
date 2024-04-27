package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                // Currently in Home, no action needed
                return true;
            } else if (itemId == R.id.navigation_workout) {
                // Navigate to the WorkoutActivity
                startActivity(new Intent(HomeActivity.this, WorkoutActivity.class));
                return true;
            } else if (itemId == R.id.navigation_status) {
                // Navigate to the StatusActivity
                startActivity(new Intent(HomeActivity.this, StatusActivity.class));
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // Navigate to the ProfileActivity
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        // Set the default selected item (if necessary)
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }
}
