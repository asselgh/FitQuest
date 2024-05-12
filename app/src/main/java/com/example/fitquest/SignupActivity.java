package com.example.fitquest;

import android.annotation.SuppressLint;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.view.View;

public class SignupActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private FirebaseAuth mAuth;
    private EditText editText1, editText2, editText3, editText4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_layout);

        mAuth = FirebaseAuth.getInstance();
        editText1 = findViewById(R.id.edit_id1);
        editText2 = findViewById(R.id.edit_id2);
        editText3 = findViewById(R.id.edit_id3);
        editText4 = findViewById(R.id.edit_id4);


        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void sendData(View view) {
        String name = editText1.getText().toString();
        String email = editText2.getText().toString();
        String password = editText3.getText().toString();
        String passwordConfirm = editText4.getText().toString();

        // Validate email, password, and Username
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields first", Toast.LENGTH_SHORT).show();
            return;
        }
        // Validate email and password
        else if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and Password Cannot Be Empty", Toast.LENGTH_SHORT).show();
            return;
        }
        //  checks if the email written correctly
        else if (!email.contains("@") || !email.contains(".")) {
            Toast.makeText(this, "Email is not valid", Toast.LENGTH_SHORT).show();
            return;
        }
        // Validate the length of the password
        else if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        // validate the the name
        else if (name.isEmpty()) {
            Toast.makeText(this, "Name Cannot Be Empty", Toast.LENGTH_SHORT).show();
            return;
        }
        // validate the length of the name
        else if (name.length() < 3) {
            Toast.makeText(this, "Name must be at least 3 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        //  checks if the password and the confirm password are the same
        else if (!password.equals(passwordConfirm)) {
            Toast.makeText(this, "Password and Confirm Password must be the same", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SignupActivity.this, "Account created successfully.",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();

                            // Write user data to the database
                            writeNewUser(); // Move this line here
                        } else {
                            // If sign up fails, display a message to the user.
                            Toast.makeText(SignupActivity.this, "Failed to create account.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    public void writeNewUser() {
        User user = new User(editText1.getText().toString(),
                editText2.getText().toString(), editText3.getText().toString());

        // Set user data under the generated key
        DatabaseReference userRef = mDatabase.child("users").child(user.getName());
        userRef.setValue(user);

        // Add an empty "workouts" node under the user node
        userRef.child("workouts").setValue(""); // Placeholder value, can be empty string or null
    }

}