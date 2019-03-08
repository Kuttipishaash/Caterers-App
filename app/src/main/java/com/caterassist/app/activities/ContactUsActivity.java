package com.caterassist.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.caterassist.app.R;

public class ContactUsActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "ContactUsActivity";
    private TextView callTxtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        initViews();
    }

    private void initViews() {
        callTxtView = findViewById(R.id.act_contact_call);

        callTxtView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == callTxtView.getId()) {
            callCaterBazar();
        }
    }

    private void callCaterBazar() {
        String phoneNumber = callTxtView.getText().toString();
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
        startActivity(callIntent);
    }
}
