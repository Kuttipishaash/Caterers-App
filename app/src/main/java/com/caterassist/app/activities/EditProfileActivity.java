package com.caterassist.app.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

public class EditProfileActivity extends Activity implements View.OnClickListener {
    private UserDetails userDetails;
    private static final String TAG = "ProfileAct";
    public static final int REQUEST_IMAGE = 100;
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
    private AwesomeValidation awesomeValidation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
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
                Glide.with(EditProfileActivity.this)
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
        setValidation();
        saveBtn.setOnClickListener(this);
        editImageBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.act_prof_save_btn) {
            if (imageChanged && awesomeValidation.validate()) {
                uploadNewImage();

            } else {
                if (awesomeValidation.validate()) {
                    changeData();
                }
            }
        } else if (v.getId() == R.id.act_prof_user_img_change) {
            pickImage();
        }
    }

    private void changeData() {
        if (nameEdtTxt.getText() != null)
            userDetails.setUserName(nameEdtTxt.getText().toString());
        if (streetEdtTxt.getText() != null)
            userDetails.setUserStreetName(streetEdtTxt.getText().toString());
        if (localityEdtTxt.getText() != null)
            userDetails.setUserLocationName(localityEdtTxt.getText().toString());
        if (districtEdtTxt.getText() != null)
            userDetails.setUserDistrictName(districtEdtTxt.getText().toString());
        userDetails.setUserID(FirebaseAuth.getInstance().getUid());
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.USER_INFO_BRANCH_NAME + FirebaseAuth.getInstance().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
        databaseReference.setValue(userDetails).addOnSuccessListener(aVoid -> {
            Toasty.success(this, "Profile changed successfully!").show();
            AppUtils.setUserInfoSharedPreferences(userDetails, EditProfileActivity.this);
            imageChanged = false;
            finish();
        }).addOnFailureListener(e -> Toasty.error(EditProfileActivity.this, "User profile update failed!").show());
    }

    private void uploadNewImage() {
        if (profileImageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("images/" + userDetails.getUserEmail());
            UploadTask uploadTask = storageRef.putFile(profileImageUri);
            uploadTask.addOnFailureListener(exception
                    -> Toasty.error(EditProfileActivity.this, "Registration request failed").show())
                    .addOnSuccessListener(taskSnapshot
                            -> {
                        Log.i(TAG, "editProfile: Image uploaded.");
                        if (taskSnapshot.getMetadata() != null) {
                            String imagePath = taskSnapshot.getMetadata().getPath();
                            userDetails.setUserImageUrl(imagePath);
                            if (awesomeValidation.validate())
                                changeData();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.i(TAG, "editProfile: Image upload failed.");
                        Toasty.error(this, "User profile update failed!").show();
                    });
        }
    }

//    private void setNewImage() {
//        imagePicker = new ImagePicker(this,
//                null,
//                imageUri -> {/*on image picked */
//                    profileImage.setImageURI(imageUri);
//                    try {
//                        File file = new File(imageUri.getPath());
//                        Bitmap compressedImageBitmap = new Compressor(this).compressToBitmap(file);
//                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//                        compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
//                        String path = MediaStore.Images.Media.insertImage(EditProfileActivity.this.getContentResolver(), compressedImageBitmap, "catering_app_profile_picture", null);
//                        profileImageUri = Uri.parse(path);
//                        imageChanged = true;
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                })
//                .setWithImageCrop(
//                        1, 1);
//        imagePicker.choosePicture(true /*show camera intents*/);
//    }

    void pickImage() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            showImagePickerOptions();
                        } else {
                            Toasty.error(EditProfileActivity.this, "You cannot use this feature without giving permission.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void showImagePickerOptions() {
        ImagePickerActivity.showImagePickerOptions(this, new ImagePickerActivity.PickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
                launchCameraIntent();
            }

            @Override
            public void onChooseGallerySelected() {
                launchGalleryIntent();
            }
        });
    }

    private void launchCameraIntent() {
        Intent intent = new Intent(EditProfileActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);

        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void launchGalleryIntent() {
        Intent intent = new Intent(EditProfileActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getParcelableExtra("path");
                try {
                    // You can update this bitmap to your server
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    profileImage.setImageURI(uri);
                    profileImageUri = uri;
                    try {
                        File file = new File(uri.getPath());
                        Bitmap compressedImageBitmap = new Compressor(this).compressToBitmap(file);
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
                        String path = MediaStore.Images.Media.insertImage(EditProfileActivity.this.getContentResolver(), compressedImageBitmap, "catering_app_profile_picture", null);
                        profileImageUri = Uri.parse(path);
                        imageChanged = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // loading profile image from local cache
//                    loadProfile(uri.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setValidation() {
        awesomeValidation.addValidation(this, nameEdtTxt.getId(), "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.addr_error);
        awesomeValidation.addValidation(this, streetEdtTxt.getId(), "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.addr_error);
        awesomeValidation.addValidation(this, localityEdtTxt.getId(), "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.addr_error);
        awesomeValidation.addValidation(this, districtEdtTxt.getId(), "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.addr_error);
    }
}
