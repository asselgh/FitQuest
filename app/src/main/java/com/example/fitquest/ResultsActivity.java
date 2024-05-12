package com.example.fitquest;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ResultsActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String userEmail;
    private String workoutType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Retrieve the userEmail and workoutType from intent extras
        userEmail = getIntent().getStringExtra("user_email");
        workoutType = getIntent().getStringExtra("workout_type");

        TextView durationTextView = findViewById(R.id.durationTextView);
        TextView stepsTextView = findViewById(R.id.stepsTextView);
        TextView caloriesTextView = findViewById(R.id.caloriesTextView);
        TextView distanceTextView = findViewById(R.id.distanceTextView);

        // Get the passed values
        String duration = getIntent().getStringExtra("Duration");
        String steps = getIntent().getStringExtra("Steps");
        String calories = getIntent().getStringExtra("Calories");
        String distance = getIntent().getStringExtra("Distance");

        String stepsNumric = extractNumber(steps); // Extract only number from steps
        String caloriesNumric = extractNumber(calories); // Extract only number from calories
        String distanceNumric = extractNumber(distance); // Extract only number from distance

        storeDataInFirebase(duration, stepsNumric, caloriesNumric, distanceNumric);

        durationTextView.setText(duration);
        stepsTextView.setText(steps);
        caloriesTextView.setText(calories);
        distanceTextView.setText(distance);

    }

    private String extractNumber(String text) {
        if (text != null) {
            return text.replaceAll("\\D+", ""); // Remove all non-digits from the string
        } else {
            return "0"; // Handle null case with default value
        }
    }

    private void storeDataInFirebase(String duration, String steps, String calories, String distance) {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        // Reference to the users node
        DatabaseReference usersRef = mDatabase.child("users");

        // Query to find the user node based on email
        Query query = usersRef.orderByChild("email").equalTo(userEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the user node
                    DataSnapshot userSnapshot = dataSnapshot.getChildren().iterator().next();
                    String userId = userSnapshot.getKey();

                    // Reference to the workouts node under the user node
                    DatabaseReference workoutsRef = usersRef.child(userId).child("workouts");

                    // Create a new node for the current date and time to store workout data
                    DatabaseReference workoutRef = workoutsRef.child(currentDate + " - " + currentTime);

                    // Set values for workout data (numeric values only)
                    workoutRef.child("workout_type").setValue(workoutType);
                    workoutRef.child("date_time").setValue(currentDate + " " + currentTime);
                    workoutRef.child("duration").setValue(duration);
                    workoutRef.child("steps").setValue(Integer.parseInt(steps)); // Convert string to int for storage
                    workoutRef.child("calories_burned").setValue(Integer.parseInt(calories));  // Convert string to int for storage
                    workoutRef.child("distance").setValue(Double.parseDouble(distance)); // Convert string to double for storage
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }
}
