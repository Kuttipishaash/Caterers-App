package com.caterassist.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.caterassist.app.R;
import com.caterassist.app.models.VendorItem;

public class AddEditItemActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "AddEditItemAct";
    Spinner itemCategorySpinner;
    TextView itemCategoryTxtView;
    Spinner itemNameSpinner;
    TextView itemNameTxtView;
    EditText itemRateEdtTxt;
    EditText itemStockEdtTxt;
    Button saveButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);
        initViews();
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("item")) {
                VendorItem vendorItem = (VendorItem) intent.getSerializableExtra("item");
                itemCategorySpinner.setVisibility(View.GONE);
                itemNameSpinner.setVisibility(View.GONE);
                itemCategoryTxtView.setVisibility(View.VISIBLE);
                itemNameTxtView.setVisibility(View.VISIBLE);
                itemCategoryTxtView.setText(vendorItem.getCategory());
                itemNameTxtView.setText(vendorItem.getName());
                itemRateEdtTxt.setText(String.valueOf(vendorItem.getRatePerUnit()));
                itemStockEdtTxt.setText(String.valueOf(vendorItem.getStock()));
                itemRateEdtTxt.setEnabled(true);
                itemStockEdtTxt.setEnabled(true);
                saveButton.setEnabled(true);
            }
        }
    }

    private void initViews() {
        itemCategorySpinner = findViewById(R.id.act_addedt_category);
        itemCategoryTxtView = findViewById(R.id.act_addedt_category_txt);
        itemNameSpinner = findViewById(R.id.act_addedt_item_name);
        itemNameTxtView = findViewById(R.id.act_addedt_item_name_txt);
        itemRateEdtTxt = findViewById(R.id.act_addedt_rate);
        itemStockEdtTxt = findViewById(R.id.act_addedt_units_available);
        saveButton = findViewById(R.id.act_addedt_submit_btn);

        saveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == saveButton.getId()) {
            //Save changes
        }
    }
}
