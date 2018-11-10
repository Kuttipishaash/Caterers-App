package com.caterassist.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.caterassist.app.R;
import com.caterassist.app.fragments.BottomNavigationDrawerFragment;
import com.caterassist.app.fragments.CatererDashboardFragment;
import com.caterassist.app.fragments.VendorDashboardFragments;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.utils.AppUtils;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

public class HomeActivity extends FragmentActivity implements View.OnClickListener {

    private static final String TAG = "HomeActivity";
    BottomAppBar bottomAppBar;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private UserDetails userDetails;
    private String currentUserID;

    private FloatingActionButton searchFAB;
    private boolean isFABVisible;
    private EditText vendorSearchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUserID = checkLogin();
        userDetails = AppUtils.getUserInfoSharedPreferences(this);
        setContentView(R.layout.activity_home);
        initViews();
        setupBottomAppBar();
        if (userDetails.isVendor()) {
            loadVendorViews();
        } else {
            loadCatererViews();
        }
        searchFAB.setOnClickListener(this);
    }
//TODO: GET PHONE PERMISSION

    private void setupBottomAppBar() {
        if (userDetails.isVendor()) {
            bottomAppBar.replaceMenu(R.menu.bottom_bar_overflow_menu_vendor);
        } else {
            bottomAppBar.replaceMenu(R.menu.bottom_bar_overflow_menu_caterer);
        }
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
                    case R.id.btm_sheet_option_cart:
                        startActivity(new Intent(HomeActivity.this, CartActivity.class));
                        break;
                }
                return true;
            }
        });
    }


    private void loadCatererViews() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.act_home_fragment, new CatererDashboardFragment());
        fragmentTransaction.commit();
    }


    private void loadVendorViews() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.act_home_fragment, new VendorDashboardFragments());
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        if (!userDetails.isVendor()) {
            hideSearchBar();
        }
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            AppUtils.cleanUpAndLogout(this);
        }
    }

    private void hideSearchBar() {
        vendorSearchEditText.setVisibility(View.GONE);
        searchFAB.show();
        isFABVisible = true;
    }

    private void showSearchBar() {
        searchFAB.hide();
        vendorSearchEditText.setVisibility(View.VISIBLE);
        isFABVisible = false;
    }

    private String checkLogin() {
        if (firebaseAuth.getCurrentUser() == null) {
            AppUtils.cleanUpAndLogout(this);
            return "";
        } else {
            return firebaseAuth.getUid();
        }
    }

    private void initViews() {
        searchFAB = findViewById(R.id.act_home_fab_search_vendor);
        vendorSearchEditText = findViewById(R.id.act_home_edt_txt_vendor_search);
        bottomAppBar = findViewById(R.id.bottom_app_bar);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.act_home_fab_search_vendor:
                if (userDetails.isVendor()) {
                    //TODO: Vendor fab
                } else {
                    showSearchBar();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (!userDetails.isVendor() && !isFABVisible) {
            hideSearchBar();
        } else {
            super.onBackPressed();
        }
    }
}
