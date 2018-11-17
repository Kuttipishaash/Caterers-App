package com.caterassist.app.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.models.CartItem;
import com.caterassist.app.models.VendorItem;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import androidx.annotation.NonNull;
import es.dmoral.toasty.Toasty;

public class AddToCartDialog extends Dialog implements View.OnClickListener {
    private Context context;
    private TextView itemNameTextView, itemUnitsTxtView, itemQtuAvailableTxtView;
    private Button cancelButton, confirmButton;
    private EditText itemQuantitiyEdtTxt;
    private VendorItem vendorItem;
    private String vendorUID;

    public AddToCartDialog(@NonNull Context context, VendorItem vendorItem, String vendorUID) {
        super(context);
        this.context = context;
        this.vendorItem = vendorItem;
        this.vendorUID = vendorUID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_to_cart);
        initViews();
        setViewContent();
    }

    private void setViewContent() {
        itemNameTextView.setText(vendorItem.getName());
        itemUnitsTxtView.setText(vendorItem.getUnit());
        String unitsAvailable = String.valueOf(vendorItem.getStock()) + vendorItem.getUnit();
        itemQtuAvailableTxtView.setText(unitsAvailable);
        //TODO: Show rate.
    }

    private void initViews() {
        itemNameTextView = findViewById(R.id.diag_add_to_cart_item_name_txt_view);
        itemUnitsTxtView = findViewById(R.id.diag_add_to_cart_units_txt_view);
        cancelButton = findViewById(R.id.diag_add_to_cart_cancel_btn);
        confirmButton = findViewById(R.id.diag_add_to_cart_confirm_button);
        itemQuantitiyEdtTxt = findViewById(R.id.diag_add_to_cart_quantity_input);
        itemQtuAvailableTxtView = findViewById(R.id.diag_add_to_cart_item_stock);

        cancelButton.setOnClickListener(this);
        confirmButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.diag_add_to_cart_confirm_button:
                Double inputQuantity = Double.parseDouble(itemQuantitiyEdtTxt.getText().toString());
                if (inputQuantity > vendorItem.getStock()) {
                    Toasty.warning(context, "The quantity entered is not currently in stock.", Toast.LENGTH_SHORT).show();
                } else {
                    addToCart(inputQuantity);
                }
                break;
            case R.id.diag_add_to_cart_cancel_btn:
                dismiss();
                break;
        }
    }

    private void addToCart(Double inputQuantity) {
        //TODO: Add to firebase.
        double totalAmount = vendorItem.getRatePerUnit() * inputQuantity;
        CartItem cartItem = new CartItem(vendorItem.getId(),
                vendorItem.getName(),
                vendorItem.getRatePerUnit(),
                inputQuantity,
                vendorItem.getUnit(),
                "",
                totalAmount);
        //TODO: Set image url from vendorItem.
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.CART_BRANCH_NAME + FirebaseAuth.getInstance().getUid() + "/";
        String cartItemsPath = databasePath + FirebaseUtils.CART_ITEMS_BRANCH;
        DatabaseReference itemsReference = FirebaseDatabase.getInstance().getReference(cartItemsPath);
        itemsReference.child(cartItem.getId()).setValue(cartItem);
        Objects.requireNonNull(itemsReference.getParent()).child(FirebaseUtils.CART_VENDOR_BRANCH).setValue(vendorUID);
        dismiss();
    }
}
