package com.example.pickacard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Home_Fragment extends Fragment {

    ImageView phonepeIcon, gpayIcon, paytmIcon, credIcon;
    ImageButton addCardButton;



    private void openBankWebsite(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void openOrRedirectToPlayStore(String packageName) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + packageName));
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException anfe) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            startActivity(intent);
        }
    }

    private void openGPayScannerExperimental() {
        try {
            Intent intent = new Intent();
            intent.setClassName("com.google.android.apps.nbu.paisa.user",
                    "com.google.android.apps.nbu.paisa.user.qrscanning.QrScannerActivity");
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Cannot open GPay scanner", Toast.LENGTH_SHORT).show();
            openOrRedirectToPlayStore("com.google.android.apps.nbu.paisa.user");
        }
    }

    private void openPhonePeScanner() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("phonepe://upi/pay"));
            intent.setPackage("com.phonepe.app");
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "PhonePe not installed", Toast.LENGTH_SHORT).show();
            openOrRedirectToPlayStore("com.phonepe.app");
        }
    }

    private void openPaytmScanner() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("paytmmp://pay"));
            intent.setPackage("net.one97.paytm");
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Paytm not installed", Toast.LENGTH_SHORT).show();
            openOrRedirectToPlayStore("net.one97.paytm");
        }
    }

    private void openCREDApp() {

        PackageManager pm = requireContext().getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage("com.dreamplug.androidapp");
        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            Toast.makeText(getContext(), "CRED not installed", Toast.LENGTH_SHORT).show();
            openOrRedirectToPlayStore("com.dreamplug.androidapp");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Payment App Icons
        addCardButton = view.findViewById(R.id.addCardButton);
        phonepeIcon = view.findViewById(R.id.phonepeIcon);
        gpayIcon = view.findViewById(R.id.gpayIcon);
        paytmIcon = view.findViewById(R.id.paytmIcon);
        credIcon = view.findViewById(R.id.credIcon);

        phonepeIcon.setOnClickListener(v -> openPhonePeScanner());
        gpayIcon.setOnClickListener(v -> openGPayScannerExperimental());
        paytmIcon.setOnClickListener(v -> openPaytmScanner());
        credIcon.setOnClickListener(v -> openCREDApp());

        applyPressAnimation(phonepeIcon);
        applyPressAnimation(gpayIcon);
        applyPressAnimation(paytmIcon);
        applyPressAnimation(credIcon);
        applyPressAnimation(addCardButton);


        addCardButton.setOnClickListener(v -> {
            showTermsAndConditionsDialog();
        });


        // Net Banking Click Listeners
        View sbi = view.findViewById(R.id.sbiBank);
        View hdfc = view.findViewById(R.id.hdfcBank);
        View icici = view.findViewById(R.id.iciciBank);
        View kotak = view.findViewById(R.id.kotakBank);
        View axis = view.findViewById(R.id.axisBank);
        View canara = view.findViewById(R.id.canaraBank);
        View rbl = view.findViewById(R.id.rblBank);
        View indusind = view.findViewById(R.id.indusindBank);

// press animation on all
        applyPressAnimation(sbi);
        applyPressAnimation(hdfc);
        applyPressAnimation(icici);
        applyPressAnimation(kotak);
        applyPressAnimation(axis);
        applyPressAnimation(canara);
        applyPressAnimation(rbl);
        applyPressAnimation(indusind);

// clicks
        sbi.setOnClickListener(v -> logAndOpenBank("SBI", "https://retail.onlinesbi.sbi/retail/login.htm"));
        hdfc.setOnClickListener(v -> logAndOpenBank("HDFC", "https://netbanking.hdfcbank.com"));
        icici.setOnClickListener(v -> logAndOpenBank("ICICI", "https://infinity.icicibank.com/corp/AuthenticationController?FORMSGROUP_ID__=AuthenticationFG&__START_TRAN_FLAG__=Y&FG_BUTTONS__=LOAD&ACTION.LOAD=Y&AuthenticationFG.LOGIN_FLAG=1&BANK_ID=ICI"));
        kotak.setOnClickListener(v -> logAndOpenBank("Kotak", "https://www.kotak.com/"));
        axis.setOnClickListener(v -> logAndOpenBank("Axis", "https://www.axisbank.com/bank-smart/internet-banking/getting-started"));
        canara.setOnClickListener(v -> logAndOpenBank("Canara", "https://online.canarabank.in/?module=login"));
        rbl.setOnClickListener(v -> logAndOpenBank("RBL", "https://online.rblbank.com/corp/AuthenticationController?FORMSGROUP_ID__=AuthenticationFG&__START_TRAN_FLAG__=Y&__FG_BUTTONS__=LOAD&ACTION.LOAD=Y&AuthenticationFG.LOGIN_FLAG=1&BANK_ID=176"));
        indusind.setOnClickListener(v -> logAndOpenBank("IndusInd", "https://indusnet.indusind.com/corp/BANKAWAY?Action.RetUser.Init.001=Y&AppSignonBankId=234&AppType=corporate&CorporateSignonLangId=001"));

    }

    private void showTermsAndConditionsDialog() {
        // Inflate custom layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_terms_conditions, null);

        // Reference views from the custom layout
        TextView termsText = dialogView.findViewById(R.id.termsText);
        CheckBox acceptCheckBox = dialogView.findViewById(R.id.acceptCheckBox);

        // Set formatted HTML terms
        String termsHtml = "<b>1. Card Information Use:</b><br>" +
                "&emsp;Your registered credit/debit card details will be securely stored. Sensitive data like CVV is never stored.<br><br>" +

                "<b>2. Privacy Protection:</b><br>" +
                "&emsp;Card data is encrypted and shown only to you and trusted connections.<br><br>" +

                "<b>3. No Financial Liability:</b><br>" +
                "&emsp;PickACard is not responsible for card use by others. Use at your discretion.<br><br>" +

                "<b>4. Secure Sharing Environment:</b><br>" +
                "&emsp;Never share full card info like CVV and OTP. Only share what's needed for discounts.<br><br>" +

                "<b>5. Goodwill-based System:</b><br>" +
                "&emsp;All exchanges are commission-free and based on mutual trust.<br><br>" +

                "<b>6. Activity Tracking:</b><br>" +
                "&emsp;Your actions like adding cards or accessing NetBanking are securely logged.<br><br>" +

                "<b>7. Platform Awareness:</b><br>" +
                "&emsp;You understand PickACard connects users for card discount sharing and banking convenience.<br><br>" +

                "<b>8. Revoking Access:</b><br>" +
                "&emsp;You can remove or pause your card anytime from being visible to others.<br><br>" +

                "<b>9. Community Guidelines:</b><br>" +
                "&emsp;Misuse leads to account suspension and legal action.<br><br>" +

                "<b>10. Acceptance Required:</b><br>" +
                "&emsp;You must accept these terms to add a card to PickACard.";

        termsText.setText(Html.fromHtml(termsHtml));

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Terms and Conditions");
        builder.setView(dialogView);

        builder.setPositiveButton("Accept", (dialog, which) -> {
            if (acceptCheckBox.isChecked()) {
                Intent intent = new Intent(getActivity(), Add_Card_Activity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Please accept the terms to continue.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Decline", (dialog, which) -> dialog.dismiss());

        // Show and set custom background
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2386EA")));

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);  // "Accept"
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);  // "Decline"
    }



    private void logAndOpenBank(String bankName, String url) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String actionText = "NetBanking opened with " + bankName;
        String iconUrl = IconUtil.getBankIcon(bankName); // Should return icon URL or keyword like 'kotaklogo'

        String customId = requireContext()
                .getSharedPreferences("UserData", getContext().MODE_PRIVATE)
                .getString("customId", null);

        if (customId == null) {
            Toast.makeText(getContext(), "User ID not found", Toast.LENGTH_SHORT).show();
            openBankWebsite(url);
            return;
        }

        DatabaseReference historyRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(customId)
                .child("history");

        historyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                String historyId = "history" + (count + 1);

                Map<String, Object> historyData = new HashMap<>();
                historyData.put("action", actionText);
                historyData.put("time", time);
                historyData.put("icon", iconUrl); // either tag like "kotaklogo" or URL

                historyRef.child(historyId).setValue(historyData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeFragment", "Failed to log net banking", error.toException());
            }
        });

        openBankWebsite(url);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void applyPressAnimation(View view) {

        view.setOnTouchListener((v, event) -> {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    v.animate()
                            .scaleX(0.88f)
                            .scaleY(0.88f)


                            .translationZ(8f) // slight depth
                            .setDuration(120)
                            .setInterpolator(new android.view.animation.DecelerateInterpolator())
                            .start();
                    break;

                case MotionEvent.ACTION_UP:
                    v.animate()
                            .scaleX(1f)


                            .scaleY(1f)
                            .translationZ(0f)
                            .setDuration(180)
                            .setInterpolator(new android.view.animation.OvershootInterpolator(2f))
                            .start();
                    break;

                case MotionEvent.ACTION_CANCEL:
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .translationZ(0f)
                            .setDuration(150)
                            .start();
                    break;
            }

            return false;
        });
    }





}
