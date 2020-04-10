package com.caterbazar.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caterbazar.R;
import com.caterbazar.activities.CartActivity;
import com.caterbazar.models.CartItem;
import com.caterbazar.models.UserDetails;
import com.caterbazar.models.VendorItem;
import com.caterbazar.utils.FirebaseUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import de.mateware.snacky.Snacky;
import es.dmoral.toasty.Toasty;

public class AddToCartDialog extends DialogFragment implements View.OnClickListener {
    private TextView itemNameTextView, itemUnitsTxtView, itemQtuAvailableTxtView, itemRateTxtView;
    private FloatingActionButton cancelButton, confirmButton;
    private EditText itemQuantitiyEdtTxt;
    private VendorItem vendorItem;
    private UserDetails vendorDetails;
    private ImageView itemImage;

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


    private void setViewContent() {
        itemNameTextView.setText(vendorItem.getName());
        itemUnitsTxtView.setText(vendorItem.getUnit());
        String imageUrl = vendorItem.getImageUrl();
        if (imageUrl != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child(imageUrl).getDownloadUrl().addOnSuccessListener(uri -> {
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.ic_error_placeholder)
                        .override(70, 70);
                Glide.with(itemImage.getContext())
                        .setDefaultRequestOptions(requestOptions)
                        .load(uri).thumbnail(0.1f)
                        .into(itemImage);
            }).addOnFailureListener(exception -> itemImage.setImageResource(R.drawable.ic_error_placeholder));
        }
        String rate = "₹" + String.valueOf(vendorItem.getRatePerUnit()) + vendorItem.getUnit();
        itemRateTxtView.setText(rate);
        String unitsAvailable = String.valueOf(vendorItem.getStock()) + vendorItem.getUnit();
        itemQtuAvailableTxtView.setText(unitsAvailable);
    }

    private void initViews() {
        itemNameTextView = rootView.findViewById(R.id.diag_add_to_cart_item_name_txt_view);
        itemUnitsTxtView = rootView.findViewById(R.id.diag_add_to_cart_units_txt_view);
        itemImage = rootView.findViewById(R.id.add_to_cart_placeholder);
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
                if (inputQuantity > vendorItem.getStock() && getContext() != null) {
                    Toasty.warning(getContext(), "The quantity entered is not currently in stock.", Toast.LENGTH_SHORT).show();
                } else {
                    addToCart(inputQuantity);
                    dismiss();
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
            itemsReference.child(cartItem.getId()).setValue(cartItem)
                    .addOnSuccessListener(aVoid -> Objects.requireNonNull(itemsReference.getParent()).child(FirebaseUtils.CART_VENDOR_BRANCH)
                            .setValue(vendorDetails)
                            .addOnSuccessListener(aVoid1 -> showCartSnack())).addOnFailureListener(e -> {
                if (getContext() != null) {
                    Toasty.error(getContext(), "Adding to cart failed").show();

                }
            });

            dismiss();
        } else {
            if (getContext() != null) {
                Toasty.warning(getContext(), getContext().getString(R.string.toast_added_item_qty_less_than_zero), Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void showCartSnack() {
        if (getActivity() != null) {
            Snacky.builder()
                    .setActivity(getActivity())
                    .setBackgroundColor(getResources().getColor(R.color.colorPrimary))
                    .setActionText("View Cart")
                    .setActionClickListener(v -> {
                        startActivity(new Intent(getActivity(), CartActivity.class));
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    })
                    .setText("Item added to cart")
                    .setIcon(R.drawable.ic_cart)
                    .setActionTextTypefaceStyle(Typeface.BOLD)
                    .setActionTextColor(getResources().getColor(R.color.white))
                    .setDuration(Snacky.LENGTH_INDEFINITE)
                    .build()
                    .show();
        }
    }
}
