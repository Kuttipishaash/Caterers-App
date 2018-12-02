package com.caterassist.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.caterassist.app.R;
import com.caterassist.app.models.CartItem;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.OrderDetailsViewHolder> {
    ArrayList<CartItem> cartItemArrayList;

    public void setCartItemArrayList(ArrayList<CartItem> cartItemArrayList) {
        this.cartItemArrayList = cartItemArrayList;
    }

    @NonNull
    @Override
    public OrderDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_order_details, parent, false);
        return new OrderDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailsViewHolder holder, int position) {
        CartItem cartItem = cartItemArrayList.get(position);
        //ToDO: set image view image
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
