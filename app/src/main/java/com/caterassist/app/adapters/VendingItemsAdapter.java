package com.caterassist.app.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.caterassist.app.R;
import com.caterassist.app.activities.AddEditItemActivity;
import com.caterassist.app.models.VendorItem;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .apply(requestOptions)
                        .into(holder.itemImage);
            }).addOnFailureListener(exception -> holder.itemImage.setImageResource(R.drawable.ic_error_placeholder));
        }
        holder.itemName.setText(vendorItem.getName());
        holder.itemCategory.setText(vendorItem.getCategory());
        String itemRateString = " ₹" + String.valueOf(vendorItem.getRatePerUnit()) + "/" + vendorItem.getUnit();
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
            new AlertDialog.Builder(itemView.getContext())
                    .setTitle("Remove Item")
                    .setMessage("Are you sure to remove this item from your list?")
                    .setPositiveButton("YES", (dialog, which) -> {
                        String itemId = vendingItemArrayList.get(getAdapterPosition()).getId();
                        removeItemFromStock(itemId);
                        Toast.makeText(itemView.getContext(), "Item removed", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("NO", (dialog, which) -> dialog.dismiss())
                    .show();
        }

        private void removeItemFromStock(String itemId) {
            String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.VENDOR_LIST_BRANCH_NAME
                    + FirebaseAuth.getInstance().getUid() + "/" + itemId;
            Toast.makeText(itemView.getContext(), databasePath, Toast.LENGTH_SHORT).show();
            DatabaseReference vendingItemReference = FirebaseDatabase.getInstance().getReference(databasePath);
            vendingItemReference.setValue(null);
        }
    }
}
