package com.caterassist.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.caterassist.app.R;
import com.caterassist.app.models.CartItem;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartItemViewHolder> {
    private ArrayList<CartItem> cartItemsArrayList;

    public void setCartItemsArrayList(ArrayList<CartItem> cartItemsArrayList) {
        this.cartItemsArrayList = cartItemsArrayList;
    }

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_cart_item, parent, false);
        return new CartItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {
        CartItem cartItem = cartItemsArrayList.get(position);
        //TODO:Set image
        holder.itemNameTextView.setText(cartItem.getName());
        holder.itemRateTextView.setText(String.valueOf(cartItem.getRate()));
        holder.itemTotalTextView.setText(String.valueOf(cartItem.getTotalAmount()));
        holder.itemQtyEdtTxt.setText(String.valueOf(cartItem.getQuantity()));
        holder.tempQuantity = cartItem.getQuantity();
    }

    @Override
    public int getItemCount() {
        return cartItemsArrayList.size();
    }

    class CartItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnFocusChangeListener {

        ImageView itemImageView;
        TextView itemNameTextView;
        TextView itemRateTextView;
        TextView itemTotalTextView;
        EditText itemQtyEdtTxt;
        ImageButton itemQtyIncrease;
        ImageButton itemQtyDecrease;
        double tempQuantity;

        public CartItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.li_item_cart_item_image);
            itemNameTextView = itemView.findViewById(R.id.li_item_cart_item_name);
            itemRateTextView = itemView.findViewById(R.id.li_item_cart_item_rate);
            itemTotalTextView = itemView.findViewById(R.id.li_item_cart_item_total);
            itemQtyEdtTxt = itemView.findViewById(R.id.li_item_cart_item_qty);
            itemQtyIncrease = itemView.findViewById(R.id.li_item_cart_qty_inc);
            itemQtyDecrease = itemView.findViewById(R.id.li_item_cart_qty_dec);
            tempQuantity = 1;

            itemQtyIncrease.setOnClickListener(this);
            itemQtyDecrease.setOnClickListener(this);
            itemQtyEdtTxt.setOnFocusChangeListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.li_item_cart_qty_inc:
                    //TODO:Increase qty
                    break;
                case R.id.li_item_cart_qty_dec:
                    //TODO:Decrease qty
                    break;
            }
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (v.getId() == R.id.li_item_cart_item_qty) {
                if (hasFocus) {
                    tempQuantity = cartItemsArrayList.get(getAdapterPosition()).getQuantity();
                } else {
                    if (tempQuantity != cartItemsArrayList.get(getAdapterPosition()).getQuantity()) {
                        //TODO: change quantity.
                    }
                }
            }
        }
    }
}
