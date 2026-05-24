package com.example.pickacard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;

import java.io.IOException;
import java.util.Random;

public class Register_Page extends AppCompatActivity {

    // ================= OTP STATE =================
    private String generatedOtp;
    private long otpTime;
    private int otpFailCount;
    private boolean otpVerified;
    private AlertDialog otpDialog;

    private TextView otpError;

    // ================= OTP LOCK =================
    private static final int MAX_FAILS = 3;
    private static final long LOCK_DURATION = 10 * 60 * 1000; // 10 minutes

    private long otpLockUntil = 0L;


    private FrameLayout loadingOverlay;
    private ProgressBar loadingSpinner;
    private ImageView successTick;
    private TextView loadingText;

    private DatabaseReference usersRef;

    private boolean isNameOk = false;
    private boolean isEmailOk = false;
    private boolean isPhoneOk = false;
    private boolean isPasswordOk = false;
    private boolean isConfirmOk = false;

    private String lastEmailChecked = "";
    private String lastPhoneChecked = "";

    private final Handler checkHandler = new Handler(Looper.getMainLooper());
    private Runnable emailCheckRunnable;
    private Runnable phoneCheckRunnable;

    private boolean otpRequestInProgress = false;
    private String otpEmailRequest = "";









    // ================= UI VIEWS =================
    EditText username, gmailid, userphone, passwordedit, cnfpasswordedit;
    Button btnNext, createaccount;
    TextView passwordError, alreadyuser;

    ImageView passMatchIcon;

    TextInputLayout cnfLayout;

    LinearLayout stepOneLayout, stepTwoLayout;


    ProgressBar loadingBar2;






    // ================= FIREBASE =================
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;


    boolean otpExpired = false;
    boolean showEnterOtp = false;

    boolean fromGoogle = false;




    CountDownTimer otpTimer;
    CountDownTimer expiryTimer;
    CountDownTimer hardCloseTimer;
    CountDownTimer resendTimer;

    CountDownTimer sessionTimer;


    // ================= ACTIVITY =================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        initViews();

        otpLockUntil = getSharedPreferences("otp_lock", MODE_PRIVATE)
                .getLong("lock_until", 0L);


        //123
        fromGoogle = getIntent().getBooleanExtra("from_google", false);

        if (fromGoogle) {
            String gEmail = getIntent().getStringExtra("google_email");
            String gName  = getIntent().getStringExtra("google_name");

            gmailid.setText(gEmail);
            username.setText(gName);

            // lock verified data
            gmailid.setEnabled(false);
            username.setEnabled(false);

            // Google already verified email
            otpVerified = true;

            // jump to step 2 UI
            goToStepTwo();
        }


        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        setupLiveValidation();


        setupPasswordWatcher();

        setupClicks();


        String email = getIntent().getStringExtra("prefill_email");
        if (email != null && !email.trim().isEmpty()) {
            gmailid.setText(email);
            gmailid.setSelection(email.length());
            // gmailid.setEnabled(false);  // ❌ REMOVE
        }





    }





    // ================= INIT UI =================
    private void initViews() {
        username = findViewById(R.id.name);
        gmailid = findViewById(R.id.gmail);
        userphone = findViewById(R.id.phone);
        passwordedit = findViewById(R.id.password);
        cnfpasswordedit = findViewById(R.id.cnfpassword);

        btnNext = findViewById(R.id.btnNext);
        createaccount = findViewById(R.id.createaccount);
        passwordError = findViewById(R.id.passwordError);
        alreadyuser = findViewById(R.id.alreadyuser);

        stepOneLayout = findViewById(R.id.stepOneLayout);
        stepTwoLayout = findViewById(R.id.stepTwoLayout);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        successTick = findViewById(R.id.successTick);
        loadingText = findViewById(R.id.loadingText);
        loadingBar2= (ProgressBar) findViewById(R.id.progress2);

        loadingOverlay = findViewById(R.id.loadingOverlay);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        successTick = findViewById(R.id.successTick);
        loadingText = findViewById(R.id.loadingText);
        cnfLayout = findViewById(R.id.cnfLayout);







        setupEditorActions();

        manageFocusStyle(username);
        manageFocusStyle(gmailid);
        manageFocusStyle(userphone);
        manageFocusStyle(passwordedit);
        manageFocusStyle(cnfpasswordedit);

        cnfpasswordedit.setEnabled(false);

        applyPressAnimation(btnNext);
        applyPressAnimation(createaccount);


        btnNext.setEnabled(false);
        createaccount.setEnabled(false);
        btnNext.setAlpha(0.5f);
        createaccount.setAlpha(0.5f);







    }

    private void showAccountCreatedDialog(String emailToPrefill) {

        View v = getLayoutInflater().inflate(R.layout.dialog_account_created, null);

        ImageView close = v.findViewById(R.id.btnClose);
        Button loginBtn = v.findViewById(R.id.btnLogin);

        applyPressAnimation(loginBtn);
        applyPressAnimation(close);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(v)
                .setCancelable(false)
                .create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        // ✅ CLOSE: go to Login WITHOUT prefill
        close.setOnClickListener(view -> {
            dialog.dismiss();
            goToLogin(null);
        });

        // ✅ LOGIN: go to Login WITH prefill
        loginBtn.setOnClickListener(view -> {
            dialog.dismiss();
            goToLogin(emailToPrefill);
        });

        dialog.show();
    }






    private void setupLiveValidation() {

        // NAME
        username.addTextChangedListener(new SimpleWatcher() {
            @Override public void afterTextChanged(Editable s) {
                String name = s.toString().trim();
                isNameOk = !name.isEmpty();
                if (!isNameOk) username.setError("Name required");
                else username.setError(null);
                updateNextButtonState();
            }
        });

        // EMAIL (controls NEXT button)
        gmailid.addTextChangedListener(new SimpleWatcher() {
            @Override public void afterTextChanged(Editable s) {
                if (fromGoogle) return;

                String email = s.toString().trim();

                isEmailOk = false;
                gmailid.setError(null);
                updateNextButtonState();

                if (email.isEmpty()) {
                    gmailid.setError("Email required");
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    gmailid.setError("Invalid email");
                    return;
                }

                if (emailCheckRunnable != null) checkHandler.removeCallbacks(emailCheckRunnable);
                emailCheckRunnable = () -> checkEmailExists(email);
                checkHandler.postDelayed(emailCheckRunnable, 500);
            }
        });

        // PHONE (controls CREATE ACCOUNT button)
        userphone.addTextChangedListener(new SimpleWatcher() {
            @Override public void afterTextChanged(Editable s) {
                String phone = s.toString().trim();

                isPhoneOk = false;
                userphone.setError(null);
                updateCreateButtonState();

                if (!phone.matches("[6-9][0-9]{9}")) {
                    userphone.setError("Invalid phone number");
                    return;
                }

                if (phoneCheckRunnable != null) checkHandler.removeCallbacks(phoneCheckRunnable);
                phoneCheckRunnable = () -> checkPhoneExists(phone);
                checkHandler.postDelayed(phoneCheckRunnable, 500);
            }
        });




    }

    private abstract static class SimpleWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
    }



    private void updateNextButtonState() {
        boolean enable = isNameOk && isEmailOk && !fromGoogle;
        btnNext.setEnabled(enable);
        btnNext.setAlpha(enable ? 1f : 0.5f);
    }

    private void updateCreateButtonState() {
        boolean enable = otpVerified && isPhoneOk && isPasswordOk && isConfirmOk;
        createaccount.setEnabled(enable);
        createaccount.setAlpha(enable ? 1f : 0.5f);
    }




    private void goToLogin(String emailToPrefill) {
        Intent i = new Intent(Register_Page.this, Login_Page.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (emailToPrefill != null && !emailToPrefill.trim().isEmpty()) {
            i.putExtra("prefill_email", emailToPrefill);
        }

        startActivity(i);
        finish();
    }




    private void ensureGoogleSessionOrKickToLogin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) return;

        // If user is null, you're not in a valid Google-authenticated state.
        toast("Google session expired. Please login again.");
        Intent i = new Intent(this, Login_Page.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }






    private void checkEmailExists(String email) {

        lastEmailChecked = email;

        firebaseAuth.fetchSignInMethodsForEmail(email)
                .addOnSuccessListener(result -> {

                    if (!email.equals(lastEmailChecked)) return;

                    boolean authExists = result.getSignInMethods() != null
                            && !result.getSignInMethods().isEmpty();

                    if (authExists) {
                        isEmailOk = false;
                        gmailid.setError("Email already exists");
                        updateNextButtonState();
                        return;
                    }

                    // DB check too
                    usersRef.orderByChild("email").equalTo(email)
                            .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                                @Override
                                public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                                    if (!email.equals(lastEmailChecked)) return;

                                    if (snapshot.exists()) {
                                        isEmailOk = false;
                                        gmailid.setError("Email already exists");
                                    } else {
                                        isEmailOk = true;
                                        gmailid.setError(null);
                                    }
                                    updateNextButtonState();
                                }

                                @Override
                                public void onCancelled(com.google.firebase.database.DatabaseError error) {
                                    if (!email.equals(lastEmailChecked)) return;
                                    isEmailOk = false;
                                    gmailid.setError("Email check failed");
                                    updateNextButtonState();
                                }
                            });

                })
                .addOnFailureListener(e -> {
                    if (!email.equals(lastEmailChecked)) return;
                    isEmailOk = false;
                    gmailid.setError("Email check failed");
                    updateNextButtonState();
                });
    }




    private void checkPhoneExists(String phone) {

        lastPhoneChecked = phone;

        usersRef.orderByChild("mobile").equalTo(phone)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {

                        if (!phone.equals(lastPhoneChecked)) return;

                        if (snapshot.exists()) {
                            isPhoneOk = false;
                            userphone.setError("Phone already exists");
                        } else {
                            isPhoneOk = true;
                            userphone.setError(null);
                        }

                        updateCreateButtonState();
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error) {
                        if (!phone.equals(lastPhoneChecked)) return;

                        isPhoneOk = false;
                        userphone.setError("Phone check failed");
                        updateCreateButtonState();
                    }
                });
    }

    private void hardRecheckEmailThenSendOtp() {

        if (otpRequestInProgress) return;
        otpRequestInProgress = true;

        final String name = username.getText().toString().trim();
        final String email = gmailid.getText().toString().trim();

        otpEmailRequest = email;

        // UI: disable next while checking
        btnNext.setEnabled(false);
        btnNext.setText("");
        loadingBar2.setVisibility(View.VISIBLE);

        // 1) Check FirebaseAuth
        firebaseAuth.fetchSignInMethodsForEmail(email)
                .addOnSuccessListener(result -> {

                    // if user changed email while request running, ignore
                    if (!email.equals(otpEmailRequest)) {
                        resetNextUi();
                        otpRequestInProgress = false;
                        return;
                    }

                    boolean authExists = result.getSignInMethods() != null
                            && !result.getSignInMethods().isEmpty();

                    if (authExists) {
                        gmailid.setError("Email already exists");
                        toast("Email already registered");
                        resetNextUi();
                        otpRequestInProgress = false;
                        return;
                    }

                    // 2) Check Realtime Database
                    usersRef.orderByChild("email").equalTo(email)
                            .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                                @Override
                                public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {

                                    if (!email.equals(otpEmailRequest)) {
                                        resetNextUi();
                                        otpRequestInProgress = false;
                                        return;
                                    }

                                    if (snapshot.exists()) {
                                        gmailid.setError("Email already exists");
                                        toast("Email already used");
                                        resetNextUi();
                                        otpRequestInProgress = false;
                                        return;
                                    }

                                    // ✅ Really new in both places → send OTP
                                    otpRequestInProgress = false;

                                    // restore UI before opening dialog (your sendOtp handles button too)
                                    btnNext.setEnabled(true);
                                    btnNext.setText("Next");
                                    loadingBar2.setVisibility(View.GONE);

                                    sendOtp();
                                }

                                @Override
                                public void onCancelled(com.google.firebase.database.DatabaseError error) {
                                    toast("DB email check failed");
                                    resetNextUi();
                                    otpRequestInProgress = false;
                                }
                            });

                })
                .addOnFailureListener(e -> {
                    toast("Email check failed");
                    resetNextUi();
                    otpRequestInProgress = false;
                });
    }

    private void resetNextUi() {
        btnNext.setEnabled(true);
        btnNext.setText("Next");
        loadingBar2.setVisibility(View.GONE);
    }






    // ================= EDITTEXT FOCUS STYLE =================
    private void manageFocusStyle(EditText editText) {

        // When focus is lost and field is empty
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && editText.getText().toString().trim().isEmpty()) {
                editText.setBackgroundResource(R.drawable.edittext_selector);
            }
        });

        // When user types something
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().isEmpty()) {
                    editText.setBackgroundResource(R.drawable.edittext_selector);
                }
            }
        });
    }



    // ================= KEYBOARD NAV =================
    private void setupEditorActions() {
        username.setOnEditorActionListener((v, a, e) -> {
            if (a == EditorInfo.IME_ACTION_NEXT) {
                gmailid.requestFocus();
                return true;
            }
            return false;
        });

        gmailid.setOnEditorActionListener((v, a, e) -> {
            if (a == EditorInfo.IME_ACTION_NEXT) {
                userphone.requestFocus();
                return true;
            }
            return false;
        });

        userphone.setOnEditorActionListener((v, a, e) -> {
            if (a == EditorInfo.IME_ACTION_NEXT) {
                passwordedit.requestFocus();
                return true;
            }
            return false;
        });

        passwordedit.setOnEditorActionListener((v, a, e) -> {
            if (a == EditorInfo.IME_ACTION_NEXT) {
                cnfpasswordedit.requestFocus();
                return true;
            }
            return false;
        });

        cnfpasswordedit.setOnEditorActionListener((v, a, e) -> {
            if (a == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    // ================= KEYBOARD =================
    private void hideKeyboard() {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null)
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    // ================= LOADING =================
    private void showLoading(String msg) {
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingText.setText(msg);

        successTick.setVisibility(View.GONE);
        loadingSpinner.setVisibility(View.VISIBLE);
    }

    private void showSuccessTick(String msg) {
        loadingText.setText(msg);
        loadingSpinner.setVisibility(View.GONE);
        successTick.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        loadingOverlay.setVisibility(View.GONE);
    }


    // ================= PASSWORD CHECK =================
    private void setupPasswordWatcher() {

        Handler hideHandler = new Handler(Looper.getMainLooper());

        Runnable hideTick = () -> {
            if (cnfLayout != null) {
                cnfLayout.setEndIconTintList(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
            }
        };

        Runnable showTick = () -> {
            if (cnfLayout != null) {
                cnfLayout.setEndIconTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2E7D32")));
            }
        };

        // PASSWORD FIELD
        passwordedit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c){}

            @Override
            public void afterTextChanged(Editable s) {
                String p = s.toString();
                String confirm = cnfpasswordedit.getText().toString();

                isPasswordOk = isStrongPassword(p);

                if (isPasswordOk) {
                    cnfpasswordedit.setEnabled(true);
                    passwordError.setVisibility(View.INVISIBLE);
                } else {
                    cnfpasswordedit.setEnabled(false);
                    cnfpasswordedit.setText("");
                    passwordError.setText("Password must be strong");
                    passwordError.setVisibility(View.VISIBLE);
                    isConfirmOk = false;
                    hideTick.run();

                    hideHandler.postDelayed(() ->
                            passwordError.setVisibility(View.INVISIBLE), 1500);
                }

                // if confirm already typed, re-check match
                if (!confirm.isEmpty()) {
                    isConfirmOk = p.equals(confirm);

                    if (isConfirmOk) {
                        cnfpasswordedit.setError(null);
                        showTick.run();
                    } else {
                        cnfpasswordedit.setError("Passwords do not match");
                        hideTick.run();

                        hideHandler.postDelayed(() ->
                                cnfpasswordedit.setError(null), 1500);
                    }
                }

                updateCreateButtonState();
            }
        });

        // CONFIRM PASSWORD FIELD
        cnfpasswordedit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c){}

            @Override
            public void afterTextChanged(Editable s) {
                String confirm = s.toString();
                String p = passwordedit.getText().toString();

                isConfirmOk = !confirm.isEmpty() && confirm.equals(p);

                if (isConfirmOk) {
                    cnfpasswordedit.setError(null);
                    showTick.run();
                } else {
                    hideTick.run();

                    if (!confirm.isEmpty()) {
                        cnfpasswordedit.setError("Passwords do not match");
                        shakeView(cnfpasswordedit);

                        hideHandler.postDelayed(() ->
                                cnfpasswordedit.setError(null), 1500);
                    }
                }

                updateCreateButtonState();
            }
        });
    }





    // ================= CLICK HANDLERS =================
    private void setupClicks() {

        btnNext.setOnClickListener(v -> {

            if (fromGoogle) {
                goToStepTwo();
                return;
            }

            String name = username.getText().toString().trim();
            String email = gmailid.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
                toast("Fill all details");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                toast("Invalid email");
                return;
            }

            showLoading("Otp verification");

            // ✅ ONE single gatekeeper
            hardRecheckEmailThenSendOtp();
        });




        createaccount.setOnClickListener(v -> registerUser());

        alreadyuser.setOnClickListener(v ->
                startActivity(new Intent(this, Login_Page.class)));
    }

    // ================= PASSWORD RULE =================
    private boolean isStrongPassword(String p) {
        if (p.length() < 8) return false;

        boolean u=false, l=false, d=false, s=false;
        String sp="!@#$%^&*()-_=+[]{}|;:'\",.<>?/`~";

        for(int i=0;i<p.length();i++){
            char c=p.charAt(i);
            if(Character.isUpperCase(c)) u=true;
            else if(Character.isLowerCase(c)) l=true;
            else if(Character.isDigit(c)) d=true;
            else if(sp.indexOf(c)>=0) s=true;
        }
        return u && l && d && s;
    }


    // ================= OTP SEND =================
    private void sendOtp() {


        long now = System.currentTimeMillis();

        if (now < otpLockUntil) {
            long remaining = (otpLockUntil - now) / 1000;
            toast("Too many attempts. Try again in " + remaining + " seconds");

            // 🔥 RESET BUTTON UI
            btnNext.setEnabled(true);
            btnNext.setText("Next");
            loadingBar2.setVisibility(View.GONE);
            return;
        }
        otpExpired = false;

        generatedOtp = String.valueOf(new Random().nextInt(900000) + 100000);
        otpTime = System.currentTimeMillis();
        otpFailCount = 0;

        OkHttpClient client = new OkHttpClient();

        JSONObject payload = new JSONObject();
        try {
            payload.put("service_id", "service_xy8cwd7");
            payload.put("template_id", "template_548779v");
            payload.put("user_id", "jOZd_ynxTJkP0NJTN");

            JSONObject params = new JSONObject();
            params.put("to_email", gmailid.getText().toString().trim());
            params.put("otp", generatedOtp);
            params.put("name", username.getText().toString().trim());

            payload.put("template_params", params);

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Request request = new Request.Builder()
                .url("https://api.emailjs.com/api/v1.0/email/send")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        payload.toString(),
                        MediaType.parse("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    toast("Network error. Check internet.");
                    btnNext.setEnabled(true);
                    btnNext.setText("Next");
                    loadingBar2.setVisibility(View.INVISIBLE);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int code = response.code();
                response.close();

                runOnUiThread(() -> {
                    btnNext.setEnabled(true);
                    btnNext.setText("Next");
                    loadingBar2.setVisibility(View.INVISIBLE);// 🔥 ALWAYS RESET UI

                    if (code == 200 || code == 201) {
                        toast("OTP sent");
                        showOtpDialog();
                    } else {
                        toast("Email failed");
                    }
                });
            }

        });
    }


    // ================= OTP DIALOG =================
    private void showOtpDialog() {

        View view = getLayoutInflater().inflate(R.layout.otp_page, null);

        EditText[] otp = {
                view.findViewById(R.id.otp1),
                view.findViewById(R.id.otp2),
                view.findViewById(R.id.otp3),
                view.findViewById(R.id.otp4),
                view.findViewById(R.id.otp5),
                view.findViewById(R.id.otp6)
        };

        Button verify = view.findViewById(R.id.btnVerify);
        ProgressBar bar = view.findViewById(R.id.progressBar);
        ImageView status = view.findViewById(R.id.statusIcon);
        TextView resend = view.findViewById(R.id.resend);
        TextView timer = view.findViewById(R.id.timer);
        otpError = view.findViewById(R.id.otp_error);


        TextView tvOtpInfo = view.findViewById(R.id.mailinfo);
        String userEmail = gmailid.getText().toString().trim();
        String message = "Your code was sent to you via Email: " + userEmail;
        SpannableString spannable = new SpannableString(message);
        int start = message.indexOf(userEmail);
        int end = start + userEmail.length();
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#1A92F1")),
                start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvOtpInfo.setText(spannable);


        setupOtpInputs(otp);

        otpDialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true) // allow back press
                .create();

        otpDialog.setOnCancelListener(dialog -> {
            // Cancel all timers
            if (otpTimer != null) otpTimer.cancel();
            if (expiryTimer != null) expiryTimer.cancel();
            if (hardCloseTimer != null) hardCloseTimer.cancel();
            if (resendTimer != null) resendTimer.cancel();
            btnNext.setEnabled(true);
            btnNext.setText("Next");
            loadingBar2.setVisibility(View.GONE);


            toast("OTP dialog closed");
        });

        otpDialog.show();

        otpTimer = new CountDownTimer(120000, 1000) {

            public void onTick(long ms) {
                timer.setText(String.format("%02d:%02d",
                        (ms / 1000) / 60,
                        (ms / 1000) % 60));
            }

            public void onFinish() {
                otpError.setVisibility(View.VISIBLE);
                otpError.setText("Please enter OTP")
                ;

                startExpiryTimer();      // 120 → 135
                startHardCloseTimer();   // 120 → 150
            }

        }.start();






        resend.setEnabled(false);
        resend.setTextColor(Color.GRAY);

        resendTimer = new CountDownTimer(30000,1000) {
            public void onFinish() {
                resend.setEnabled(true);
                resend.setTextColor(Color.BLUE);
            }
            public void onTick(long l) {}
        }.start();

        resend.setOnClickListener(v -> {

            if (otpTimer != null) otpTimer.cancel();
            if (expiryTimer != null) expiryTimer.cancel();
            if (hardCloseTimer != null) hardCloseTimer.cancel();
            if (resendTimer != null) resendTimer.cancel();

            otpDialog.dismiss();


            sendOtp();
        });


        verify.setOnClickListener(v -> {

            long now = System.currentTimeMillis();
            if (now < otpLockUntil) {
                long remaining = (otpLockUntil - now) / 1000;
                otpError.setVisibility(View.VISIBLE);
                otpError.setText("Locked. Try again in " + remaining + " seconds");
                return; // ⛔ STOP EVERYTHING
            }

            StringBuilder code = new StringBuilder();
            for (EditText e : otp)
                code.append(e.getText().toString().trim());

            // Empty OTP
            if (code.length() < 6) {
                otpError.setVisibility(View.VISIBLE);
                otpError.setText("Please enter OTP");
                new Handler().postDelayed(() ->
                        otpError.setVisibility(View.GONE), 3000);
                return;
            }

            // OTP expired (after 135s)
            if (otpExpired) {
                otpError.setVisibility(View.VISIBLE);
                otpError.setText("OTP expired");
                new Handler().postDelayed(() ->
                        otpError.setVisibility(View.GONE), 3000);
                return;
            }

            // Reset UI
            verify.setVisibility(View.GONE);
            status.setVisibility(View.GONE);
            otpError.setVisibility(View.GONE);
            bar.setVisibility(View.VISIBLE);

            new Handler().postDelayed(() -> {

                bar.setVisibility(View.GONE);



                // SUCCESS
                if (code.toString().equals(generatedOtp)) {

                    otpFailCount = 0;
                    otpLockUntil = 0L;

                    status.setVisibility(View.VISIBLE);
                    status.setImageResource(R.drawable.ic_tick_green);

                    otpVerified = true;
                    gmailid.setEnabled(false);
                    username.setEnabled(false);

                    // Cancel all timers
                    // Cancel all timers (NULL SAFE)
                    if (otpTimer != null) otpTimer.cancel();
                    if (expiryTimer != null) expiryTimer.cancel();
                    if (hardCloseTimer != null) hardCloseTimer.cancel();
                    if (resendTimer != null) resendTimer.cancel();


                    new Handler().postDelayed(() -> {
                        otpDialog.dismiss();
                        goToStepTwo();
                        startSessionTimer();
                    }, 800);


                }
                // FAILURE
                else {

                    otpFailCount++; // 🔥 MOVE HERE

                    if (otpFailCount >= MAX_FAILS) {

                        otpLockUntil = System.currentTimeMillis() + LOCK_DURATION;

                        getSharedPreferences("otp_lock", MODE_PRIVATE)
                                .edit()
                                .putLong("lock_until", otpLockUntil)
                                .apply();

                        toast("Too many wrong attempts. Locked for 10 minutes");

                        if (otpTimer != null) otpTimer.cancel();
                        if (expiryTimer != null) expiryTimer.cancel();
                        if (hardCloseTimer != null) hardCloseTimer.cancel();
                        if (resendTimer != null) resendTimer.cancel();

                        otpDialog.dismiss();
                        resetRegistration();
                        return;
                    }

                    status.setVisibility(View.VISIBLE);
                    status.setImageResource(R.drawable.ic_close_red);

                    otpError.setVisibility(View.VISIBLE);
                    otpError.setText("Wrong OTP");
                    new Handler().postDelayed(() ->
                            otpError.setVisibility(View.GONE), 3000);

                    new Handler().postDelayed(() -> {
                        status.setVisibility(View.GONE);
                        verify.setVisibility(View.VISIBLE);
                    }, 3000);
                }

            }, 1500);
        });

    }

    private void startSessionTimer() {
        if (sessionTimer != null) sessionTimer.cancel();

        sessionTimer = new CountDownTimer(120000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // nothing needed
            }


            @Override
            public void onFinish() {
                runOnUiThread(() -> {
                    toast("Session completed, please try again");
                    resetRegistration();
                });
            }

        }.start();
    }


    private void startExpiryTimer() {
        expiryTimer = new CountDownTimer(15000, 1000) {
            public void onTick(long l) {}

            public void onFinish() {
                otpExpired = true;
                otpError.setVisibility(View.VISIBLE);
                otpError.setText("OTP expired");
            }
        }.start();
    }
    private void startHardCloseTimer() {
        hardCloseTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long l) {}

            public void onFinish() {
                toast("OTP session ended");
                otpDialog.dismiss();
                resetRegistration();
            }
        }.start();
    }


    // ================= OTP INPUT =================
    private void setupOtpInputs(EditText[] e){
        for(int i=0;i<e.length;i++){
            int idx=i;
            e[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
                @Override public void onTextChanged(CharSequence s,int st,int b,int c){}
                @Override public void afterTextChanged(Editable s){
                    if(s.length()==1 && idx<e.length-1)
                        e[idx+1].requestFocus();
                }
            });
        }
    }

    // ================= STEP NAV =================
    private void goToStepTwo() {
        stepOneLayout.setVisibility(View.GONE);
        stepTwoLayout.setVisibility(View.VISIBLE);
        updateCreateButtonState();

        // Auto focus phone
        userphone.requestFocus();

        // Open keyboard
        userphone.post(() -> {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(userphone, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }



    private void resetRegistration() {

        // ==== OTP STATE RESET ====
        otpVerified = false;
        otpExpired = false;
        otpFailCount = 0;
        generatedOtp = null;

        // ==== CANCEL TIMERS ====
        if (otpTimer != null) otpTimer.cancel();
        if (expiryTimer != null) expiryTimer.cancel();
        if (hardCloseTimer != null) hardCloseTimer.cancel();
        if (resendTimer != null) resendTimer.cancel();
        if (sessionTimer != null) sessionTimer.cancel();

        // ==== RESET UI ====
        stepTwoLayout.setVisibility(View.GONE);
        stepOneLayout.setVisibility(View.VISIBLE);

        btnNext.setEnabled(true);
        btnNext.setText("Next");              //  YOU FORGOT THIS
        loadingBar2.setVisibility(View.GONE); //  AND THIS

        // ==== RE-ENABLE FIELDS ====
        username.setEnabled(true);
        gmailid.setEnabled(true);

        // Optional: clear password fields
        passwordedit.setText("");
        cnfpasswordedit.setText("");
        cnfpasswordedit.setEnabled(false);
    }

    private void registerUser() {

        if (!otpVerified) {
            toast("Verify email first");
            return;
        }

        if (fromGoogle) {
            registerGoogleUser();
        } else {
            registerNormalUser();
        }
    }


    private void registerGoogleUser() {

        String phone = userphone.getText().toString().trim();
        String password = passwordedit.getText().toString().trim();
        String confirm = cnfpasswordedit.getText().toString().trim();
        String name = username.getText().toString().trim();
        String email = gmailid.getText().toString().trim();

        if (!phone.matches("[6-9][0-9]{9}")) {
            passwordError.setText("Invalid phone number");
            passwordError.setVisibility(View.VISIBLE);
            return;
        }

        if (!password.equals(confirm)) {
            passwordError.setText("Passwords do not match");
            passwordError.setVisibility(View.VISIBLE);
            return;
        }

        showLoading("Creating account...");

        ensureGoogleSessionOrKickToLogin();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            hideLoading();
            toast("Google session expired. Please login again.");
            startActivity(new Intent(this, Login_Page.class));
            finish();
            return;
        }

        AuthCredential credential =
                EmailAuthProvider.getCredential(email, password);

        user.linkWithCredential(credential)
                .addOnSuccessListener(result -> {

                    String customId = name.replaceAll("\\s+", "") + phone;

                    DatabaseReference ref = databaseReference.child(customId);

                    ref.child("name").setValue(name);
                    ref.child("mobile").setValue(phone);
                    ref.child("email").setValue(email);

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        showSuccessTick("Account created!");

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {

                            // sign out both
                            FirebaseAuth.getInstance().signOut();
                            GoogleSignIn.getClient(
                                    Register_Page.this,
                                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                            .requestEmail()
                                            .build()
                            ).signOut();

                            hideLoading();
                            showAccountCreatedDialog(email);

                        }, 400);

                    }, 1000);
                })

                .addOnFailureListener(e -> {
                    hideLoading();

                    // Show real cause (collision, weak password, network, etc.)
                    String msg = e.getMessage();

                    // Common: email already has password provider somewhere else
                    // (usually when you accidentally created another account)
                    passwordError.setText(msg != null ? msg : "Link failed");
                    passwordError.setVisibility(View.VISIBLE);
                });

    }



    // ================= REGISTER =================
    private void registerNormalUser() {

        String phone = userphone.getText().toString().trim();
        String password = passwordedit.getText().toString().trim();
        String confirm = cnfpasswordedit.getText().toString().trim();
        String name = username.getText().toString().trim();
        String email = gmailid.getText().toString().trim();

        if (!phone.matches("[6-9][0-9]{9}")) {
            passwordError.setText("Invalid phone number");
            passwordError.setVisibility(View.VISIBLE);
            return;
        }

        if (!password.equals(confirm)) {
            passwordError.setText("Passwords do not match");
            passwordError.setVisibility(View.VISIBLE);
            return;
        }

        showLoading("Creating account...");


        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(auth -> {

                    String customId = name.replaceAll("\\s+", "") + phone;

                    DatabaseReference ref = databaseReference.child(customId);

                    ref.child("name").setValue(name);
                    ref.child("mobile").setValue(phone);
                    ref.child("email").setValue(email);

                    // 1) wait 1 sec, then show green tick
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        showSuccessTick("Account created!");

                        // 2) wait a bit (optional), then show dialog
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {

                            FirebaseAuth.getInstance().signOut();
                            hideLoading();

                            showAccountCreatedDialog(email);

                        }, 400);

                    }, 1000);
                })

                .addOnFailureListener(e -> {
                    hideLoading();
                    passwordError.setText(e.getMessage());
                    passwordError.setVisibility(View.VISIBLE);
                });
    }





    // ================= UI HELPERS =================
    @SuppressLint("ClickableViewAccessibility")
    private void applyPressAnimation(View view) {

        view.setClickable(true);
        view.setFocusable(true);

        view.setOnTouchListener((v, event) -> {

            switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:
                    v.animate().cancel();
                    v.animate()
                            .scaleX(0.92f)      // deeper push
                            .scaleY(0.92f)
                            .setDuration(90)
                            .setInterpolator(new android.view.animation.DecelerateInterpolator())
                            .start();
                    return true;

                case MotionEvent.ACTION_UP:
                    v.animate().cancel();
                    v.animate()
                            .scaleX(1.05f)      // small overshoot
                            .scaleY(1.05f)
                            .setDuration(70)
                            .setInterpolator(new android.view.animation.AccelerateInterpolator())
                            .withEndAction(() -> {

                                v.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(120)
                                        .setInterpolator(
                                                new android.view.animation.OvershootInterpolator()
                                        )
                                        .start();
                            })
                            .start();

                    v.performClick();
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    v.animate().cancel();
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(120)
                            .start();
                    return true;
            }

            return false;
        });
    }

    private void shakeView(View v) {
        v.animate()
                .translationXBy(10f).setDuration(40)
                .withEndAction(() ->
                        v.animate()
                                .translationXBy(-20f).setDuration(40)
                                .withEndAction(() ->
                                        v.animate()
                                                .translationXBy(20f).setDuration(40)
                                                .withEndAction(() ->
                                                        v.animate()
                                                                .translationXBy(-10f).setDuration(40)
                                                                .start()
                                                ).start()
                                ).start()
                ).start();
    }




    private void toast(String m){
        Toast.makeText(this,m,Toast.LENGTH_SHORT).show();
    }



    @Override
    public void onBackPressed() {


        if (fromGoogle) {

            // 1st back: Step2 -> Step1 (keep your current behavior)
            if (stepTwoLayout.getVisibility() == View.VISIBLE) {
                resetRegistration();
                return;
            }

            // 2nd back: Step1 -> Login page
            Intent i = new Intent(Register_Page.this, Login_Page.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return;
        }
        // If OTP dialog is showing, close it
        if (otpDialog != null && otpDialog.isShowing()) {
            otpDialog.dismiss();

            // Cancel all timers
            if (otpTimer != null) otpTimer.cancel();
            if (expiryTimer != null) expiryTimer.cancel();
            if (hardCloseTimer != null) hardCloseTimer.cancel();
            if (resendTimer != null) resendTimer.cancel();

            // Reset step one button UI
            btnNext.setEnabled(true);
            btnNext.setText("Next");
            loadingBar2.setVisibility(View.GONE);

            toast("OTP dialog closed");
            return; // prevent activity back press
        }

        // If step two is visible, go back to step one
        if (stepTwoLayout.getVisibility() == View.VISIBLE) {
            if (sessionTimer != null) sessionTimer.cancel();
            resetRegistration();

            // Reset step one button UI
            btnNext.setEnabled(true);
            btnNext.setText("Next");
            loadingBar2.setVisibility(View.GONE);

            return;
        }

        super.onBackPressed();
    }



}