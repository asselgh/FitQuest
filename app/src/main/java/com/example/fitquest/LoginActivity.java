package com.example.fitquest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private CheckBox rememberMeCheckbox;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        mAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.edit_email); // Corrected ID
        passwordEditText = findViewById(R.id.edit_password); // Corrected ID
        Button loginBtn = findViewById(R.id.loginBtn);
        rememberMeCheckbox = findViewById(R.id.rememberMe); // ID for remember me checkbox
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);

        // Check if remember me is checked and populate email and password fields
        if (sharedPreferences.getBoolean("rememberMe", false)) {
            emailEditText.setText(sharedPreferences.getString("email", ""));
            passwordEditText.setText(sharedPreferences.getString("password", ""));
            rememberMeCheckbox.setChecked(true);
        }

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                boolean rememberMe = rememberMeCheckbox.isChecked();

                if (email.isEmpty() || password.isEmpty()) {
                    // Display a toast message if email or password is empty
                    Toast.makeText(LoginActivity.this, "Email or password cannot be empty!", Toast.LENGTH_SHORT).show();
                } else if (!email.contains("@") || !email.contains(".")) {
                    // Display a toast message if email is not in the correct format
                    Toast.makeText(LoginActivity.this, "Please enter a valid email!", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 6) {
                    // Display a toast message if password is less than 6 characters
                    Toast.makeText(LoginActivity.this, "Wrong Password!", Toast.LENGTH_SHORT).show();
                } else {
                    // If email and password are not empty, proceed with Firebase authentication
                    signInWithEmailAndPassword(email, password, rememberMe);
                }
            }
        });
    }

    private void signInWithEmailAndPassword(String email, String password, final boolean rememberMe) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Authentication success.",
                                    Toast.LENGTH_SHORT).show();
                            // Proceed to the next activity with user email
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.putExtra("user_email", email);
                            startActivity(intent);
                            finish();

                            // Store credentials if remember me is checked
                            if (rememberMe) {
                                sharedPreferences.edit().putString("email", email).apply();
                                sharedPreferences.edit().putString("password", password).apply();
                                sharedPreferences.edit().putBoolean("rememberMe", true).apply(); // Add this line
                            } else {
                                // Clear stored credentials if remember me is unchecked
                                sharedPreferences.edit().remove("email").apply();
                                sharedPreferences.edit().remove("password").apply();
                                sharedPreferences.edit().putBoolean("rememberMe", false).apply(); // Add this line
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
