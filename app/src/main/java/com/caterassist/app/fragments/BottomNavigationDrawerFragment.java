package com.caterassist.app.fragments;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.main_nav_faq:
                    startActivity(new Intent(getActivity(), FAQActivity.class));
                    break;
                case R.id.main_nav_contact_us:
                    startActivity(new Intent(getActivity(), ContactUsActivity.class));
                    break;
                case R.id.main_nav_about_us:
                    startActivity(new Intent(getActivity(), AboutUsActivity.class));
                    break;
                case R.id.main_nav_share_app:
                    shareApp();
                    break;
                case R.id.main_nav_settings:
                    startActivity(new Intent(getActivity(), SettingsActivity.class));
                    break;
                case R.id.main_nav_logout:
                    AppUtils.cleanUpAndLogout(getActivity());
                    break;
            }
            return true;
        });

        return parentView;
    }

    private void shareApp() {
        String shareText = "Download CaterAssist from Google Play to assist you with your catering or vending services: " + getString(R.string.app_link);
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        getActivity().startActivity(sharingIntent);
    }
}
