package com.example.fitquest;

import android.annotation.SuppressLint;

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
    private EditText editText1, editText2, editText3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_layout);

        mAuth = FirebaseAuth.getInstance();
        editText1 = findViewById(R.id.edit_id1);
        editText2 = findViewById(R.id.edit_id2);
        editText3 = findViewById(R.id.edit_id3);

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void sendData(View view) {
        String email = editText2.getText().toString();
        String password = editText3.getText().toString();
        String name = editText1.getText().toString();

        // Validate email and password
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email or password cannot be empty", Toast.LENGTH_SHORT).show();
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
                            // Proceed to the next activity or perform any additional actions
                            // For example, you might want to redirect the user to the login page
                        } else {
                            // If sign up fails, display a message to the user.
                            Toast.makeText(SignupActivity.this, "Failed to create account.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        writeNewUser();

    }

    public void writeNewUser() {
        User user = new User(editText1.getText().toString(),
                editText2.getText().toString(), editText3.getText().toString());

        mDatabase.child("users").child(user.getName()).setValue(user);
    }
}