package com.example.pickacard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import com.google.android.material.progressindicator.CircularProgressIndicator;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



public class Login_Page extends AppCompatActivity {
    EditText gmailtext, password;
    Button signinbutton;
    ImageButton googleButton;
    TextView redirect, forgotpassword;
    ProgressBar progressBar;
    ImageView loginTick;
    FirebaseAuth firebaseAuth;

    private View contentRoot;
    private View blurOverlay;
    private ProgressBar overlaySpinner;

    private View googleBlurOverlay;
    private long googleLoaderStartTime = 0;





    private ImageView googleOverlaySpinner;
    private Animation googleSpinAnim;



    // 1---------- Google ----------
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 100; // request code
// ---------- Google ----------

    private boolean navigatedToMain = false;

    private void goMainOnce() {
        if (navigatedToMain || isFinishing()) return;
        navigatedToMain = true;
        hideAllLoaders();
        startActivity(new Intent(Login_Page.this, Main_Screen_Page.class));
        finish();
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        firebaseAuth = FirebaseAuth.getInstance();
        gmailtext = findViewById(R.id.gmail);
        password = findViewById(R.id.password);
        signinbutton = findViewById(R.id.signin);
        googleButton = findViewById(R.id.google_login_btn);
        redirect = findViewById(R.id.newuser);
        forgotpassword = findViewById(R.id.forgot_password);
        progressBar = findViewById(R.id.progress1);
        loginTick = findViewById(R.id.loginTick);

        applyPressAnimation(signinbutton);
        applyPressAnimation(googleButton);

        contentRoot = findViewById(R.id.contentRoot);
        blurOverlay = findViewById(R.id.blurOverlay);
        overlaySpinner = findViewById(R.id.overlaySpinner);



        googleBlurOverlay = findViewById(R.id.googleBlurOverlay);
        googleOverlaySpinner = findViewById(R.id.googleOverlaySpinner);
        googleSpinAnim = AnimationUtils.loadAnimation(this, R.anim.spinner_rotate);






        // Email + Password login
        signinbutton.setOnClickListener(v -> loginuser());

        redirect.setOnClickListener(v -> startActivity(new Intent(this, Register_Page.class)));

       forgotpassword.setOnClickListener(v -> showForgotPasswordDialog());
        googleButton.setOnClickListener(v -> signInWithGoogle());


        setupGoogleClient();

        String prefill = getIntent().getStringExtra("prefill_email");
        if (prefill != null && !prefill.trim().isEmpty()) {
            gmailtext.setText(prefill);
            gmailtext.setSelection(prefill.length());
        }

    }
//helpers
private void showBlurLoading() {
    blurOverlay.setVisibility(View.VISIBLE);
    overlaySpinner.setVisibility(View.VISIBLE);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        contentRoot.setRenderEffect(
                RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
        );
    }
}

    private void hideBlurLoading() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contentRoot.setRenderEffect(null);
        }

        blurOverlay.setVisibility(View.GONE);
        overlaySpinner.setVisibility(View.GONE);
    }

    private void hideAllLoaders() {
        hideBlurLoading();
        hideGoogleLoading();
    }


    private void showGoogleLoading() {
        googleLoaderStartTime = System.currentTimeMillis();
        googleBlurOverlay.setVisibility(View.VISIBLE);

        if (googleOverlaySpinner != null) {
            googleOverlaySpinner.clearAnimation();
            googleOverlaySpinner.startAnimation(googleSpinAnim);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contentRoot.setRenderEffect(
                    RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
            );
        }
    }

    private void hideGoogleLoading() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contentRoot.setRenderEffect(null);
        }

        if (googleOverlaySpinner != null) {
            googleOverlaySpinner.clearAnimation();
        }

        googleBlurOverlay.setVisibility(View.GONE);
    }



    private void hideGoogleLoadingWithDelay(Runnable afterHide) {

        long elapsed = System.currentTimeMillis() - googleLoaderStartTime;
        long remaining = 2000 - elapsed;   // 1.5 seconds minimum

        if (remaining <= 0) {
            hideGoogleLoading();
            afterHide.run();
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                hideGoogleLoading();
                afterHide.run();
            }, remaining);
        }
    }





    // ---------------- EMAIL / PASSWORD ----------------


    private void loginuser() {
        String gmail=gmailtext.getText().toString().trim();
        String pass=password.getText().toString().trim();
        if (gmail.isEmpty()||pass.isEmpty())
        {
            Toast.makeText(this, "Please give valid Inputs", Toast.LENGTH_SHORT).show();
            resetLoginUI();
            return;
        }

        signinbutton.setEnabled(false);
        signinbutton.setText("");
        progressBar.setVisibility(View.VISIBLE);
          loginTick.setVisibility(View.GONE);

        firebaseAuth.signInWithEmailAndPassword(gmail,pass).addOnCompleteListener(task -> {

            if (isFinishing()) return; // SAFETY
            progressBar.setVisibility(View.GONE);

            if (task.isSuccessful()) {

                // 1) Stop spinner on button
                progressBar.setVisibility(View.GONE);
              //  signinbutton.setText("");

                // 2) Show tick on button (let UI draw it)
                loginTick.setImageResource(R.drawable.ic_success_green);
                loginTick.setVisibility(View.VISIBLE);

                Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show();

                String userEmail = gmail;
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

                new Handler(Looper.getMainLooper()).postDelayed(() -> {

                    if (isFinishing()) return;

                    showBlurLoading();

                    usersRef.orderByChild("email").equalTo(userEmail)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        hideAllLoaders();
                                        Toast.makeText(Login_Page.this,
                                                "User profile not found. Please register again.",
                                                Toast.LENGTH_LONG).show();

                                        // Optional: logout to avoid broken session
                                        FirebaseAuth.getInstance().signOut();
                                        return;
                                    }

                                    // ✅ Get first match
                                    DataSnapshot userSnap = snapshot.getChildren().iterator().next();
                                    String customId = userSnap.getKey();

                                    getSharedPreferences("UserData", MODE_PRIVATE)
                                            .edit()
                                            .putString("customId", customId)
                                            .putString("email", userEmail)
                                            .apply();

                                    goMainOnce();
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    hideAllLoaders();
                                    Toast.makeText(Login_Page.this,
                                            "Database error. Try again.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                }, 800);
                // ✅ 600ms is enough for tick to be seen
            }

            else
            {
                signinbutton.setText("");
                // signinbutton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close_red, 0);
                loginTick.setImageResource(R.drawable.ic_close_red);
                loginTick.setVisibility(View.VISIBLE);
                hideBlurLoading();
                Toast.makeText(this, "Incorrect Details", Toast.LENGTH_SHORT).show();

                new Handler(Looper.getMainLooper()).postDelayed(() ->
                {
                    if (!isFinishing())
                    {
                        loginTick.setVisibility(View.GONE);
                        //  signinbutton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        signinbutton.setEnabled(true);
                        signinbutton.setText("LOGIN"); }
                }, 2500); }


        });

    }

    // 2GOOGLE SIGN-IN SETUP
    // --------------------------------------------------
    private void setupGoogleClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build(); // no setPrompt here to avoid SDK issues

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }


    // 3GOOGLE BUTTON CLICK
    // --------------------------------------------------
    private void signInWithGoogle() {


        // ✅ FORCE account chooser every time
        googleSignInClient.signOut().addOnCompleteListener(t -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }



    // 4GOOGLE RESULT
    // --------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode != RESULT_OK) {
                hideBlurLoading();
                FirebaseAuth.getInstance().signOut();   // ✅ important
                googleSignInClient.signOut();           // ✅ important
                return;
            }
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                // Got Google account, now authenticate with Firebase
                firebaseAuthWithGoogle(account.getIdToken());

            } catch (ApiException e) {
                hideAllLoaders();

                Toast.makeText(this, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    // 5FIREBASE AUTH WITH GOOGLE
    // --------------------------------------------------
    private void firebaseAuthWithGoogle(String idToken) {
        showBlurLoading();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {

                    if (!task.isSuccessful()) {
                        hideAllLoaders();
                        Toast.makeText(this, "Firebase auth failed", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (firebaseUser == null || firebaseUser.getEmail() == null) {
                        hideAllLoaders();
                        Toast.makeText(this, "Google user not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean isNewAuthUser = false;
                    if (task.getResult() != null && task.getResult().getAdditionalUserInfo() != null) {
                        isNewAuthUser = task.getResult().getAdditionalUserInfo().isNewUser();
                    }

                    checkGoogleUserInDatabase(firebaseUser.getEmail(), firebaseUser.getDisplayName(), isNewAuthUser);
                });
    }




    // 6HYBRID / BRIDGE LOGIC
    // --------------------------------------------------
    private void checkGoogleUserInDatabase(String email, String name, boolean isNewAuthUser) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // existing user -> go main
                            showGoogleLoading();
                            DataSnapshot userSnap = snapshot.getChildren().iterator().next();
                            String customId = userSnap.getKey();
                            saveSession(customId, email);

                            hideGoogleLoadingWithDelay(() -> {
                                if (isFinishing()) return;
                                goMainOnce();
                            });

                        } else {
                            hideAllLoaders();
                            showNewGoogleAccountDialog(email, name, isNewAuthUser);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        hideAllLoaders();
                        Toast.makeText(Login_Page.this, "Database error", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    // 7FETCH EMAIL USER FROM DB
    // --------------------------------------------------


    // SESSION SAVE
    // --------------------------------------------------
    private void saveSession(String customId, String email) {

        getSharedPreferences("UserData", MODE_PRIVATE)
                .edit()
                //.putBoolean("isLoggedIn", true)
                .putString("customId", customId)
                .putString("email", email)
                .apply();
    }




    private void resetLoginUI() {
        progressBar.setVisibility(View.GONE);
        loginTick.setVisibility(View.GONE);
        signinbutton.setVisibility(View.VISIBLE);
        signinbutton.setEnabled(true);
        signinbutton.setText("LOGIN");
    }


    private void showNewGoogleAccountDialog(String email, String name, boolean isNewAuthUser) {

        View v = getLayoutInflater().inflate(R.layout.dialog_new_google_account, null);

        Button btnYes = v.findViewById(R.id.btnYes);
        Button btnNo  = v.findViewById(R.id.btnNo);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(v)
                .setCancelable(false)
                .create();

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        btnNo.setOnClickListener(view -> {
            dialog.dismiss();
            hideAllLoaders();

            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();

            // ✅ if Firebase created a NEW auth user, delete it
            if (isNewAuthUser && u != null) {
                u.delete().addOnCompleteListener(t -> {
                    FirebaseAuth.getInstance().signOut();
                    if (googleSignInClient != null) {
                        googleSignInClient.signOut();
                        googleSignInClient.revokeAccess(); // optional but helps
                    }
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
                });
            } else {
                // existing auth user: just sign out
                FirebaseAuth.getInstance().signOut();
                if (googleSignInClient != null) {
                    googleSignInClient.signOut();
                    googleSignInClient.revokeAccess();
                }
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        btnYes.setOnClickListener(view -> {
            dialog.dismiss();
            hideAllLoaders();

            Intent i = new Intent(Login_Page.this, Register_Page.class);
            i.putExtra("from_google", true);
            i.putExtra("google_email", email);
            i.putExtra("google_name", name);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }




    // ---------------- FORGOT PASSWORD ----------------

    private void showForgotPasswordDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);

        EditText emailInput = view.findViewById(R.id.email_input);
        ProgressBar progressBar = view.findViewById(R.id.progress_bar);
        ImageView successTick = view.findViewById(R.id.success_tick);
        ImageView errorIcon = view.findViewById(R.id.error_icon);
        TextView statusText = view.findViewById(R.id.status_text);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setView(view)
                .setPositiveButton("Send", null)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .create();
        dialog.show();

        Button sendBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        sendBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.setError("Enter valid email");
                return;
            }

            sendBtn.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            successTick.setVisibility(View.GONE);
            errorIcon.setVisibility(View.GONE);
            statusText.setVisibility(View.GONE);

            // ✅ FIX: Query database for email instead of fetching all users
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
            usersRef.orderByChild("email").equalTo(email)
                    .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                progressBar.setVisibility(View.GONE);
                                errorIcon.setVisibility(View.VISIBLE);
                                statusText.setVisibility(View.VISIBLE);
                                statusText.setTextColor(Color.RED);
                                statusText.setText("Email not found. Please register.");
                                new Handler().postDelayed(() -> {
                                    errorIcon.setVisibility(View.GONE);
                                    statusText.setVisibility(View.GONE);
                                    sendBtn.setEnabled(true);
                                }, 2500);
                                return;
                            }

                            // Email exists → send reset link
                            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                    .addOnSuccessListener(unused -> {
                                        progressBar.setVisibility(View.GONE);
                                        successTick.setVisibility(View.VISIBLE);
                                        statusText.setVisibility(View.VISIBLE);
                                        statusText.setTextColor(Color.parseColor("#2E7D32"));
                                        statusText.setText("Reset link sent successfully");
                                        new Handler().postDelayed(dialog::dismiss, 2500);
                                    })
                                    .addOnFailureListener(e -> {
                                        progressBar.setVisibility(View.GONE);
                                        sendBtn.setEnabled(true);
                                        errorIcon.setVisibility(View.VISIBLE);
                                        statusText.setVisibility(View.VISIBLE);
                                        statusText.setTextColor(Color.RED);
                                        statusText.setText(e.getMessage());
                                    });
                        }

                        @Override
                        public void onCancelled(com.google.firebase.database.DatabaseError error) {
                            progressBar.setVisibility(View.GONE);
                            sendBtn.setEnabled(true);
                            errorIcon.setVisibility(View.VISIBLE);
                            statusText.setVisibility(View.VISIBLE);
                            statusText.setTextColor(Color.RED);
                            statusText.setText("Database error. Try again.");
                        }
                    });
        });
    }



    private void stopLoginLoader() {
        progressBar.setVisibility(View.GONE);
        signinbutton.setText("LOGIN");
        signinbutton.setEnabled(true);
    }



    @SuppressLint("ClickableViewAccessibility")
    private void applyPressAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                    break;
            }
            return false;
        });
    }

}