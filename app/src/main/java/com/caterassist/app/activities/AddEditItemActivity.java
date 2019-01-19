package com.caterassist.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caterassist.app.R;
import com.caterassist.app.models.Item;
import com.caterassist.app.models.VendorItem;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import es.dmoral.toasty.Toasty;

public class AddEditItemActivity extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "AddEditItemAct";
    ArrayList<String> categoriesArrayList;
    ArrayList<String> itemNamesArrayList;
    ArrayList<Item> itemsArrayList;
    Spinner itemCategorySpinner;
    TextView itemCategoryTxtView;
    Spinner itemNameSpinner;
    TextView itemNameTxtView;
    TextView titleTextView;
    EditText itemRateEdtTxt;
    EditText itemStockEdtTxt;
    ImageView itemImageView;
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
                titleTextView.setText("Edit Listed Item");
                itemCategorySpinner.setVisibility(View.GONE);
                itemNameSpinner.setVisibility(View.GONE);
                itemCategoryTxtView.setTextSize(15);
                itemNameTxtView.setTextSize(18);
                itemNameTxtView.setTypeface(null, Typeface.BOLD);
                itemCategoryTxtView.setText(vendorItem.getCategory());
                itemNameTxtView.setText(vendorItem.getName());
                unitTextView.setText(vendorItem.getUnit());
                itemRateEdtTxt.setText(String.valueOf(vendorItem.getRatePerUnit()));
                itemStockEdtTxt.setText(String.valueOf(vendorItem.getStock()));
                if (vendorItem.getImageUrl() != null) {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                    storageReference.child(vendorItem.getImageUrl()).getDownloadUrl().addOnSuccessListener(uri -> {
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions.placeholder(R.drawable.placeholder);
                        requestOptions.error(R.drawable.ic_error_placeholder);
                        Glide.with(AddEditItemActivity.this)
                                .setDefaultRequestOptions(requestOptions)
                                .load(uri)
                                .into(itemImageView);
                    }).addOnFailureListener(exception -> itemImageView.setImageResource(R.drawable.ic_error_placeholder));
                }

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
        itemImageView = findViewById(R.id.act_addedt_item_img);
        titleTextView = findViewById(R.id.add_edit_title);
        saveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == saveButton.getId()) {
            String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.VENDOR_LIST_BRANCH_NAME + FirebaseAuth.getInstance().getUid();
            DatabaseReference itemManagereference = FirebaseDatabase.getInstance().getReference(databasePath);

            if (isEdit) {
                setValues();
                itemManagereference.child(vendorItem.getId()).setValue(vendorItem)
                        .addOnSuccessListener(aVoid -> {
                            Toasty.success(AddEditItemActivity.this, "Item edited successfully").show();
                            finish();
                        })
                        .addOnFailureListener(e -> Toasty.error(AddEditItemActivity.this, "Item edit failed").show());
            } else {
                setValues();
                String currentVendingListPath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.VENDOR_LIST_BRANCH_NAME
                        + FirebaseAuth.getInstance().getUid();
                DatabaseReference vendingListReference = FirebaseDatabase.getInstance().getReference(currentVendingListPath);
                vendingListReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<VendorItem> currentVendingList = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            currentVendingList.add(snapshot.getValue(VendorItem.class));
                        }
                        boolean flag = true;
                        for (VendorItem item : currentVendingList) {
                            if (item.getName().equalsIgnoreCase(vendorItem.getName())) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            itemManagereference.push().setValue(vendorItem)
                                    .addOnSuccessListener(aVoid -> {
                                        Toasty.success(AddEditItemActivity.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toasty.error(AddEditItemActivity.this, "Item could not be added. Please try again!", Toast.LENGTH_SHORT).show());
                        } else {
                            Toasty.warning(AddEditItemActivity.this, "Item already in the list please select the edit option.").show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toasty.error(AddEditItemActivity.this, "Adding item failed").show();
                    }
                });
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
                        Item item = snapshot.getValue(Item.class);
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
            String imageUrl = itemsArrayList.get(position).getItemImageURL();
            if (imageUrl != null) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                storageReference.child(imageUrl).getDownloadUrl().addOnSuccessListener(uri -> {
                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.placeholder(R.drawable.placeholder);
                    requestOptions.error(R.drawable.ic_error_placeholder);
                    Glide.with(AddEditItemActivity.this)
                            .setDefaultRequestOptions(requestOptions)
                            .load(uri)
                            .into(itemImageView);
                }).addOnFailureListener(exception -> itemImageView.setImageResource(R.drawable.ic_error_placeholder));
            }
            vendorItem.setImageUrl(imageUrl);
            unitTextView.setText(itemsArrayList.get(position).getUnit());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
