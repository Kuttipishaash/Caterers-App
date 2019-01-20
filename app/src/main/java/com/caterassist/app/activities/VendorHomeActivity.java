package com.caterassist.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caterassist.app.R;
import com.caterassist.app.adapters.VendingItemsAdapter;
import com.caterassist.app.fragments.BottomNavigationDrawerFragment;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.models.VendorItem;
import com.caterassist.app.utils.AppUtils;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class VendorHomeActivity extends FragmentActivity implements View.OnClickListener {

    private static final String TAG = "VendorDash";
    private static final int CALL_PERMISSION_REQ_CODE = 100;
    TextView awaitingOrderNumberTxtView;
    private BottomAppBar bottomAppBar;
    private FloatingActionButton addEditItemFAB;
    private DatabaseReference vendingItemsReference;
    private ChildEventListener vendingItemsEventListener;
    private ArrayList<VendorItem> vendingItemsArrayList;
    private LinearLayoutManager vendingItemsLayoutManager;
    private VendingItemsAdapter vendingItemsAdapter;
    private RecyclerView vendingItemsRecyclerView;
    private Toolbar toolbar;
    private Integer approvalAwaitingOrders;
    private FloatingActionButton awaitingOrdersFab;
    private FloatingActionButton orderHistoryFab;
    private ImageView viewProfileFab;
    private TextView profileName;
    private TextView profileLocation;


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_home);
        getPermissions();


    }

    private void initViews() {
        vendingItemsRecyclerView = findViewById(R.id.frag_vend_recyc_vending_items);
        toolbar = findViewById(R.id.vendor_dash_toolbar);
        awaitingOrderNumberTxtView = findViewById(R.id.frag_vend_dash_awaiting_orders);
        awaitingOrdersFab = findViewById(R.id.frag_vend_dash_awaiting_orders_fab);
        orderHistoryFab = findViewById(R.id.frag_vend_dash_order_history);
        viewProfileFab = findViewById(R.id.vendor_view_profile);
        profileName = findViewById(R.id.vendor_home_name);
        profileLocation = findViewById(R.id.vendor_home_location);

        addEditItemFAB = findViewById(R.id.act_home_fab);
        bottomAppBar = findViewById(R.id.bottom_app_bar_vendor);

        awaitingOrdersFab.setOnClickListener(this);
        orderHistoryFab.setOnClickListener(this);
        viewProfileFab.setOnClickListener(this);
    }

    private void fetchPendingOrders() {
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.ORDERS_AWAITING_APPROVAL + FirebaseAuth.getInstance().getUid();
        DatabaseReference awaitingOrdersReference = FirebaseDatabase.getInstance().getReference(databasePath);
        awaitingOrdersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    approvalAwaitingOrders = dataSnapshot.getValue(Integer.class);
                    if (approvalAwaitingOrders != null) {
                        if (approvalAwaitingOrders == 0) {
                            awaitingOrderNumberTxtView.setText("No Pending Orders");
                        } else {
                            String pendingText = approvalAwaitingOrders + " pending orders";
                            awaitingOrderNumberTxtView.setText(pendingText);
                        }
                    } else {
                        awaitingOrderNumberTxtView.setText("No Pending Orders");
                    }

                } catch (NullPointerException e) {
                    approvalAwaitingOrders = 0;
                    awaitingOrderNumberTxtView.setText("No Pending Orders");
                    Log.e(TAG, "onDataChange: Approval awaiting order variable null in firebase");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: Failed to fetch pending orders");
            }
        });
    }

    private void fetchItems() {
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() +
                FirebaseUtils.VENDOR_LIST_BRANCH_NAME +
                FirebaseAuth.getInstance().getUid();

        vendingItemsReference = FirebaseDatabase.getInstance().getReference(databasePath);
        vendingItemsEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new VendorItem has been added, add it to the displayed list
                VendorItem cartItem = dataSnapshot.getValue(VendorItem.class);
                cartItem.setId(dataSnapshot.getKey());
                vendingItemsArrayList.add(cartItem);
                vendingItemsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A VendorItem has changed, use the key to determine if we are displaying this
                // VendorItem and if so displayed the changed VendorItem.
                VendorItem cartItem = dataSnapshot.getValue(VendorItem.class);
                String cartItemKey = dataSnapshot.getKey();
                for (int i = 0; i < vendingItemsArrayList.size(); i++) {
                    if (vendingItemsArrayList.get(i).getId().equals(cartItemKey)) {
                        vendingItemsArrayList.remove(i);
                        cartItem.setId(dataSnapshot.getKey());
                        vendingItemsArrayList.add(i, cartItem);
                        vendingItemsAdapter.notifyDataSetChanged();
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
                String cartItemKey = dataSnapshot.getKey();
                for (int i = 0; i < vendingItemsArrayList.size(); i++) {
                    if (vendingItemsArrayList.get(i).getId().equals(cartItemKey)) {
                        vendingItemsArrayList.remove(i);
                        vendingItemsAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A VendorItem has changed position, use the key to determine if we are
                // displaying this VendorItem and if so move it.
                VendorItem cartItem = dataSnapshot.getValue(VendorItem.class);
                String cartItemKey = dataSnapshot.getKey();
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: Fetching cancelled");
                Log.w(TAG, "onCancelled", databaseError.toException());
                Toast.makeText(VendorHomeActivity.this, "Failed to load cart items.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        vendingItemsReference.addChildEventListener(vendingItemsEventListener);
        vendingItemsAdapter = new VendingItemsAdapter();
        vendingItemsAdapter.setVendingItemArrayList(vendingItemsArrayList);
        vendingItemsAdapter.setActivity(this);
        vendingItemsLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        vendingItemsRecyclerView.setLayoutManager(vendingItemsLayoutManager);
        vendingItemsRecyclerView.setAdapter(vendingItemsAdapter);
        vendingItemsRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.frag_vend_dash_awaiting_orders_fab) {
            startActivity(new Intent(this, VendorPendingOrdersActivity.class));
        } else if (v.getId() == R.id.vendor_view_profile) {
            startActivity(new Intent(this, EditProfileActivity.class));
        } else if (v.getId() == R.id.act_home_fab) {

            startActivity(new Intent(VendorHomeActivity.this, AddEditItemActivity.class));
        } else if (v.getId() == R.id.frag_vend_dash_order_history) {

            startActivity(new Intent(VendorHomeActivity.this, OrderHistoryActivity.class));
        }

    }

    private void getPermissions() {
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            } else {
                doIfPermissionGranted();
            }
        } else {
            doIfPermissionGranted();
        }
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_REQ_CODE);

    }

    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
        return result == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CALL_PERMISSION_REQ_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doIfPermissionGranted();
                } else {
                    Toasty.warning(this, "You need to provide call permissions to use all the features of this app.", Toast.LENGTH_SHORT).show();
                    AppUtils.cleanUpAndLogout(this);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void doIfPermissionGranted() {
        initViews();
        setupBottomAppBar();
        vendingItemsArrayList = new ArrayList<>();

        UserDetails userDetails = AppUtils.getUserInfoSharedPreferences(this);

        String title = "Hi, " + userDetails.getUserName();
        profileName.setText(title);
        toolbar.setTitle(title);
        String subtitle = userDetails.getUserLocationName() + ", " + userDetails.getUserDistrictName();
        profileLocation.setText(subtitle);
        toolbar.setSubtitle(subtitle);
        String imageUrl = userDetails.getUserImageUrl();
        if (imageUrl != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child(imageUrl).getDownloadUrl().addOnSuccessListener(uri -> {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.drawable.placeholder);
                requestOptions.error(R.drawable.ic_error_placeholder);
                Glide.with(VendorHomeActivity.this)
                        .setDefaultRequestOptions(requestOptions)
                        .load(uri)
                        .into(viewProfileFab);
            }).addOnFailureListener(exception -> viewProfileFab.setImageResource(R.drawable.ic_error_placeholder));
        }


        fetchItems();
        fetchPendingOrders();
        addEditItemFAB.setOnClickListener(this);
    }


    private void setupBottomAppBar() {
        bottomAppBar.replaceMenu(R.menu.bottom_bar_overflow_menu_vendor);

        bottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialogFragment bottomSheetDialogFragment = new BottomNavigationDrawerFragment();
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
            }
        });
        bottomAppBar.setOnMenuItemClickListener(new androidx.appcompat.widget.Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.btm_sheet_vendor_order_history:
                        startActivity(new Intent(VendorHomeActivity.this, OrderHistoryActivity.class));
                        break;
                }
                return true;
            }
        });
    }


}
