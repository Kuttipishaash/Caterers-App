package com.caterassist.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;


public class LoginActivity extends Activity {

    private static final String TAG = "LoginActivity";

    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    private RelativeLayout mRoot;

    private TextInputEditText mEmailField, mPasswordField;
    private TextView mCreateAccount;
    private FloatingActionButton mLogInButton;


    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        if (firebaseAuth.getCurrentUser() != null) {
//            //TODO: Remove log message after testing
//            Log.i(TAG, "User is already logged in.");
//            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
//            finish();
//        }

        initViews();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            finish();
            startActivity(new Intent(this, HomeActivity.class));
        }
        mCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });
        mLogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailField.getText().toString().trim();
                String password = mPasswordField.getText().toString().trim();
                signIn(email, password);
            }
        });
//        mForgotPassword.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(LoginActivity.this, ResetPassword.class));
//            }
//        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            finish();
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        }
    }

    private void initViews() {
        mRoot = findViewById(R.id.login_root_view);
        mEmailField = findViewById(R.id.login_email);
        mPasswordField = findViewById(R.id.login_password);
        mCreateAccount = findViewById(R.id.login_create_account);
        mLogInButton = findViewById(R.id.login_button_fab);
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        //TODO: showProgressDialog();

        progressBar = new ProgressBar(LoginActivity.this, null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mRoot.addView(progressBar, params);
        progressBar.setVisibility(View.VISIBLE);

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            checkEmailVerification();

                            //TODO: updateUI(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //TODO: updateUI(null);
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            //mStatusTextView.setText(R.string.auth_failed);
                        }
                        progressBar.setVisibility(View.GONE);
                        //hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }


    public void checkEmailVerification() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        boolean isVerified = user.isEmailVerified();

        if (isVerified) {
            finish();
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        } else {
            Toast.makeText(LoginActivity.this, "Please verify you're Email to login",
                    Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }
}
