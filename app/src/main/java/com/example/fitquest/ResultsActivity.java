package com.example.fitquest;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        TextView durationTextView = findViewById(R.id.durationTextView);
        TextView stepsTextView = findViewById(R.id.stepsTextView);
        TextView caloriesTextView = findViewById(R.id.caloriesTextView);
        TextView distanceTextView = findViewById(R.id.distanceTextView);

        // Get the passed values
        durationTextView.setText(getIntent().getStringExtra("Duration"));
        stepsTextView.setText(getIntent().getStringExtra("Steps"));
        caloriesTextView.setText(getIntent().getStringExtra("Calories"));
        distanceTextView.setText(getIntent().getStringExtra("Distance"));
    }

}
