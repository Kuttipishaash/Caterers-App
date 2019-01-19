package com.caterassist.app.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.models.CartItem;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.models.VendorItem;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import es.dmoral.toasty.Toasty;

public class AddToCartDialog extends DialogFragment implements View.OnClickListener {
    private TextView itemNameTextView, itemUnitsTxtView, itemQtuAvailableTxtView, itemRateTxtView;
    private FloatingActionButton cancelButton, confirmButton;
    private EditText itemQuantitiyEdtTxt;
    private VendorItem vendorItem;
    private UserDetails vendorDetails;

    private View rootView;

    public AddToCartDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_add_to_cart, container, false);
        initViews();
        setViewContent();
        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    public void setValues(VendorItem vendorItem, UserDetails vendorDetails) {
        this.vendorItem = vendorItem;
        this.vendorDetails = vendorDetails;
    }


    public void setViewContent() {
        itemNameTextView.setText(vendorItem.getName());
        itemUnitsTxtView.setText(vendorItem.getUnit());
        itemRateTxtView.setText(String.valueOf(vendorItem.getRatePerUnit()));
        String unitsAvailable = String.valueOf(vendorItem.getStock()) + vendorItem.getUnit();
        itemQtuAvailableTxtView.setText(unitsAvailable);
    }

    private void initViews() {
        itemNameTextView = rootView.findViewById(R.id.diag_add_to_cart_item_name_txt_view);
        itemUnitsTxtView = rootView.findViewById(R.id.diag_add_to_cart_units_txt_view);
        cancelButton = rootView.findViewById(R.id.diag_add_to_cart_cancel_btn);
        confirmButton = rootView.findViewById(R.id.diag_add_to_cart_confirm_button);
        itemQuantitiyEdtTxt = rootView.findViewById(R.id.diag_add_to_cart_quantity_input);
        itemQtuAvailableTxtView = rootView.findViewById(R.id.diag_add_to_cart_item_stock);
        itemRateTxtView = rootView.findViewById(R.id.diag_add_to_cart_item_rate);

        cancelButton.setOnClickListener(this);
        confirmButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.diag_add_to_cart_confirm_button:
                Double inputQuantity = Double.parseDouble(itemQuantitiyEdtTxt.getText().toString());
                if (inputQuantity > vendorItem.getStock()) {
                    Toasty.warning(getContext(), "The quantity entered is not currently in stock.", Toast.LENGTH_SHORT).show();
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
        if (inputQuantity > 0.0) {
            double totalAmount = vendorItem.getRatePerUnit() * inputQuantity;
            CartItem cartItem = new CartItem(vendorItem.getId(),
                    vendorItem.getName(),
                    vendorItem.getRatePerUnit(),
                    inputQuantity,
                    vendorItem.getUnit(),
                    vendorItem.getImageUrl(),
                    totalAmount);
            String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.CART_BRANCH_NAME + FirebaseAuth.getInstance().getUid() + "/";
            String cartItemsPath = databasePath + FirebaseUtils.CART_ITEMS_BRANCH;
            DatabaseReference itemsReference = FirebaseDatabase.getInstance().getReference(cartItemsPath);
            itemsReference.child(cartItem.getId()).setValue(cartItem);
            Objects.requireNonNull(itemsReference.getParent()).child(FirebaseUtils.CART_VENDOR_BRANCH).setValue(vendorDetails);
            dismiss();
        } else {
            Toasty.warning(getContext(), getContext().getString(R.string.toast_added_item_qty_less_than_zero), Toast.LENGTH_SHORT).show();
        }
    }
}
