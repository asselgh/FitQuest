package com.example.fitquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        mAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.edit_email); // Corrected ID
        passwordEditText = findViewById(R.id.edit_password); // Corrected ID
        Button loginBtn = findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    // Display a toast message if email or password is empty
                    Toast.makeText(LoginActivity.this, "Email or password cannot be empty!", Toast.LENGTH_SHORT).show();
                }
                else if (!email.contains("@") || !email.contains(".")){
                    // Display a toast message if email is not in the correct format
                    Toast.makeText(LoginActivity.this, "Please enter a valid email!", Toast.LENGTH_SHORT).show();
                }
                else if(password.length() < 6){
                    // Display a toast message if password is less than 6 characters
                    Toast.makeText(LoginActivity.this, "Wrong Password!", Toast.LENGTH_SHORT).show();
                }
                    else {
                    // If email and password are not empty, proceed with Firebase authentication
                    signInWithEmailAndPassword(email, password);
                }
            }
        });
    }


    private void signInWithEmailAndPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Authentication success.",
                                    Toast.LENGTH_SHORT).show();
                            // Proceed to the next activity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class); //change this when we create the mainpage
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }
}
