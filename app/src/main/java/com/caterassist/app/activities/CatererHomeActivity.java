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
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caterassist.app.R;
import com.caterassist.app.adapters.FavouriteVendorsAdapter;
import com.caterassist.app.adapters.VendorListAdapter;
import com.caterassist.app.fragments.BottomNavigationDrawerFragment;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.utils.AppUtils;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
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
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class CatererHomeActivity extends FragmentActivity implements View.OnClickListener {
    private static final String TAG = "CatererDash";
    private static final int CALL_PERMISSION_REQ_CODE = 100;
    private ArrayList<UserDetails> allVendorsArrayList;
    private Toolbar toolbar;
    private DatabaseReference favouriteVendorsReference;
    private ValueEventListener favouriteVendorsEventListener;
    private ValueEventListener vendorsEventListener;
    private RecyclerView favouriteVendorsRecyclerView;
    private LinearLayoutManager favouriteVendorsLayoutManager;
    private FavouriteVendorsAdapter favouriteVendorsAdapter;
    private ArrayList<UserDetails> favouriteVendorArrayList;
    private DatabaseReference allVendorsRef;
    private LinearLayoutManager allVendorsLayoutManager;
    private VendorListAdapter allVendorsAdapter;
    private RecyclerView allVendorsRecyclerView;
    private FloatingActionButton viewOrderHistoryFAB, viewCartFAB;
    private BottomAppBar bottomAppBar;
    private SearchView searchView;
    private ImageView viewProfileFab;
    private TextView profileName;
    private TextView profileLocation;
    private TextView noFavouritesTxtView;
    private TextView noVendorsTxtView;


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

        UserDetails userDetails = AppUtils.getUserInfoSharedPreferences(this);

        String title = "Hi, " + userDetails.getUserName();
        profileName.setText(title);
        toolbar.setTitle(title);
        String subtitle = userDetails.getUserLocationName() + ", " + userDetails.getUserDistrictName();
        profileLocation.setText(subtitle);

        String imageUrl = userDetails.getUserImageUrl();
        if (imageUrl != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child(imageUrl).getDownloadUrl().addOnSuccessListener(uri -> {
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.ic_error_placeholder)
                        .override(100, 100);
                Glide.with(CatererHomeActivity.this)
                        .setDefaultRequestOptions(requestOptions)
                        .load(uri)
                        .into(viewProfileFab);
            }).addOnFailureListener(exception -> viewProfileFab.setImageResource(R.drawable.ic_error_placeholder));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (favouriteVendorsEventListener != null) {
            favouriteVendorsReference.removeEventListener(favouriteVendorsEventListener);
        }
        if (vendorsEventListener != null) {
            allVendorsRef.removeEventListener(vendorsEventListener);
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
                ScrollView sv = findViewById(R.id.caterer_dash_nested_scroll_view);
                sv.smoothScrollTo(0, sv.getBottom());
                allVendorsAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                NestedScrollView sv = findViewById(R.id.caterer_dash_nested_scroll_view);
                sv.smoothScrollTo(0, sv.getBottom());
                allVendorsAdapter.getFilter().filter(newText);
                return false;
            }
        });

        bottomAppBar.setNavigationOnClickListener(v -> {
            BottomSheetDialogFragment bottomSheetDialogFragment = new BottomNavigationDrawerFragment();
            bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
        });
        bottomAppBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.btm_sheet_option_cart:
                    startActivity(new Intent(CatererHomeActivity.this, CartActivity.class));
                    break;
            }
            return true;
        });
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
        vendorsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allVendorsArrayList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserDetails userDetails = snapshot.getValue(UserDetails.class);
                    if (userDetails.getIsVendor()) {
                        userDetails.setUserID(snapshot.getKey());
                        allVendorsArrayList.add(userDetails);
                    }
                }
                allVendorsAdapter.notifyDataSetChanged();
                checkVendorsEmpty();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: failed to fetch favourite vendors", databaseError.toException());
                Toast.makeText(CatererHomeActivity.this, "Failed to load favourite vendors.",
                        Toast.LENGTH_SHORT).show();
                checkVendorsEmpty();
            }
        };
        allVendorsRef.addValueEventListener(vendorsEventListener);
    }

    private void checkVendorsEmpty() {
        //TODO: implement actions to be taken when the vendors list is empty
        if (allVendorsArrayList.size() > 0) {
            allVendorsRecyclerView.setVisibility(VISIBLE);
            noVendorsTxtView.setVisibility(GONE);
        } else {
            noVendorsTxtView.setVisibility(VISIBLE);
            allVendorsRecyclerView.setVisibility(GONE);
        }
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
        favouriteVendorsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                favouriteVendorArrayList.clear();
                for (DataSnapshot snapshot :
                        dataSnapshot.getChildren()) {
                    UserDetails favouriteVendor = snapshot.getValue(UserDetails.class);
                    if (favouriteVendor != null) {
                        favouriteVendor.setUserID(snapshot.getKey());
                        favouriteVendorArrayList.add(favouriteVendor);
                    }
                }
//                Collections.reverse(favouriteVendorArrayList);
                favouriteVendorsAdapter.notifyDataSetChanged();
                checkFavouritesEmpty();
                //TODO :dismiss loading
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: failed to fetch favourite vendors", databaseError.toException());
                Toast.makeText(CatererHomeActivity.this, "Failed to load favourite vendors.",
                        Toast.LENGTH_SHORT).show();
                checkFavouritesEmpty();
                //TODO: dismiss loading
            }
        };
        favouriteVendorsReference.addValueEventListener(favouriteVendorsEventListener);
    }

    private void checkFavouritesEmpty() {
        if (favouriteVendorArrayList.size() > 0) {
            favouriteVendorsRecyclerView.setVisibility(VISIBLE);
            noFavouritesTxtView.setVisibility(GONE);
        } else {
            noFavouritesTxtView.setVisibility(VISIBLE);
            favouriteVendorsRecyclerView.setVisibility(GONE);
        }
    }

    private void initViews() {
        favouriteVendorsRecyclerView = findViewById(R.id.frag_cate_dash_fav_vendors);
        toolbar = findViewById(R.id.cater_dash_toolbar);
        allVendorsRecyclerView = findViewById(R.id.frag_cate_all_vendors);
        viewOrderHistoryFAB = findViewById(R.id.caterer_order_history);
        viewCartFAB = findViewById(R.id.act_caterer_cart);
        favouriteVendorArrayList = new ArrayList<>();
        bottomAppBar = findViewById(R.id.bottom_app_bar_caterer);

        viewProfileFab = findViewById(R.id.cater_view_profile);
        profileName = findViewById(R.id.cater_home_name);
        profileLocation = findViewById(R.id.cater_home_location);
        noFavouritesTxtView = findViewById(R.id.frag_cate_dash_fav_vendors_nothing);
        noFavouritesTxtView.setVisibility(View.GONE);
        noVendorsTxtView = findViewById(R.id.frag_cate_dash_vendors_nothing);
        noVendorsTxtView.setVisibility(View.GONE);

        viewProfileFab.setOnClickListener(this);
        viewCartFAB.setOnClickListener(this);
        viewOrderHistoryFAB.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == viewProfileFab.getId()) {
            startActivity(new Intent(this, EditProfileActivity.class));
        } else if (v.getId() == viewCartFAB.getId()) {
            startActivity(new Intent(CatererHomeActivity.this, CartActivity.class));
        } else if (v.getId() == viewOrderHistoryFAB.getId()) {
            startActivity(new Intent(CatererHomeActivity.this, OrderHistoryActivity.class));
        }

    }


}

