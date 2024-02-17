package com.example.fitquest;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.view.View;



public class SignupActivity extends AppCompatActivity{

    private DatabaseReference mDatabase;
    private EditText editText1, editText2, editText3;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_layout);

        editText1 = findViewById(R.id.edit_id1);
        editText2 = findViewById(R.id.edit_id2);
        editText3 = findViewById(R.id.edit_id3);


        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void sendData (View view) {
        writeNewUser();
    }

    public void writeNewUser () {
        User user = new User(editText1.getText().toString(),
                editText2.getText().toString(), editText3.getText().toString());

        mDatabase.child("users").child(user.getName()).setValue(user);
    }
}
