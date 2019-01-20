package com.caterassist.app.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caterassist.app.R;
import com.caterassist.app.models.CartItem;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.OrderDetailsViewHolder> {
    ArrayList<CartItem> cartItemArrayList;
    Activity activity;

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setCartItemArrayList(ArrayList<CartItem> cartItemArrayList) {
        this.cartItemArrayList = cartItemArrayList;
    }

    @NonNull
    @Override
    public OrderDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_order_item, parent, false);
        return new OrderDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailsViewHolder holder, int position) {
        CartItem cartItem = cartItemArrayList.get(position);
        String imageUrl = cartItem.getImageURL();
        if (imageUrl != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child(imageUrl).getDownloadUrl().addOnSuccessListener(uri -> {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.drawable.placeholder);
                requestOptions.error(R.drawable.ic_error_placeholder);
                Glide.with(activity.getApplicationContext())
                        .setDefaultRequestOptions(requestOptions)
                        .load(uri)
                        .into(holder.itemImageView);
            }).addOnFailureListener(exception -> holder.itemImageView.setImageResource(R.drawable.ic_error_placeholder));
        }
        holder.itemNameTxtView.setText(cartItem.getName());
        holder.itemRateTxtView.setText(String.valueOf(cartItem.getRate()));
        holder.itemQtyTxtView.setText(String.valueOf(cartItem.getQuantity()));
        holder.itemTotalTxtView.setText(String.valueOf(cartItem.getTotalAmount()));
    }

    @Override
    public int getItemCount() {
        return cartItemArrayList.size();
    }

    class OrderDetailsViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImageView;
        TextView itemNameTxtView;
        TextView itemRateTxtView;
        TextView itemQtyTxtView;
        TextView itemTotalTxtView;

        public OrderDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.li_ord_det_item_image);
            itemNameTxtView = itemView.findViewById(R.id.li_ord_det_item_name);
            itemRateTxtView = itemView.findViewById(R.id.li_ord_det_item_rate);
            itemQtyTxtView = itemView.findViewById(R.id.li_ord_det_item_qty);
            itemTotalTxtView = itemView.findViewById(R.id.li_ord_det_item_total);
        }
    }
}
