package com.caterbazar.activities;

import android.app.Activity;
import android.os.Bundle;

import com.caterbazar.R;

public class AboutUsActivity extends Activity {
    private static final String TAG = "AboutUsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
    }
}
