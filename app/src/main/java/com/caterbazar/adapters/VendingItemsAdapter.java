package com.caterbazar.adapters;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caterbazar.R;
import com.caterbazar.activities.AddEditItemActivity;
import com.caterbazar.models.VendorItem;
import com.caterbazar.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class VendingItemsAdapter extends RecyclerView.Adapter<VendingItemsAdapter.VendingItemViewHolder> {
    private ArrayList<VendorItem> vendingItemArrayList;
    private Activity activity;

    public void setVendingItemArrayList(ArrayList<VendorItem> vendingItemArrayList) {
        this.vendingItemArrayList = vendingItemArrayList;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @NonNull
    @Override
    public VendingItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_vending_item, parent, false);
        return new VendingItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VendingItemViewHolder holder, int position) {
        VendorItem vendorItem = vendingItemArrayList.get(position);
        String imageUrl = vendorItem.getImageUrl();
        if (imageUrl != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child(imageUrl).getDownloadUrl().addOnSuccessListener(uri -> {
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.ic_error_placeholder)
                        .override(140, 140);
                Glide.with(activity.getApplicationContext())
                        .setDefaultRequestOptions(requestOptions)
                        .load(uri)
                        .into(holder.itemImage);
            }).addOnFailureListener(exception -> holder.itemImage.setImageResource(R.drawable.ic_error_placeholder));
        }
        holder.itemName.setText(vendorItem.getName());
        holder.itemCategory.setText(vendorItem.getCategory());
        String itemRateString = "₹" + String.valueOf(vendorItem.getRatePerUnit()) + "/" + vendorItem.getUnit();
        holder.itemRate.setText(itemRateString);
        String itemStockString = String.valueOf(vendorItem.getStock()) + " " + vendorItem.getUnit();
        holder.itemStock.setText(String.valueOf(itemStockString));
    }

    @Override
    public int getItemCount() {
        return vendingItemArrayList.size();
    }

    class VendingItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView itemImage;
        TextView itemName;
        TextView itemRate;
        TextView itemCategory;
        TextView itemStock;
        RelativeLayout itemLayout;
        ImageButton removeItemButton;
        ImageButton editItemButton;

        public VendingItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.li_vending_item_image);
            itemName = itemView.findViewById(R.id.li_vending_item_name);
            itemRate = itemView.findViewById(R.id.li_vending_item_rate);
            itemCategory = itemView.findViewById(R.id.li_vending_item_category);
            itemStock = itemView.findViewById(R.id.textVieli_vending_item_stock);
            removeItemButton = itemView.findViewById(R.id.li_vending_item_remove);
            editItemButton = itemView.findViewById(R.id.li_vending_item_edit);
            itemLayout = itemView.findViewById(R.id.vending_list_item);

            itemLayout.setOnClickListener(this);
            removeItemButton.setOnClickListener(this);
            editItemButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case (R.id.vending_list_item):
                    Intent intent = new Intent(itemView.getContext(), AddEditItemActivity.class);
                    intent.putExtra("item", vendingItemArrayList.get(getAdapterPosition()));
                    itemView.getContext().startActivity(intent);
                    break;
                case R.id.li_vending_item_remove:
                    showRemoveItemDialog();
                    break;
            }
        }

        private void showRemoveItemDialog() {
            androidx.appcompat.app.AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new androidx.appcompat.app.AlertDialog.Builder(itemView.getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth);
            } else {
                builder = new AlertDialog.Builder(itemView.getContext());
            }
            builder.setTitle("Remove Item")
                    .setMessage("Are you sure to remove this item from your list?")
                    .setPositiveButton("YES", (dialog, which) -> {
                        String itemId = vendingItemArrayList.get(getAdapterPosition()).getId();
                        removeItemFromStock(itemId);
                        Toasty.success(itemView.getContext(), "Item removed", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("NO", (dialog, which) -> dialog.dismiss())
                    .show();
        }

        private void removeItemFromStock(String itemId) {
            String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.VENDOR_LIST_BRANCH_NAME
                    + FirebaseAuth.getInstance().getUid() + "/" + itemId;
            Toasty.error(itemView.getContext(), databasePath, Toast.LENGTH_SHORT).show();
            DatabaseReference vendingItemReference = FirebaseDatabase.getInstance().getReference(databasePath);
            vendingItemReference.setValue(null);
        }
    }
}
