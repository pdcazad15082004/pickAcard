package com.example.pickacard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Main_Screen_Page extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    BottomNavigationView bottomNavigationView;
    FrameLayout frameLayout;
    DrawerLayout drawerLayout;
    ImageButton menuButton;

    ImageView toolbarIcon;
    NavigationView navigationView;
    TextView toolbarTitle,headerUserName,headerUserMobile;
    ImageView rightLogo;
    View headerView;


    // Track last selected bottom navigation item
    private int lastSelectedBottomItemId = R.id.btmhome;
    private long lastBackPressedTime = 0;
    public static boolean shouldReopenDrawer = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ 1) SESSION GATEKEEPER (must be BEFORE setContentView)
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String customId = prefs.getString("customId", null);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || customId == null || customId.trim().isEmpty()) {
            redirectToLogin();
            return;
        }

        // ✅ 2) Only load UI after session is valid
        setContentView(R.layout.activity_main_screen_page);

        rightLogo = findViewById(R.id.right_logo);

        drawerLayout = findViewById(R.id.drawer_layout);
        menuButton = findViewById(R.id.menuid);
        frameLayout = findViewById(R.id.framelay);
        navigationView = findViewById(R.id.navigation_button);
        bottomNavigationView = findViewById(R.id.btmnavigationview);
        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarIcon = findViewById(R.id.toolbar_icon);

        headerView = navigationView.getHeaderView(0);
        headerUserName = headerView.findViewById(R.id.userid);
        headerUserMobile = headerView.findViewById(R.id.usermobile);

        FloatingActionButton contactUsFab = findViewById(R.id.contactUsFab);
        TextView contactUsLabel = findViewById(R.id.contactUsLabel);

        rightLogo.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(Main_Screen_Page.this, v);
            popup.getMenuInflater().inflate(R.menu.right_logo_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.delete_account) {
                    showDeleteConfirmationDialog1();
                    return true;
                }
                return false;
            });

            popup.show();
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // ✅ 3) Header page load (use the SAME customId we already validated)
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(customId);
        userRef.get().addOnSuccessListener(dataSnapshot -> {
            String name = dataSnapshot.child("name").getValue(String.class);
            String mobile = dataSnapshot.child("mobile").getValue(String.class);

            if (name != null) headerUserName.setText(name);
            if (mobile != null) headerUserMobile.setText(mobile);

        }).addOnFailureListener(e ->
                Toast.makeText(Main_Screen_Page.this, "Failed to load user info", Toast.LENGTH_SHORT).show()
        );

        // Drawer animation handling
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                contactUsFab.setTranslationX(slideOffset * 300);
                contactUsFab.setAlpha(1 - slideOffset);
                contactUsLabel.setAlpha(1 - slideOffset);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                contactUsFab.setAlpha(0f);
                contactUsLabel.setVisibility(View.GONE);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                contactUsFab.setAlpha(1f);
                contactUsLabel.setAlpha(1f);
            }
        });

        contactUsFab.setOnClickListener(v -> {
            boolean selected = contactUsFab.isSelected();
            contactUsFab.setSelected(!selected);

            if (!selected) {
                contactUsLabel.setVisibility(View.VISIBLE);

                lastSelectedBottomItemId = bottomNavigationView.getSelectedItemId();

                bottomNavigationView.getMenu().setGroupCheckable(0, false, true);
                for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
                    bottomNavigationView.getMenu().getItem(i).setChecked(false);
                }
                bottomNavigationView.getMenu().setGroupCheckable(0, true, true);

                loadFragment(new ContactUs_Fragment());
                toolbarTitle.setText("Contact Us");
                toolbarIcon.setImageResource(R.drawable.contact1);

            } else {
                contactUsLabel.setVisibility(View.GONE);
                contactUsFab.setSelected(false);
                bottomNavigationView.setSelectedItemId(lastSelectedBottomItemId);
            }
        });

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navhome) {
                loadFragment(new Home_Fragment());
                Toast.makeText(Main_Screen_Page.this, "Home", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.navcards) {
                loadFragment(new Cards_Page_Fragment());
                Toast.makeText(Main_Screen_Page.this, "Cards", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.navfeedback) {
                shouldReopenDrawer = true;
                startActivity(new Intent(Main_Screen_Page.this, Feedback_Page.class));
                drawerLayout.close();
                return true;
            } else if (itemId == R.id.navaboutus) {
                shouldReopenDrawer = true;
                startActivity(new Intent(Main_Screen_Page.this, AboutUs_Page.class));
                drawerLayout.close();
                return true;
            } else if (itemId == R.id.navlogout) {
                new androidx.appcompat.app.AlertDialog.Builder(Main_Screen_Page.this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes", (dialog, which) -> doLogout())
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            }

            drawerLayout.close();
            return true;
        });

        // ✅ 4) Load default fragment
        toolbarTitle.setText("Home");
        toolbarIcon.setImageResource(R.drawable.home);
        loadFragment(new Home_Fragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            contactUsFab.setSelected(false);
            contactUsLabel.setVisibility(View.GONE);

            if (id == R.id.btmhome) {
                lastSelectedBottomItemId = R.id.btmhome;
                loadFragment(new Home_Fragment());
                toolbarTitle.setText("Home");
                toolbarIcon.setImageResource(R.drawable.home);
                return true;
            } else if (id == R.id.btmcards) {
                lastSelectedBottomItemId = R.id.btmcards;
                loadFragment(new Cards_Page_Fragment());
                toolbarTitle.setText("Cards");
                toolbarIcon.setImageResource(R.drawable.cards);
                return true;
            } else if (id == R.id.btmshop) {
                lastSelectedBottomItemId = R.id.btmshop;
                loadFragment(new Shopping_Fragment());
                toolbarTitle.setText("Shopping");
                toolbarIcon.setImageResource(R.drawable.cart);
                return true;
            } else if (id == R.id.btmhistory) {
                lastSelectedBottomItemId = R.id.btmhistory;
                loadFragment(new History_Fragment());
                toolbarTitle.setText("History");
                toolbarIcon.setImageResource(R.drawable.history);
                return true;
            }

            return false;
        });
    }


    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.framelay, fragment)
                .commit();
    }

    private void doLogout() {

        // 1) Firebase sign out
        FirebaseAuth.getInstance().signOut();

        // 2) Clear session storage
        SharedPreferences.Editor editor = getSharedPreferences("UserData", MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        // 3) Google sign out (optional)
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> redirectToLogin());
        } else {
            redirectToLogin();
        }
    }


    private void redirectToLogin() {
        Intent intent = new Intent(Main_Screen_Page.this, Login_Page.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // 1. If drawer is open, close it
        super.onBackPressed();
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        // 2. Get current fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.framelay);

        // 3. If not on Home_Fragment, go to Home
        if (!(currentFragment instanceof Home_Fragment)) {
            loadFragment(new Home_Fragment());
            toolbarTitle.setText("Home");
            toolbarIcon.setImageResource(R.drawable.home);
            bottomNavigationView.setSelectedItemId(R.id.btmhome);
            return;
        }

        // 4. If already on Home → handle double back to exit
        if (System.currentTimeMillis() - lastBackPressedTime < 2000) {
            new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", (dialog, which) -> finishAffinity())
                    .setNegativeButton("No", null)
                    .show();
        } else {
            lastBackPressedTime = System.currentTimeMillis();
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Check if we should reopen drawer
        if (shouldReopenDrawer) {
            drawerLayout.openDrawer(GravityCompat.START);
            shouldReopenDrawer = false; // reset
        }
    }

    private void showDeleteConfirmationDialog1() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    showDeleteConfirmationDialog(); // Step 4
                })
                .setNegativeButton("No", null)
                .show();
    }


    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account");
        builder.setMessage("Enter your password to confirm deletion");

        View view = getLayoutInflater().inflate(R.layout.dialog_delete_account, null);
        builder.setView(view);

        EditText passwordInput = view.findViewById(R.id.et_password);


        builder.setPositiveButton("Delete", (dialog, which) -> {
            String password = passwordInput.getText().toString().trim();

            if (password.isEmpty()) {
                Toast.makeText(this, "Password required", Toast.LENGTH_SHORT).show();
                return;
            }
            reAuthenticateAndDelete(password);
        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // ✅ Forgot password click

    }

    private void reAuthenticateAndDelete(String password) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential =
                EmailAuthProvider.getCredential(user.getEmail(), password);

        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> deleteUserCompletely(user))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show()
                );
    }

    private void deleteUserCompletely(FirebaseUser user) {

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String customId = prefs.getString("customId", null);

        if (customId != null) {
            FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(customId)
                    .removeValue();
        }

        user.delete().addOnSuccessListener(aVoid -> {
            FirebaseAuth.getInstance().signOut();
            prefs.edit().clear().apply();

            Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(this, Register_Page.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });
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

