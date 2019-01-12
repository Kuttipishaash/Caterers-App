package com.caterassist.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.Toolbar;

import com.caterassist.app.R;
import com.caterassist.app.adapters.VendorItemsAdapter;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.models.VendorItem;
import com.caterassist.app.utils.Constants;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class ViewVendorItemsActivity extends Activity {

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
        final CircularImageView image = findViewById(R.id.image);
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
                vendorItemsAdapter.setVendorDetails(vendorDetails);
                vendorItemsAdapter.setVendorItemArrayList(vendorItemsArrayList);
                vendorItemsLayoutManager = new LinearLayoutManager(ViewVendorItemsActivity.this, RecyclerView.VERTICAL, false);
                vendorItemsRecyclerView.setLayoutManager(vendorItemsLayoutManager);
                vendorItemsRecyclerView.setAdapter(vendorItemsAdapter);
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
    }
}
