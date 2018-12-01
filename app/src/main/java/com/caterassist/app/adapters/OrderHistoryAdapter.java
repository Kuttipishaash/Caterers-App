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

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {
    ArrayList<CartItem> orderItemArrayList;

    public void setOrderItemArrayList(ArrayList<CartItem> orderItemArrayList) {
        this.orderItemArrayList = orderItemArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_order_history, parent, false);
        return new OrderHistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem cartItem = orderItemArrayList.get(position);
        //TODO: Set item image
        holder.orderItemNameTxtView.setText(cartItem.getName());
        holder.orderItemQtyTxtView.setText(String.valueOf(cartItem.getQuantity()));
        holder.orderItemRateTxtView.setText(String.valueOf(cartItem.getRate()));
        holder.orderItemUnitTxtView.setText(cartItem.getUnit());
        holder.orderItemTotalCostTxtView.setText(String.valueOf(cartItem.getTotalAmount()));
    }

    @Override
    public int getItemCount() {
        return orderItemArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView orderItemImage;
        TextView orderItemNameTxtView;
        TextView orderItemQtyTxtView;
        TextView orderItemRateTxtView;
        TextView orderItemTotalCostTxtView;
        TextView orderItemUnitTxtView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderItemImage = itemView.findViewById(R.id.order_item_image);
            orderItemNameTxtView = itemView.findViewById(R.id.order_item_name);
            orderItemQtyTxtView = itemView.findViewById(R.id.order_item_qty);
            orderItemRateTxtView = itemView.findViewById(R.id.order_item_rate);
            orderItemTotalCostTxtView = itemView.findViewById(R.id.order_item_total_cost);
            orderItemUnitTxtView = itemView.findViewById(R.id.order_item_units);
        }
    }
}
