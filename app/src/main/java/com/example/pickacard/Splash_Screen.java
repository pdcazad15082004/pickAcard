package com.example.pickacard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class Splash_Screen extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        logo = findViewById(R.id.image_logo);
        firebaseAuth = FirebaseAuth.getInstance();

        Animation zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        logo.startAnimation(zoomIn);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            // ✅ Read session values
            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
            String customId = prefs.getString("customId", null);

            boolean firebaseLoggedIn = (firebaseAuth.getCurrentUser() != null);
            boolean hasCustomId = (customId != null && !customId.trim().isEmpty());

            // ✅ If session not valid -> Login
            if (!firebaseLoggedIn || !hasCustomId) {
                startActivity(new Intent(Splash_Screen.this, Login_Page.class));
                finish();
                return;
            }

            // ✅ Extra safety: confirm user node exists in DB
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(customId)
                    .get()
                    .addOnSuccessListener(snap -> {
                        if (snap.exists()) {
                            startActivity(new Intent(Splash_Screen.this, Main_Screen_Page.class));
                        } else {
                            // user data missing -> force logout and go login
                            FirebaseAuth.getInstance().signOut();
                            prefs.edit().clear().apply();
                            startActivity(new Intent(Splash_Screen.this, Login_Page.class));
                        }
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        startActivity(new Intent(Splash_Screen.this, Login_Page.class));
                        finish();
                    });

        }, 2500);
    }
}
