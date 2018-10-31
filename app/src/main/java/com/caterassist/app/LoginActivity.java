package com.caterassist.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends Activity {

    private static final String TAG = "LoginActivity";


    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (firebaseAuth.getCurrentUser() != null) {
            //TODO: Remove log message after testing
            Log.i(TAG, "User is already logged in.");
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }
        setContentView(R.layout.activity_login);
        initViews();
    }

    private void initViews() {

    }
}
