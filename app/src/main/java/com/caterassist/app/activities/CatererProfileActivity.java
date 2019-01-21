package com.caterassist.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caterassist.app.R;
import com.caterassist.app.dialogs.LoadingDialog;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.utils.Constants;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import es.dmoral.toasty.Toasty;

public class CatererProfileActivity extends Activity implements View.OnClickListener {

    private String userID;
    private UserDetails userDetails;

    private ImageView userImage;
    private TextView userNameTxtView;
    private TextView addressLine1TxtView;
    private TextView addressLine2TxtView;
    private LinearLayout callButton;
    private LinearLayout emailButton;
    private LinearLayout parentLayout;

    private LoadingDialog loadingDialog;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caterer_profile);
        Intent intent = getIntent();
        if (intent.getStringExtra(Constants.IntentExtrasKeys.USER_ID) != null) {
            userID = intent.getStringExtra(Constants.IntentExtrasKeys.USER_ID);
            initViews();
            fetchUserDetails();
        } else {
            Toasty.error(this, "No vendor data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchUserDetails() {
        loadingDialog = new LoadingDialog(this, "Loading user details");
        loadingDialog.show();
        final int interval = 10000; // 1 Second
        handler = new Handler();
        runnable = () -> {
            if (loadingDialog != null)
                if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                    Toast.makeText(CatererProfileActivity.this, "Please check your internet connection and try again!", Toast.LENGTH_SHORT).show();
                    showErrorLayout();
                }
        };
        handler.postAtTime(runnable, System.currentTimeMillis() + interval);
        handler.postDelayed(runnable, interval);
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.USER_INFO_BRANCH_NAME + userID;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userDetails = dataSnapshot.getValue(UserDetails.class);
                if (userDetails != null) {
                    showUserDetails();
                } else {
                    showErrorLayout();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        super.onBackPressed();
    }

    private void showErrorLayout() {
        //TODO: Set visiblity of error layout
        parentLayout.setVisibility(View.GONE);
        loadingDialog.dismiss();
    }

    private void showUserDetails() {
        userNameTxtView.setText(userDetails.getUserName());
        String addressLine1 = userDetails.getUserStreetName() + ", " + userDetails.getUserLocationName();
        addressLine1TxtView.setText(addressLine1);
        addressLine2TxtView.setText(userDetails.getUserDistrictName());
        String imageUrl = userDetails.getUserImageUrl();
        if (imageUrl != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child(imageUrl).getDownloadUrl().addOnSuccessListener(uri -> {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.drawable.placeholder);
                requestOptions.error(R.drawable.ic_error_placeholder);
                Glide.with(CatererProfileActivity.this.getApplicationContext())
                        .setDefaultRequestOptions(requestOptions)
                        .load(uri)
                        .into(userImage);
            }).addOnFailureListener(exception -> userImage.setImageResource(R.drawable.ic_error_placeholder));
        }
        callButton.setOnClickListener(this);
        emailButton.setOnClickListener(this);
        parentLayout.setVisibility(View.VISIBLE);
        loadingDialog.dismiss();
    }

    private void initViews() {
        userImage = findViewById(R.id.act_user_prof_image);
        userNameTxtView = findViewById(R.id.act_user_prof_name);
        addressLine1TxtView = findViewById(R.id.act_user_prof_address_1);
        addressLine2TxtView = findViewById(R.id.act_user_prof_address_2);
        callButton = findViewById(R.id.act_user_prof_call_btn);
        emailButton = findViewById(R.id.act_user_prof_email);
        parentLayout = findViewById(R.id.act_user_prof_parent_layout);

        //TODO: initialize error layout
        parentLayout.setVisibility(View.GONE);
        //TODO: Set error layout visibility as GONE
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == callButton.getId()) {
            String phoneNumber = userDetails.getUserPhone();
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(android.Manifest.permission.CALL_PHONE)
                        == PackageManager.PERMISSION_GRANTED) {
                    startActivity(callIntent);
                }
            }
        } else if (v.getId() == emailButton.getId()) {
            if (userDetails.getUserEmail() != null) {
                String emailAddress = userDetails.getUserEmail();
                Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                sendIntent.setData(Uri.parse("mailto:"));
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Enquiry about vending services.");
                sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
                sendIntent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(sendIntent);
            }
        }
    }
}
