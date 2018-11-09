package com.caterassist.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.fragments.BottomNavigationDrawerFragment;
import com.caterassist.app.fragments.CatererDashboardFragment;
import com.caterassist.app.fragments.VendorDashboardFragments;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import es.dmoral.toasty.Toasty;

public class HomeActivity extends FragmentActivity implements View.OnClickListener {

    private static final String TAG = "HomeActivity";
    ValueEventListener userDetailsListener;
    BottomAppBar bottomAppBar;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private DatabaseReference userInfoReference;
    private UserDetails userDetails;
    private FloatingActionButton cartFAB;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUserID = checkLogin();
        getUserInfo();
        setContentView(R.layout.activity_home);
        initViews();
        setupBottomAppBar();
        cartFAB.setOnClickListener(this);
    }

    private void setupBottomAppBar() {
        bottomAppBar.replaceMenu(R.menu.bottom_bar_overflow_menu);
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


    private void loadVendorFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.act_home_fragment, new CatererDashboardFragment());
        fragmentTransaction.commit();
    }

    private void getUserInfo() {
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() +
                FirebaseUtils.userInfoBranchName +
                currentUserID;
        userInfoReference = FirebaseDatabase.getInstance().getReference(databasePath);
        userDetailsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userDetails = dataSnapshot.getValue(UserDetails.class);
                if (userDetails != null) {
                    Log.d(TAG, "onDataChange: Fetch successful");
                    AppUtils.setUserInfoSharedPreferences(userDetails, HomeActivity.this);
                    if (userDetails.isVendor())
                        loadVendorFragment();
                    else
                        loadVendorFragment();
                    //TODO: Uncomment following line and remove the above line
//                        loadCatererFragment();
                } else {
                    Log.e(TAG, "onDataChange: Failed to fetch");
                    //TODO: Do something if user info couldnt be fetched.
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                userDetails = null;
            }
        };
        userInfoReference.addListenerForSingleValueEvent(userDetailsListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        userInfoReference.removeEventListener(userDetailsListener);
    }

    private void loadCatererFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.act_home_fragment, new VendorDashboardFragments());
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        userInfoReference.addListenerForSingleValueEvent(userDetailsListener);
        assert FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    private String checkLogin() {
        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
            return "";
        } else {
            return firebaseAuth.getUid();
        }
    }

    private void initViews() {
        cartFAB = findViewById(R.id.act_home_fab_cart);
        bottomAppBar = findViewById(R.id.bottom_app_bar);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.act_home_fab_cart:
                //TODO: Change the functonality
                logout();
                break;
        }
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Toasty.success(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
        checkLogin();
    }

}
