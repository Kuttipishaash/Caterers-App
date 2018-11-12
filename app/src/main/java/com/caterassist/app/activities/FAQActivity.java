package com.caterassist.app.activities;

import android.app.Activity;
import android.os.Bundle;

import com.caterassist.app.R;

public class FAQActivity extends Activity {
    private static final String TAG = "FAQActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);
    }
}
