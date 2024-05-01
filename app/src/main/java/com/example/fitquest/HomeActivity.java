package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase Database reference
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        // Retrieve the user's email from the intent extras
        String userEmail = getIntent().getStringExtra("user_email");

        // Set the welcome message with the user's name fetched from the database
        final TextView welcomeTextView = findViewById(R.id.welcomeTextView);
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
