package com.example.pickacard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class AboutUs_Page extends AppCompatActivity {
    ImageButton backarrow;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us_page);

        backarrow = findViewById(R.id.backarrow);


        backarrow.setOnClickListener(v -> onBackPressed());


    }

    public void open(View view) {
        Uri picAcard= Uri.parse("https://drive.google.com/file/d/13Kz9K7wreVHn0zY11fR77-7sTXlm-p8H/view?usp=drivesdk");
        Intent i=new Intent(Intent.ACTION_VIEW,picAcard);
        startActivity(i);


}


}