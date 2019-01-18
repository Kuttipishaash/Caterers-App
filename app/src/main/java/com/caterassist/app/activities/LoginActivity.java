package com.caterassist.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.utils.AppUtils;
import com.caterassist.app.utils.Constants;
import com.caterassist.app.utils.FirebaseUtils;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import androidx.annotation.NonNull;
import es.dmoral.toasty.Toasty;


public class LoginActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private TextInputEditText usernameEdtTxt;
    private TextInputEditText passwordEdtTxt;
    private TextView signUpLinearLayout, termsCondTextView, forgotPasswordTextView;
    private FloatingActionButton loginFAB;
    private FABProgressCircle fabProgressCircle;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    ValueEventListener userDetailsListener;
    private DatabaseReference userInfoReference;
    private SharedPreferences sharedPreferences;
    private UserDetails userDetails;
    private String currentUserID;

    @Override
    protected void onResume() {
        if (userDetailsListener != null) {
            userInfoReference.addListenerForSingleValueEvent(userDetailsListener);
        }
        if (firebaseAuth.getCurrentUser() != null) {
            Log.i(TAG, "User is already logged in.");
            userDetails = AppUtils.getUserInfoSharedPreferences(this);
            launchHomeActivity();
        }
        super.onResume();
    }

    private void launchHomeActivity() {
        if (userDetails.getIsVendor()) {
            startActivity(new Intent(LoginActivity.this, VendorHomeActivity.class));
        } else {
            startActivity(new Intent(LoginActivity.this, CatererHomeActivity.class));
        }
        finish();
    }

    @Override
    protected void onPause() {
        if (userDetailsListener != null) {
            userInfoReference.removeEventListener(userDetailsListener);
        }
        super.onPause();
    }

    private void generateNotificationToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "getInstanceId failed", task.getException());
                        return;
                    } else {
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Log.e(TAG, token);
                        Toasty.info(LoginActivity.this, token, Toast.LENGTH_SHORT).show();
                        saveToken(token);
                    }
                });
    }

    private void saveToken(String token) {
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.USER_INFO_BRANCH_NAME + currentUserID;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
        databaseReference.child(FirebaseUtils.USER_TOKEN_BRANCH).setValue(token).addOnSuccessListener(aVoid -> {
            Toasty.success(LoginActivity.this, "Token saved").show();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constants.SharedPref.USER_NOTIFICATION_TOKEN, token);
            editor.apply();
            launchHomeActivity();
        }).addOnFailureListener(e -> AppUtils.cleanUpAndLogout(LoginActivity.this));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        initViews();

        //Setting listeners
        loginFAB.setOnClickListener(this);
        signUpLinearLayout.setOnClickListener(this);
        termsCondTextView.setOnClickListener(this);
        forgotPasswordTextView.setOnClickListener(this);
    }

    private void initViews() {
        usernameEdtTxt = findViewById(R.id.act_login_txt_inp_username);
        passwordEdtTxt = findViewById(R.id.act_login_txt_inp_passowrd);
        loginFAB = findViewById(R.id.act_login_fab_login);
        fabProgressCircle = findViewById(R.id.fabProgressCircle);
        signUpLinearLayout = findViewById(R.id.sign_up_for_account);
        termsCondTextView = findViewById(R.id.act_login_terms_conditions);
        forgotPasswordTextView = findViewById(R.id.act_login_forgot_password);
        sharedPreferences = getSharedPreferences(Constants.SharedPref.PREF_FILE, MODE_PRIVATE);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == loginFAB.getId()) {
            if (emptyCheck()) {
                login();
                fabProgressCircle.show();
            }

        } else if (v.getId() == signUpLinearLayout.getId()) {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        } else if (v.getId() == termsCondTextView.getId()) {
            startActivity(new Intent(LoginActivity.this, FAQActivity.class));
        } else if (v.getId() == forgotPasswordTextView.getId()) {
            startActivity(new Intent(LoginActivity.this, ForgotPassword.class));
        }
    }

    private void login() {
        //TODO: Check if entries are not null
        String email = usernameEdtTxt.getText().toString();
        String password = passwordEdtTxt.getText().toString();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.i(TAG, "signInWithEmail:success");
                        currentUserID = firebaseAuth.getUid();
                        getUserInfo();

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        fabProgressCircle.hide();
                        Toasty.error(LoginActivity.this, "Login failed! Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.info(getApplicationContext(), e.toString().split(":", 2)[1], Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean emptyCheck() {
        String email = usernameEdtTxt.getText().toString();
        String password = passwordEdtTxt.getText().toString();

        if (email.trim().equalsIgnoreCase("")) {
            usernameEdtTxt.requestFocus();
            usernameEdtTxt.setError("This field can not be blank");
            return false;
        } else if (password.trim().equalsIgnoreCase("")) {
            passwordEdtTxt.requestFocus();
            passwordEdtTxt.setError("This field can not be blank");
            return false;
        } else {
            return true;
        }
    }

    private void getUserInfo() {
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() +
                FirebaseUtils.USER_INFO_BRANCH_NAME +
                currentUserID;
        userInfoReference = FirebaseDatabase.getInstance().getReference(databasePath);
        userDetailsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userDetails = dataSnapshot.getValue(UserDetails.class);
                if (userDetails != null) {
                    Log.d(TAG, "onDataChange: Fetch successful");
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constants.SharedPref.USER_ID, userDetails.getUserID());
                    editor.putString(Constants.SharedPref.USER_EMAIL, userDetails.getUserEmail());
                    editor.putBoolean(Constants.SharedPref.USER_IS_VENDOR, userDetails.getIsVendor());
                    editor.putString(Constants.SharedPref.USER_NAME, userDetails.getUserName());
                    editor.putString(Constants.SharedPref.USER_STREET, userDetails.getUserStreetName());
                    editor.putString(Constants.SharedPref.USER_PHONE, userDetails.getUserPhone());
                    editor.putString(Constants.SharedPref.USER_LOC, userDetails.getUserLocationName());
                    editor.putString(Constants.SharedPref.USER_DISTRICT, userDetails.getUserDistrictName());
                    editor.putString(Constants.SharedPref.USER_IMG_URL, userDetails.getUserImageUrl());
                    editor.apply();
                    generateNotificationToken();
                } else {
                    Log.e(TAG, "onDataChange: Failed to fetch");
                    fabProgressCircle.hide();
                    Toasty.error(LoginActivity.this, "Login failed! Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                userDetails = null;
            }
        };
        userInfoReference.addListenerForSingleValueEvent(userDetailsListener);
    }
}
