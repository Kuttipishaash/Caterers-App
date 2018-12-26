package com.caterassist.app.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.caterassist.app.R;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.utils.AppUtils;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import es.dmoral.toasty.Toasty;

public class ProfileActivity extends Activity implements View.OnClickListener {
    private UserDetails userDetails;
    private static final String TAG = "ProfileAct";
    //Views
    private TextView emailTxtView;
    private TextView phoneTxtView;
    private EditText nameEdtTxt;
    private EditText localityEdtTxt;
    private EditText streetEdtTxt;
    private EditText districtEdtTxt;
    private ImageView profileImage;
    private Button saveBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initViews();
        userDetails = AppUtils.getUserInfoSharedPreferences(this);
        setInitialValues();
    }

    private void setInitialValues() {
        nameEdtTxt.setText(userDetails.getUserName());
        emailTxtView.setText(userDetails.getUserEmail());
        phoneTxtView.setText(userDetails.getUserPhone());
        streetEdtTxt.setText(userDetails.getUserStreetName());
        localityEdtTxt.setText(userDetails.getUserLocationName());
        districtEdtTxt.setText(userDetails.getUserDistrictName());
        if (userDetails.getUserImageUrl() != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child(userDetails.getUserImageUrl()).getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(ProfileActivity.this)
                        .load(uri)
                        .into(profileImage);
            }).addOnFailureListener(exception -> Log.i(TAG, "setInitialValues: No profile image link"));
        }
    }

    private void initViews() {
        emailTxtView = findViewById(R.id.act_prof_user_email);
        phoneTxtView = findViewById(R.id.act_prof_user_phone);
        nameEdtTxt = findViewById(R.id.act_prof_user_name);
        localityEdtTxt = findViewById(R.id.act_prof_locality_addr);
        streetEdtTxt = findViewById(R.id.act_prof_street_addr);
        districtEdtTxt = findViewById(R.id.act_prof_district_addr);
        profileImage = findViewById(R.id.act_prof_user_img);
        saveBtn = findViewById(R.id.act_prof_save_btn);

        saveBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.act_prof_save_btn) {
            if (validateInputFields()) {
                String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.USER_INFO_BRANCH_NAME + FirebaseAuth.getInstance().getUid();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
                databaseReference.setValue(userDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toasty.success(ProfileActivity.this, "User profile updated successfully.", Toast.LENGTH_SHORT).show();
                        AppUtils.setUserInfoSharedPreferences(userDetails, ProfileActivity.this);
                        finish();
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toasty.error(ProfileActivity.this, "User profile update failed!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    private boolean validateInputFields() {
        //TODO: validate all edit text fields
        userDetails.setUserName(nameEdtTxt.getText().toString());
        userDetails.setUserStreetName(streetEdtTxt.getText().toString());
        userDetails.setUserDistrictName(districtEdtTxt.getText().toString());
        userDetails.setUserLocationName(localityEdtTxt.getText().toString());
        userDetails.setUserID(FirebaseAuth.getInstance().getUid());
        //TODO: user image update
        return true;
    }
}
