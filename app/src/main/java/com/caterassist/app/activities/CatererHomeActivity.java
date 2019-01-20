package com.caterassist.app.activities;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.adapters.FavouriteVendorsAdapter;
import com.caterassist.app.adapters.VendorListAdapter;
import com.caterassist.app.fragments.BottomNavigationDrawerFragment;
import com.caterassist.app.models.FavouriteVendor;
import com.caterassist.app.models.UserDetails;
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

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class CatererHomeActivity extends FragmentActivity implements View.OnClickListener {
    private static final String TAG = "CatererDash";
    private static final int CALL_PERMISSION_REQ_CODE = 100;
    private ArrayList<UserDetails> allVendorsArrayList;
    //TODO: toolbar
    private DatabaseReference favouriteVendorsReference;
    private ChildEventListener favouriteVendorsEventListener;
    private RecyclerView favouriteVendorsRecyclerView;
    private LinearLayoutManager favouriteVendorsLayoutManager;
    private FavouriteVendorsAdapter favouriteVendorsAdapter;
    private ArrayList<UserDetails> favouriteVendorArrayList;
    private DatabaseReference allVendorsRef;
    private LinearLayoutManager allVendorsLayoutManager;
    private VendorListAdapter allVendorsAdapter;
    private RecyclerView allVendorsRecyclerView;
    private FloatingActionButton viewProfileFAB, viewOrderHistoryFAB, viewCartFAB;
    private BottomAppBar bottomAppBar;
    private SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caterer_home);
        getPermissions();
    }

    private void getPermissions() {
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.M) {
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
        fetchFavouriteVendors();
        fetchAllVendors();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (favouriteVendorsEventListener != null) {
            favouriteVendorsReference.removeEventListener(favouriteVendorsEventListener);
        }
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    private void setupBottomAppBar() {
        bottomAppBar.replaceMenu(R.menu.bottom_bar_overflow_menu_caterer);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) bottomAppBar.getMenu().findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                allVendorsAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                allVendorsAdapter.getFilter().filter(newText);
                return false;
            }
        });

        bottomAppBar.setNavigationOnClickListener(v -> {
            BottomSheetDialogFragment bottomSheetDialogFragment = new BottomNavigationDrawerFragment();
            bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
        });
       /* bottomAppBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.btm_sheet_option_cart:
                    startActivity(new Intent(CatererHomeActivity.this, CartActivity.class));
                    break;
                case R.id.btm_sheet_caterer_order_history:
                    startActivity(new Intent(CatererHomeActivity.this, OrderHistoryActivity.class));
                    break;
            }
            return true;
        });*/
        //TODO: Implement pending orders
    }

    private void fetchAllVendors() {
        allVendorsArrayList = new ArrayList<>();
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.USER_INFO_BRANCH_NAME;
        allVendorsRef = FirebaseDatabase.getInstance().getReference(databasePath);
        allVendorsAdapter = new VendorListAdapter();
        allVendorsAdapter.setVendorsList(allVendorsArrayList);
        allVendorsAdapter.setActivity(this);
        allVendorsLayoutManager = new LinearLayoutManager(CatererHomeActivity.this, RecyclerView.VERTICAL, false);
        allVendorsRecyclerView.setLayoutManager(allVendorsLayoutManager);
        allVendorsRecyclerView.setAdapter(allVendorsAdapter);
        allVendorsRecyclerView.addItemDecoration(new DividerItemDecoration(CatererHomeActivity.this,
                DividerItemDecoration.VERTICAL));
        allVendorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserDetails userDetails = snapshot.getValue(UserDetails.class);
                    if (userDetails.getIsVendor()) {
                        userDetails.setUserID(snapshot.getKey());
                        allVendorsArrayList.add(userDetails);
                        allVendorsAdapter.notifyDataSetChanged();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchFavouriteVendors() {
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() +
                FirebaseUtils.FAVOURITE_VENDORS_BRANCH_NAME +
                FirebaseAuth.getInstance().getUid();
        favouriteVendorsAdapter = new FavouriteVendorsAdapter();
        favouriteVendorsAdapter.setFavouriteVendorArrayList(favouriteVendorArrayList);
        favouriteVendorsAdapter.setActivity(this);
        favouriteVendorsLayoutManager = new LinearLayoutManager(CatererHomeActivity.this, RecyclerView.HORIZONTAL, false);
        favouriteVendorsRecyclerView.setLayoutManager(favouriteVendorsLayoutManager);
        favouriteVendorsRecyclerView.setAdapter(favouriteVendorsAdapter);
        favouriteVendorsReference = FirebaseDatabase.getInstance().getReference(databasePath);
        favouriteVendorsEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                // A new favouriteVendor has been added, add it to the displayed list
                UserDetails favouriteVendor = dataSnapshot.getValue(UserDetails.class);
                favouriteVendor.setUserID(dataSnapshot.getKey());
                favouriteVendorArrayList.add(favouriteVendor);
                favouriteVendorsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                // A favouriteVendor has changed, use the key to determine if we are displaying this
                // favouriteVendor and if so displayed the changed favouriteVendor.
                UserDetails favouriteVendor = dataSnapshot.getValue(UserDetails.class);
                String favouriteVendorKey = dataSnapshot.getKey();
                for (int i = 0; i < favouriteVendorArrayList.size(); i++) {
                    if (favouriteVendorArrayList.get(i).getUserID().equals(favouriteVendorKey)) {
                        favouriteVendorArrayList.remove(i);
                        favouriteVendor.setUserID(dataSnapshot.getKey());
                        favouriteVendorArrayList.add(i, favouriteVendor);
                        favouriteVendorsAdapter.notifyDataSetChanged();
                        break;
                    }
                }
                // ...
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A favouriteVendor has changed, use the key to determine if we are displaying this
                // favouriteVendor and if so remove it.
                String favouriteVendorKey = dataSnapshot.getKey();
                for (int i = 0; i < favouriteVendorArrayList.size(); i++) {
                    if (favouriteVendorArrayList.get(i).getUserID().equals(favouriteVendorKey)) {
                        favouriteVendorArrayList.remove(i);
                        favouriteVendorsAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A favouriteVendor has changed position, use the key to determine if we are
                // displaying this favouriteVendor and if so move it.
                FavouriteVendor favouriteVendor = dataSnapshot.getValue(FavouriteVendor.class);
                String favouriteVendorKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(CatererHomeActivity.this, "Failed to load favourite vendors.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        favouriteVendorsReference.addChildEventListener(favouriteVendorsEventListener);


    }

    private void initViews() {
        favouriteVendorsRecyclerView = findViewById(R.id.frag_cate_dash_fav_vendors);
        allVendorsRecyclerView = findViewById(R.id.frag_cate_all_vendors);
        viewProfileFAB = findViewById(R.id.caterer_view_profile);
        viewOrderHistoryFAB = findViewById(R.id.caterer_order_history);
        viewCartFAB = findViewById(R.id.act_caterer_cart);
        favouriteVendorArrayList = new ArrayList<>();
        bottomAppBar = findViewById(R.id.bottom_app_bar_caterer);

        viewProfileFAB.setOnClickListener(this);
        viewCartFAB.setOnClickListener(this);
        viewOrderHistoryFAB.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == viewProfileFAB.getId()) {
            startActivity(new Intent(this, EditProfileActivity.class));
        } else if (v.getId() == viewCartFAB.getId()) {
            startActivity(new Intent(CatererHomeActivity.this, CartActivity.class));
        } else if (v.getId() == viewOrderHistoryFAB.getId()) {
            startActivity(new Intent(CatererHomeActivity.this, OrderHistoryActivity.class));
        }

    }


}

