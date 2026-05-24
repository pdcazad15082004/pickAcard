package com.example.pickacard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class ContactUs_Fragment extends Fragment {

    TextView callIcon, whatsappIcon, linkedinIcon, instagramIcon, gmailIcon,  mapLink;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate layout and get the root view
        View view = inflater.inflate(R.layout.fragment_contact_us_, container, false);

        // Bind views using the inflated view
        callIcon = view.findViewById(R.id.call_icon);
        whatsappIcon = view.findViewById(R.id.whatsapp_icon);
        linkedinIcon = view.findViewById(R.id.linkedin_icon);
        instagramIcon = view.findViewById(R.id.instagram_icon);
        gmailIcon = view.findViewById(R.id.gmail_icon);
        mapLink = view.findViewById(R.id.map_link);

        // Set click listeners
        callIcon.setOnClickListener(v -> openDialer("9121831491"));
        whatsappIcon.setOnClickListener(v -> openWhatsApp("9121831491"));
        linkedinIcon.setOnClickListener(v -> openUrl("https://www.linkedin.com/in/pdc-azad-398247259?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app"));
        instagramIcon.setOnClickListener(v -> openUrl("https://www.instagram.com/pdcazad"));
        gmailIcon.setOnClickListener(v -> openGmail("pdcazad15082004@gmail.com"));
        mapLink.setOnClickListener(v -> openUrl("https://maps.app.goo.gl/7eEk3NVC2C5vS1ud8"));

        return view; // Always return the view here
    }

    private void openDialer(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    private void openWhatsApp(String number) {
        try {
            String url = "https://wa.me/" + number;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openUrl(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void openGmail(String email) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email));
        startActivity(Intent.createChooser(intent, "Send Email"));
    }
}
