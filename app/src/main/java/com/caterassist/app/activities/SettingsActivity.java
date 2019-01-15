package com.caterassist.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.caterassist.app.R;

public class SettingsActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "SettignsActivity";
    private LinearLayout profileSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initViews();

    }

    private void initViews() {
        profileSettings = findViewById(R.id.act_settings_my_profile);

        profileSettings.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.act_settings_my_profile:
                startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
        }
    }
}
