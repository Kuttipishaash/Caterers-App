package com.caterassist.app.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.caterassist.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import es.dmoral.toasty.Toasty;

public class ForgotPassword extends AppCompatActivity implements View.OnClickListener {

    private Button resetPasswordButton;

    private EditText forgotEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        initViews();


        resetPasswordButton.setOnClickListener(this);
    }

    private void initViews() {
        resetPasswordButton = findViewById(R.id.forgot_password_button);
        forgotEmail = findViewById(R.id.forgot_email_et);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == resetPasswordButton.getId()) {
            String email = forgotEmail.getText().toString();
            if (emptyCheck()) {
                resetUserPassword(email);
            }
        }
    }

    private boolean emptyCheck() {
        String email = forgotEmail.getText().toString();

        if (email.trim().equalsIgnoreCase("")) {
            forgotEmail.requestFocus();
            forgotEmail.setError("This field can not be blank");
            return false;
        } else {
            return true;
        }
    }


    public void resetUserPassword(String email) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final ProgressDialog progressDialog = new ProgressDialog(ForgotPassword.this);
        progressDialog.setMessage("Checking Email...");
        progressDialog.show();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toasty.success(ForgotPassword.this, "Reset password instructions has sent to your email",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            progressDialog.dismiss();
                            Toasty.error(ForgotPassword.this, "Email ID does not exist! Please try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toasty.info(getApplicationContext(), e.toString().split(":", 2)[1], Toast.LENGTH_LONG).show();
            }
        });
    }


}
