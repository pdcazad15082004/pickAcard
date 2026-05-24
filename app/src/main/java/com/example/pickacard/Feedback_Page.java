package com.example.pickacard;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Feedback_Page extends AppCompatActivity {

    RatingBar ratingBar;
    EditText feedbackText;

    Button submitBtn, cancelBtn;
    ImageButton backarrow;

    DatabaseReference feedbackRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_page);

        // Initialize UI
        ratingBar = findViewById(R.id.ratingBar);
        feedbackText = findViewById(R.id.feedbackText);
        submitBtn = findViewById(R.id.submitBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        backarrow = findViewById(R.id.backarrow);
        backarrow.setOnClickListener(v -> onBackPressed());

        // Get saved customId from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String customId = prefs.getString("customId", null);

        if (customId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Reference to feedback: Users/<customId>/feedback
        feedbackRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(customId).child("feedback");

        loadUserFeedback();

        // Submit or update feedback
        submitBtn.setOnClickListener(v -> {
            int rating = (int) ratingBar.getRating();
            String message = feedbackText.getText().toString().trim();

            if (TextUtils.isEmpty(message)) {
                Toast.makeText(this, "Please write feedback", Toast.LENGTH_SHORT).show();
                return;
            }

            Feedback feedback = new Feedback(rating, message);

            feedbackRef.setValue(feedback)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Feedback saved", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error saving feedback", Toast.LENGTH_SHORT).show();
                    });
        });

        cancelBtn.setOnClickListener(v -> finish());
    }

    private void loadUserFeedback() {
        feedbackRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Feedback existing = snapshot.getValue(Feedback.class);
                if (existing != null) {
                    ratingBar.setRating(existing.rating);
                    feedbackText.setText(existing.message);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Feedback_Page.this,
                        "Failed to load feedback: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // Feedback model class
    public static class Feedback {
        public int rating;
        public String message;

        public Feedback() {
        }

        public Feedback(int rating, String message) {
            this.rating = rating;
            this.message = message;
        }
    }
}
