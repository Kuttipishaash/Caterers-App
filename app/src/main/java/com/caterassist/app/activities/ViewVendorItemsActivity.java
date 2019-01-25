package com.caterassist.app.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caterassist.app.R;
import com.caterassist.app.adapters.VendorItemsAdapter;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.models.VendorItem;
import com.caterassist.app.utils.Constants;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class ViewVendorItemsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "VendorItemsViewAct";
    private String vendorUID;
    private UserDetails vendorDetails;

    private DatabaseReference vendorItemsReference;
    private DatabaseReference vendorInfoReference;
    private ChildEventListener vendorItemsEventListener;
    private RecyclerView vendorItemsRecyclerView;
    private ArrayList<VendorItem> vendorItemsArrayList;
    private LinearLayoutManager vendorItemsLayoutManager;
    private VendorItemsAdapter vendorItemsAdapter;
    private androidx.appcompat.widget.Toolbar toolbar;
    private LinearLayout emailVendorImageBtn;
    private LinearLayout callVendorImageBtn;
    private LinearLayout addToFavoutitesImageBtn;
    private ImageButton addToFavoutitesIcon;
    private ImageView vendorImageView;
    private TextView vendorNameTextView;
    private TextView vendorAddressTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_vendor_items);
        Intent intent = getIntent();
        if (intent.getStringExtra(Constants.IntentExtrasKeys.VIEW_VENDOR_ITEMS_INTENT_VENDOR_UID) != null) {
            vendorUID = intent.getStringExtra(Constants.IntentExtrasKeys.VIEW_VENDOR_ITEMS_INTENT_VENDOR_UID);
            initViews();
            fetchVendorDetails();
        } else {
            Toasty.error(this, "No vendor data", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ViewVendorItemsActivity.this, VendorHomeActivity.class));
            finish();
        }
        initComponent();
    }

    private void initComponent() {
        final CircularImageView image = findViewById(R.id.act_vendor_list_vendor_image);
        final CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        ((AppBarLayout) findViewById(R.id.app_bar_layout)).addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int min_height = ViewCompat.getMinimumHeight(collapsing_toolbar) * 2;
                float scale = (float) (min_height + verticalOffset) / min_height;
                image.setScaleX(scale >= 0 ? scale : 0);
                image.setScaleY(scale >= 0 ? scale : 0);
            }
        });
    }

    private void fetchVendorDetails() {
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() +
                FirebaseUtils.USER_INFO_BRANCH_NAME +
                vendorUID;
        vendorInfoReference = FirebaseDatabase.getInstance().getReference(databasePath);
        vendorInfoReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                vendorDetails = dataSnapshot.getValue(UserDetails.class);
                vendorDetails.setUserID(vendorUID);
                vendorItemsAdapter = new VendorItemsAdapter();
                vendorItemsAdapter.setParentActivity(ViewVendorItemsActivity.this);
                vendorItemsAdapter.setVendorDetails(vendorDetails);
                vendorItemsAdapter.setVendorItemArrayList(vendorItemsArrayList);
                vendorItemsLayoutManager = new LinearLayoutManager(ViewVendorItemsActivity.this, RecyclerView.VERTICAL, false);
                vendorItemsRecyclerView.setLayoutManager(vendorItemsLayoutManager);
                vendorItemsRecyclerView.setAdapter(vendorItemsAdapter);
                //Set vendor image
                String imageUrl = vendorDetails.getUserImageUrl();
                if (imageUrl != null) {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                    storageReference.child(imageUrl).getDownloadUrl().addOnSuccessListener(uri -> {
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions.placeholder(R.drawable.placeholder);
                        requestOptions.error(R.drawable.ic_error_placeholder);
                        Glide.with(ViewVendorItemsActivity.this.getApplicationContext())
                                .setDefaultRequestOptions(requestOptions)
                                .load(uri)
                                .into(vendorImageView);
                    }).addOnFailureListener(exception -> vendorImageView.setImageResource(R.drawable.ic_error_placeholder));
                }
                vendorNameTextView.setText(vendorDetails.getUserName());
                String location = vendorDetails.getUserLocationName() + ", " + vendorDetails.getUserDistrictName();
                vendorAddressTextView.setText(location);
                toolbar.setTitle(vendorDetails.getUserName());
                fetchVendorItems();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchVendorItems() {
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() +
                FirebaseUtils.VENDOR_LIST_BRANCH_NAME +
                vendorUID;

        vendorItemsReference = FirebaseDatabase.getInstance().getReference(databasePath);
        vendorItemsEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new VendorItem has been added, add it to the displayed list
                VendorItem vendorItem = dataSnapshot.getValue(VendorItem.class);
                vendorItem.setId(dataSnapshot.getKey());
                vendorItemsArrayList.add(vendorItem);
                vendorItemsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A VendorItem has changed, use the key to determine if we are displaying this
                // VendorItem and if so displayed the changed VendorItem.
                VendorItem vendorItem = dataSnapshot.getValue(VendorItem.class);
                String vendorItemKey = dataSnapshot.getKey();
                for (int i = 0; i < vendorItemsArrayList.size(); i++) {
                    if (vendorItemsArrayList.get(i).getId().equals(vendorItemKey)) {
                        vendorItemsArrayList.remove(i);
                        vendorItem.setId(dataSnapshot.getKey());
                        vendorItemsArrayList.add(i, vendorItem);
                        vendorItemsAdapter.notifyDataSetChanged();
                        break;
                    }
                }
                // ...
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A VendorItem has changed, use the key to determine if we are displaying this
                // VendorItem and if so remove it.
                String vendorItemKey = dataSnapshot.getKey();
                for (int i = 0; i < vendorItemsArrayList.size(); i++) {
                    if (vendorItemsArrayList.get(i).getId().equals(vendorItemKey)) {
                        vendorItemsArrayList.remove(i);
                        vendorItemsAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A VendorItem has changed position, use the key to determine if we are
                // displaying this VendorItem and if so move it.
                VendorItem vendorItem = dataSnapshot.getValue(VendorItem.class);
                String vendorItemKey = dataSnapshot.getKey();
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(ViewVendorItemsActivity.this, "Failed to load favourite vendors.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        vendorItemsReference.addChildEventListener(vendorItemsEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vendorItemsEventListener != null) {
            vendorItemsReference.removeEventListener(vendorItemsEventListener);
        }
    }

    private void initViews() {
        vendorItemsRecyclerView = findViewById(R.id.act_vendor_items_recyc_items);
        vendorItemsArrayList = new ArrayList<>();

        toolbar = findViewById(R.id.vendor_items_toolbar);

        addToFavoutitesImageBtn = findViewById(R.id.act_vendor_add_to_fav);
        addToFavoutitesIcon = findViewById(R.id.add_fav_icon);
        callVendorImageBtn = findViewById(R.id.act_vendor_call_vendor);
        emailVendorImageBtn = findViewById(R.id.act_vendor_mail_vendor);
        vendorImageView = findViewById(R.id.act_vendor_list_vendor_image);
        vendorNameTextView = findViewById(R.id.act_vending_list_vend_name);
        vendorAddressTextView = findViewById(R.id.act_vending_list_vend_address);

        addToFavoutitesImageBtn.setOnClickListener(this);
        emailVendorImageBtn.setOnClickListener(this);
        callVendorImageBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.act_vendor_add_to_fav:
                addToFavoutitesIcon.setImageResource(R.drawable.ic_favorite);
                addVendorToFavourites();
                break;
            case R.id.act_vendor_call_vendor:
                if (vendorDetails.getUserPhone() != null) {
                    String phoneNumber = vendorDetails.getUserPhone();
                    Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (checkSelfPermission(android.Manifest.permission.CALL_PHONE)
                                == PackageManager.PERMISSION_GRANTED) {
                            startActivity(callIntent);
                        }
                    }
                }

                break;
            case R.id.act_vendor_mail_vendor:
                if (vendorDetails.getUserEmail() != null) {
                    String emailAddress = vendorDetails.getUserEmail();
                    Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                    sendIntent.setData(Uri.parse("mailto:"));
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Enquiry about vending services.");
                    sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "");
                    startActivity(sendIntent);
                }
                break;
        }
    }

    private void addVendorToFavourites() {
        String userInfoDatabasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.USER_INFO_BRANCH_NAME + vendorUID;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(userInfoDatabasePath);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserDetails vendorDetails = dataSnapshot.getValue(UserDetails.class);
                String favouriteUserPath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.FAVOURITE_VENDORS_BRANCH_NAME
                        + FirebaseAuth.getInstance().getUid();
                DatabaseReference favouriteUsersDbRef = FirebaseDatabase.getInstance().getReference(favouriteUserPath);
                favouriteUsersDbRef.child(vendorUID).setValue(vendorDetails)
                        .addOnSuccessListener(aVoid -> Toasty.success(ViewVendorItemsActivity.this, "Vendor added to favourites").show())
                        .addOnFailureListener(e -> Toasty.error(ViewVendorItemsActivity.this, "Vendor couldn't be added to favourites. Try Again!").show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toasty.error(ViewVendorItemsActivity.this, "Vendor couldn't be added to favourites. Try Again!").show();
            }
        });
    }
}
