package com.caterassist.app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.caterassist.app.R;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.myhexaville.smartimagepicker.ImagePicker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;


public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int PICK_IMAGE = 1;
    public static final String TAG = "SignUpActivity";
    private Uri imageFileUri;
    private TextInputEditText nameEdtTxt;
    private TextInputEditText emailEdtTxt;
    private TextInputEditText phoneEdtTxt;
    private TextInputEditText streetEdtTxt;
    private TextInputEditText locationEdtTxt;
    private TextInputEditText districtEdtTxt;
    private Button submitBtn;
    private ImageButton chooseImageBtn;
    private ImageView userProfileImageView;
    private TextInputEditText passwordEdtTxt;
    private TextInputEditText passwordReEdtTxt;
    private AwesomeValidation awesomeValidation;
    private UserDetails userDetails;
    private ImagePicker imagePicker;
    private RadioGroup catergoryRadGrp;
    private RadioButton catererRadBtn, vendorRadBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        initViews();
        setValidation();
    }


    private void initViews() {
        nameEdtTxt = findViewById(R.id.act_sign_name_edt_txt);
        emailEdtTxt = findViewById(R.id.act_sign_email_edt_txt);
        phoneEdtTxt = findViewById(R.id.act_sign_phone_edt_txt);
        streetEdtTxt = findViewById(R.id.act_sign_street_edt_txt);
        locationEdtTxt = findViewById(R.id.act_sign_location_edt_txt);
        districtEdtTxt = findViewById(R.id.act_sign_district_edt_txt);
        submitBtn = findViewById(R.id.act_sign_submit);
        chooseImageBtn = findViewById(R.id.act_sign_user_img_choose_btn);
        userProfileImageView = findViewById(R.id.act_sign_user_img);
        passwordEdtTxt = findViewById(R.id.act_sign_passwd_edt_txt);
        passwordReEdtTxt = findViewById(R.id.act_sign_re_passwd_edt_txt);
        catergoryRadGrp = findViewById(R.id.act_sign_category_rad_grp);
        catererRadBtn = findViewById(R.id.act_sign_category_caterer);
        vendorRadBtn = findViewById(R.id.act_sign_category_vendor);
        catergoryRadGrp.check(catererRadBtn.getId());
        submitBtn.setOnClickListener(this);
        chooseImageBtn.setOnClickListener(this);
    }

    private void setValidation() {
        awesomeValidation.addValidation(this, R.id.act_sign_name_edt_txt, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.name_error);
        awesomeValidation.addValidation(this, R.id.act_sign_email_edt_txt, Patterns.EMAIL_ADDRESS, R.string.email_error);
        awesomeValidation.addValidation(this, R.id.act_sign_phone_edt_txt, "^[0-9]{9,10}$", R.string.mobile_error);
        awesomeValidation.addValidation(this, R.id.act_sign_passwd_edt_txt, "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", R.string.pass_error);
        awesomeValidation.addValidation(this, R.id.act_sign_street_edt_txt, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.addr_error);
        awesomeValidation.addValidation(this, R.id.act_sign_location_edt_txt, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.addr_error);
        awesomeValidation.addValidation(this, R.id.act_sign_district_edt_txt, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.addr_error);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == submitBtn.getId()) {
            if (awesomeValidation.validate()) {
                signUp();
            }
        } else if (v.getId() == chooseImageBtn.getId()) {
            imagePicker = new ImagePicker(this,
                    null,
                    imageUri -> {/*on image picked */
                        userProfileImageView.setImageURI(imageUri);
                        imageFileUri = imageUri;
                        try {
                            File file = new File(imageUri.getPath());
                            Bitmap compressedImageBitmap = new Compressor(this).compressToBitmap(file);
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
                            String path = MediaStore.Images.Media.insertImage(SignUpActivity.this.getContentResolver(), compressedImageBitmap, "catering_app_profile_picture", null);
                            imageFileUri = Uri.parse(path);
                            userProfileImageView.setImageURI(imageFileUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    })
                    .setWithImageCrop(
                            1, 1);
            imagePicker.choosePicture(true /*show camera intents*/);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePicker.handleActivityResult(resultCode, requestCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        imagePicker.handlePermission(requestCode, grantResults);
    }


    private void signUp() {
        if (passwordEdtTxt.getText() != null) {
            if (!passwordEdtTxt.getText().toString().equals(passwordReEdtTxt.getText().toString())) {
                passwordEdtTxt.setError("Passwords do not match");
                return;
            }
        }
        userDetails = new UserDetails();
        //validate inputs
        if (nameEdtTxt.getText() != null)
            userDetails.setUserName(nameEdtTxt.getText().toString());
        if (emailEdtTxt.getText() != null)
            userDetails.setUserEmail(emailEdtTxt.getText().toString());
        if (phoneEdtTxt.getText() != null)
            userDetails.setUserPhone(phoneEdtTxt.getText().toString());
        if (streetEdtTxt.getText() != null)
            userDetails.setUserStreetName(streetEdtTxt.getText().toString());
        if (locationEdtTxt.getText() != null)
            userDetails.setUserLocationName(locationEdtTxt.getText().toString());
        if (districtEdtTxt.getText() != null)
            userDetails.setUserDistrictName(districtEdtTxt.getText().toString());
        if (imageFileUri == null) {
            imageFileUri = Uri.parse("android.resource://com.caterassist.app/drawable/user_placeholder");
        }
        int checkedRadBtnId = catergoryRadGrp.getCheckedRadioButtonId();
        if (checkedRadBtnId == catererRadBtn.getId()) {
            userDetails.setIsVendor(false);
        } else {
            userDetails.setIsVendor(true);
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("images/" + userDetails.getUserEmail());
        UploadTask uploadTask = storageRef.putFile(imageFileUri);
        uploadTask.addOnFailureListener(exception
                -> Toasty.error(SignUpActivity.this, "Registration request failed!", Toast.LENGTH_LONG).show())
                .addOnSuccessListener(taskSnapshot
                        -> {
                    Log.i(TAG, "signUp: Image uploaded.");
                    String imagePath = taskSnapshot.getMetadata().getPath();
                    userDetails.setUserImageUrl(imagePath);
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    firebaseAuth.createUserWithEmailAndPassword(userDetails.getUserEmail(), passwordEdtTxt.getText().toString())
                            .addOnCompleteListener(this, task -> {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.USER_PENDING_REGISTRATION_BRANCH;
                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
                                    databaseReference.child(FirebaseAuth.getInstance().getUid()).setValue(userDetails)
                                            .addOnSuccessListener(aVoid -> {
                                                Toasty.success(SignUpActivity.this, "Registration request recorded successfully.", Toast.LENGTH_LONG).show();
                                                finish();
                                            })
                                            .addOnFailureListener(e ->
                                                    Toasty.error(SignUpActivity.this, "Registration request failed!", Toast.LENGTH_LONG).show());

                                    Log.d(TAG, "createUserWithEmail:success");
                                    firebaseAuth.signOut();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toasty.error(SignUpActivity.this, "Registration request failed!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                });
    }
}
