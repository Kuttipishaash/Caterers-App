package com.caterassist.app.fragments;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.activities.AboutUsActivity;
import com.caterassist.app.activities.ContactUsActivity;
import com.caterassist.app.activities.FAQActivity;
import com.caterassist.app.activities.SettingsActivity;
import com.caterassist.app.utils.AppUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;

/**
 * A simple {@link Fragment} subclass.
 */
public class BottomNavigationDrawerFragment extends BottomSheetDialogFragment {

    NavigationView navigationView;
    private View parentView;

    public BottomNavigationDrawerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        parentView = inflater.inflate(R.layout.fragment_navigation_bottm_sheet, container, false);
        navigationView = parentView.findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.main_nav_faq:
                        Toast.makeText(getContext(), "FAQ clicked", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), FAQActivity.class));
                        break;
                    case R.id.main_nav_contact_us:
                        Toast.makeText(getContext(), "Contact us clicked", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), ContactUsActivity.class));
                        break;
                    case R.id.main_nav_about_us:
                        Toast.makeText(getContext(), "About us clicked", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), AboutUsActivity.class));
                        break;
                    case R.id.main_nav_share_app:
                        Toast.makeText(getContext(), "Share app clicked", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.main_nav_settings:
                        Toast.makeText(getContext(), "Settings clicked", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), SettingsActivity.class));
                        break;
                    case R.id.main_nav_logout:
                        Toast.makeText(getContext(), "Logout clicked", Toast.LENGTH_SHORT).show();
                        AppUtils.cleanUpAndLogout(getActivity());
                        break;
                }
                return true;
            }
        });

        return parentView;
    }


}
