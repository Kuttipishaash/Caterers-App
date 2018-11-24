package com.caterassist.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.SignUpActivity;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.utils.Constants;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import es.dmoral.toasty.Toasty;


public class LoginActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private TextInputEditText usernameEdtTxt;
    private TextInputEditText passwordEdtTxt;
    private LinearLayout signUpLinearLayout;
    private FloatingActionButton loginFAB;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    ValueEventListener userDetailsListener;
    private DatabaseReference userInfoReference;
    private SharedPreferences sharedPreferences;
    private UserDetails userDetails;
    private String currentUserID;
    //TODO: Show progress dialog.

    @Override
    protected void onResume() {
        if (userDetailsListener != null) {
            userInfoReference.addListenerForSingleValueEvent(userDetailsListener);
        }
        if (firebaseAuth.getCurrentUser() != null) {
            Log.i(TAG, "User is already logged in.");
            launchHomeActivity();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (userDetailsListener != null) {
            userInfoReference.removeEventListener(userDetailsListener);
        }
        super.onPause();
    }
    private void launchHomeActivity() {
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();

        //Setting listeners
        loginFAB.setOnClickListener(this);
        signUpLinearLayout.setOnClickListener(this);
    }

    private void initViews() {
        usernameEdtTxt = findViewById(R.id.act_login_txt_inp_username);
        passwordEdtTxt = findViewById(R.id.act_login_txt_inp_passowrd);
        loginFAB = findViewById(R.id.act_login_fab_login);
        signUpLinearLayout = findViewById(R.id.sign_up_for_account);

        sharedPreferences = getSharedPreferences(Constants.SharedPref.PREF_FILE, MODE_PRIVATE);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == loginFAB.getId()) {
            login();
        } else if (v.getId() == signUpLinearLayout.getId()) {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        }
    }

    private void login() {
        //TODO: Check if entries are not null
        String email = usernameEdtTxt.getText().toString();
        String password = passwordEdtTxt.getText().toString();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.i(TAG, "signInWithEmail:success");
                            currentUserID = firebaseAuth.getUid();
                            getUserInfo();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toasty.error(LoginActivity.this, "Login failed! Please try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
                    editor.putFloat(Constants.SharedPref.USER_LAT, userDetails.getUserLat());
                    editor.putFloat(Constants.SharedPref.USER_LNG, userDetails.getUserLng());
                    editor.putString(Constants.SharedPref.USER_IMG_URL, userDetails.getUserImageUrl());
                    editor.apply();
                    launchHomeActivity();
                } else {
                    Log.e(TAG, "onDataChange: Failed to fetch");
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
