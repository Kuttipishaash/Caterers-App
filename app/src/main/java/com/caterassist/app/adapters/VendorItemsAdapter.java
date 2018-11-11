package com.caterassist.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.caterassist.app.R;
import com.caterassist.app.models.VendorItem;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class VendorItemsAdapter extends RecyclerView.Adapter<VendorItemsAdapter.VendorItemsViewHolder> {
    private ArrayList<VendorItem> vendorItemArrayList;

    public void setVendorItemArrayList(ArrayList<VendorItem> vendorItemArrayList) {
        this.vendorItemArrayList = vendorItemArrayList;
    }

    @NonNull
    @Override
    public VendorItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_vendor_item, parent, false);
        return new VendorItemsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VendorItemsViewHolder holder, int position) {
        VendorItem vendorItem = vendorItemArrayList.get(position);
        holder.name.setText(vendorItem.getName());
        holder.category.setText(vendorItem.getCategory());
        holder.rate.setText(String.valueOf(vendorItem.getRatePerUnit()));
        String itemInStock = vendorItem.getStock() + " " + vendorItem.getUnit();
        holder.category.setText(itemInStock);
        //TODO: Set item image.
    }

    @Override
    public int getItemCount() {
        return vendorItemArrayList.size();
    }

    class VendorItemsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView image;
        TextView name;
        TextView category;
        TextView rate;
        TextView qtyInStock;
        LinearLayout addToCart;

        public VendorItemsViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.li_item_vend_item_image);
            name = itemView.findViewById(R.id.li_item_vend_item_name);
            category = itemView.findViewById(R.id.li_item_vend_item_category);
            rate = itemView.findViewById(R.id.li_item_vend_item_rate);
            qtyInStock = itemView.findViewById(R.id.li_item_vend_item_stock);
            addToCart = itemView.findViewById(R.id.li_item_vend_item_add_to_cart);

            addToCart.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.li_item_vend_item_add_to_cart:
                    //TODO: Add to cart
                    //Check whether the item already exixts in the cart
                    //If so then update its quantity.
                    //If not then add it to the cart.
                    break;
            }
        }
    }
}
