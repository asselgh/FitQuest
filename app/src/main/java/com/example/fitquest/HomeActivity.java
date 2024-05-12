package com.example.fitquest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String userEmail;
    private int totalCaloriesBurned = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase Database reference
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        // Retrieve the user's email from the intent extras
        userEmail = getIntent().getStringExtra("user_email");

        // Set the welcome message with the user's name fetched from the database
        final TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        final TextView stepsTextView = findViewById(R.id.stepsTextView);
        final TextView durationTextView = findViewById(R.id.durationTextView);


        // Fetch user name from database
        mDatabase.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            welcomeTextView.setText("Welcome back, " + user.getName());
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });

        // Call retrieveWorkoutsData to fetch workout data
        retrieveWorkoutsData();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                // Currently in Home, no action needed
                return true;
            } else if (itemId == R.id.navigation_workout) {
                // Navigate to the WorkoutActivity with userEmail extra
                startActivityWithUserEmail(WorkoutActivity.class);
                return true;
            } else if (itemId == R.id.navigation_status) {
                // Navigate to the StatusActivity with userEmail extra
                startActivityWithUserEmail(StatusActivity.class);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // Navigate to the ProfileActivity with userEmail extra
                startActivityWithUserEmail(ProfileActivity.class);
                return true;
            }
            return false;
        });

        // Set the default selected item (if necessary)
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    // Method to retrieve workouts data
    private void retrieveWorkoutsData() {
        final TextView caloriesTextView = findViewById(R.id.caloriesTextView);

        // Get today's date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        // Query workouts for today's date
        mDatabase.child(userEmail).child("workouts").orderByChild("date_time").startAt(today).endAt(today + " 23:59:59")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                // Handle potential data type mismatch
                                if (snapshot.hasChild("calories_burned")) {
                                    String caloriesBurnedStr = snapshot.child("calories_burned").getValue(String.class);
                                    if (caloriesBurnedStr != null) {
                                        try {
                                            int caloriesBurned = Integer.parseInt(caloriesBurnedStr);
                                            totalCaloriesBurned += caloriesBurned;
                                        } catch (NumberFormatException e) {
                                            // Handle parsing error
                                            Log.w("HomeActivity", "Failed to parse calories_burned to integer", e);
                                        }
                                    }
                                }
                            }
                            int caloriesRemaining = 500 - totalCaloriesBurned;
                            caloriesTextView.setText(String.format("You have %d calories remaining today", caloriesRemaining));
                        } else {
                            caloriesTextView.setText("No workouts recorded today");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                    }
                });
    }
    // Method to start activities with userEmail extra
    private void startActivityWithUserEmail(Class<?> cls) {
        Intent intent = new Intent(HomeActivity.this, cls);
        intent.putExtra("user_email", userEmail);
        startActivity(intent);
    }
}
