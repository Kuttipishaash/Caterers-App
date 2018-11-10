package com.caterassist.app.activities;

import android.app.Activity;
import android.os.Bundle;

import com.caterassist.app.R;
import com.caterassist.app.utils.Constants;

public class ViewVendorItemsActivity extends Activity {

    private String vendorUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_vendor_items);
        vendorUID = savedInstanceState.getString(Constants.IntentExtrasKeys.VIEW_VENDOR_ITEMS_INTENT_VENDOR_UID);
        initViews();
        fetchVendorItems();
    }

    private void fetchVendorItems() {

    }

    private void initViews() {
    }
}
