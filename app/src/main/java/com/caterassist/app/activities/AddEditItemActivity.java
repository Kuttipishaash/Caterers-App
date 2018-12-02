package com.caterassist.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.caterassist.app.R;
import com.caterassist.app.models.GenericItem;
import com.caterassist.app.models.VendorItem;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;

public class AddEditItemActivity extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "AddEditItemAct";
    ArrayList<String> categoriesArrayList;
    ArrayList<String> itemNamesArrayList;
    ArrayList<GenericItem> itemsArrayList;
    Spinner itemCategorySpinner;
    TextView itemCategoryTxtView;
    Spinner itemNameSpinner;
    TextView itemNameTxtView;
    EditText itemRateEdtTxt;
    EditText itemStockEdtTxt;
    TextView unitTextView;
    Button saveButton;
    VendorItem vendorItem;

    boolean isEdit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);
        categoriesArrayList = new ArrayList<>();
        itemNamesArrayList = new ArrayList<>();
        itemsArrayList = new ArrayList<>();

        initViews();
        fetchSpinnerData();
        setViews();

    }

    private void fetchSpinnerData() {
        String categoryDatabasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.CATEGORIES_BRANCH;
        DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference(categoryDatabasePath);
        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String category = snapshot.getValue().toString();
                    categoriesArrayList.add(category);
                }
                ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, categoriesArrayList);
                itemCategorySpinner.setAdapter(categoriesAdapter);
                itemCategorySpinner.setOnItemSelectedListener(AddEditItemActivity.this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setViews() {
        isEdit = false;
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("item")) {
                isEdit = true;
                vendorItem = (VendorItem) intent.getSerializableExtra("item");
                itemCategorySpinner.setVisibility(View.GONE);
                itemNameSpinner.setVisibility(View.GONE);
                itemCategoryTxtView.setVisibility(View.VISIBLE);
                itemNameTxtView.setVisibility(View.VISIBLE);
                itemCategoryTxtView.setText(vendorItem.getCategory());
                itemNameTxtView.setText(vendorItem.getName());
                unitTextView.setText(vendorItem.getUnit());
                itemRateEdtTxt.setText(String.valueOf(vendorItem.getRatePerUnit()));
                itemStockEdtTxt.setText(String.valueOf(vendorItem.getStock()));
                itemRateEdtTxt.setEnabled(true);
                itemStockEdtTxt.setEnabled(true);
                saveButton.setEnabled(true);
            } else {
                vendorItem = new VendorItem();
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
        unitTextView = findViewById(R.id.act_addedt_item_unit);
        saveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == saveButton.getId()) {
            String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.VENDOR_LIST_BRANCH_NAME + FirebaseAuth.getInstance().getUid();
            DatabaseReference itemManagereference = FirebaseDatabase.getInstance().getReference(databasePath);

            if (isEdit) {
                setValues();
                itemManagereference.child(vendorItem.getId()).setValue(vendorItem);
            } else {
                setValues();
                itemManagereference.push().setValue(vendorItem);
            }
        }
    }

    private void setValues() {
        vendorItem.setRatePerUnit(Double.parseDouble(itemRateEdtTxt.getText().toString()));
        vendorItem.setStock(Double.parseDouble(itemStockEdtTxt.getText().toString()));
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.act_addedt_category) {
            String selectedCategory = categoriesArrayList.get(position);
            vendorItem.setCategory(selectedCategory);
            String itemsDatabasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.ITEMS_BRANCH + selectedCategory;
            DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference(itemsDatabasePath);
            itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    itemNamesArrayList.clear();
                    itemsArrayList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        GenericItem item = snapshot.getValue(GenericItem.class);
                        itemNamesArrayList.add(item.getItemName());
                        itemsArrayList.add(item);
                    }
                    ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, itemNamesArrayList);
                    itemNameSpinner.setAdapter(itemsAdapter);
                    itemNameSpinner.setOnItemSelectedListener(AddEditItemActivity.this);
                    itemRateEdtTxt.setEnabled(true);
                    itemStockEdtTxt.setEnabled(true);
                    saveButton.setEnabled(true);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else if (parent.getId() == R.id.act_addedt_item_name) {
            vendorItem.setName(itemNamesArrayList.get(position));
            vendorItem.setUnit(itemsArrayList.get(position).getUnit());
            unitTextView.setText(itemsArrayList.get(position).getUnit());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
