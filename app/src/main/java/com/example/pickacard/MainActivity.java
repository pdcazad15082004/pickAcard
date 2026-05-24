package com.example.pickacard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       /* firebaseAuth = FirebaseAuth.getInstance();

        findViewById(R.id.logoutBtn).setOnClickListener(v -> {
            firebaseAuth.signOut();
            startActivity(new Intent(this, Login_Page.class));
            finish();
        });*/
    }
}