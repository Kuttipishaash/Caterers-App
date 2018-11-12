package com.caterassist.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.caterassist.app.R;
import com.caterassist.app.models.VendorItem;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class VendingItemsAdapter extends RecyclerView.Adapter<VendingItemsAdapter.VendingItemViewHolder> {
    private ArrayList<VendorItem> vendingItemArrayList;

    public void setVendingItemArrayList(ArrayList<VendorItem> vendingItemArrayList) {
        this.vendingItemArrayList = vendingItemArrayList;
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
        //TODO:Set image
        holder.itemName.setText(vendorItem.getName());
        holder.itemCategory.setText(vendorItem.getCategory());
        holder.itemRate.setText(String.valueOf(vendorItem.getRatePerUnit()));
        holder.itemStock.setText(String.valueOf(vendorItem.getStock()));
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
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.li_vending_item_edit:
                    //TODO:Edit item details
                    break;
                case R.id.li_vending_item_remove:
                    //TODO: Remove item
                    break;
            }
        }
    }
}
