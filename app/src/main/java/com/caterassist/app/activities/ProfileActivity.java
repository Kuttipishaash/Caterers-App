package com.caterassist.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caterassist.app.R;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.utils.AppUtils;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.myhexaville.smartimagepicker.ImagePicker;

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
    private ImageButton editImageBtn;
    private Uri profileImageUri;
    private boolean imageChanged;
    private ImagePicker imagePicker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initViews();
        userDetails = AppUtils.getUserInfoSharedPreferences(this);
        setInitialValues();
    }

    private void setInitialValues() {
        profileImageUri = null;
        imageChanged = false;
        nameEdtTxt.setText(userDetails.getUserName());
        emailTxtView.setText(userDetails.getUserEmail());
        phoneTxtView.setText(userDetails.getUserPhone());
        streetEdtTxt.setText(userDetails.getUserStreetName());
        localityEdtTxt.setText(userDetails.getUserLocationName());
        districtEdtTxt.setText(userDetails.getUserDistrictName());
        if (userDetails.getUserImageUrl() != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child(userDetails.getUserImageUrl()).getDownloadUrl().addOnSuccessListener(uri -> {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.drawable.user_placeholder);
                requestOptions.error(R.drawable.ic_error_placeholder);
                Glide.with(ProfileActivity.this)
                        .setDefaultRequestOptions(requestOptions)
                        .load(uri)
                        .into(profileImage);
                profileImageUri = uri;
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
        editImageBtn = findViewById(R.id.act_prof_user_img_change);

        saveBtn.setOnClickListener(this);
        editImageBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.act_prof_save_btn) {
            if (imageChanged && validateInputFields()) {
                uploadNewImage();

            } else {
                if (validateInputFields()) {
                    changeData();
                }
            }
        } else if (v.getId() == R.id.act_prof_user_img_change) {
            setNewImage();
        }
    }

    private void changeData() {
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.USER_INFO_BRANCH_NAME + FirebaseAuth.getInstance().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
        databaseReference.setValue(userDetails).addOnSuccessListener(aVoid -> {
            Toasty.error(this, "Changing profile failed!").show();
            AppUtils.setUserInfoSharedPreferences(userDetails, ProfileActivity.this);
            finish();
        }).addOnFailureListener(e -> Toasty.error(ProfileActivity.this, "User profile update failed!").show());
    }

    private void uploadNewImage() {


        if (profileImageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("images/" + userDetails.getUserEmail());
            UploadTask uploadTask = storageRef.putFile(profileImageUri);
            uploadTask.addOnFailureListener(exception
                    -> Toasty.error(ProfileActivity.this, "Registration request failed").show())
                    .addOnSuccessListener(taskSnapshot
                            -> {
                        Log.i(TAG, "editProfile: Image uploaded.");
                        if (taskSnapshot.getMetadata() != null) {
                            String imagePath = taskSnapshot.getMetadata().getPath();
                            userDetails.setUserImageUrl(imagePath);
                            changeData();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.i(TAG, "editProfile: Image upload failed.");
                        Toasty.error(this, "User profile update failed!").show();
                    });

        }
    }

    private void setNewImage() {
        imagePicker = new ImagePicker(this,
                null,
                imageUri -> {/*on image picked */
                    profileImage.setImageURI(imageUri);
                    profileImageUri = imageUri;
                })
                .setWithImageCrop(
                        1, 1);
        imagePicker.choosePicture(true /*show camera intents*/);
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePicker.handleActivityResult(resultCode, requestCode, data);
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
