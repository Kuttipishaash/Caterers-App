package com.caterassist.app.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.caterassist.app.R;
import com.caterassist.app.activities.OrderDetailsActivity;
import com.caterassist.app.models.OrderDetails;
import com.caterassist.app.utils.Constants;
import com.caterassist.app.utils.FirebaseUtils;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class VendorPendingOrdersAdapter extends RecyclerView.Adapter<VendorPendingOrdersAdapter.ViewHolder> {
    ArrayList<OrderDetails> orderDetailsArrayList;

    public void setOrderDetailsArrayList(ArrayList<OrderDetails> orderDetailsArrayList) {
        this.orderDetailsArrayList = orderDetailsArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_caterer_order_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDetails orderDetails = orderDetailsArrayList.get(position);
        holder.orderIDTxtView.setText(orderDetails.getOrderId());
        holder.orderStatusTxtView.setText(String.valueOf(orderDetails.getOrderStatus()));
        holder.orderTimeStampTxtView.setText(orderDetails.getOrderTime());
        holder.vendorNameTxtView.setText(orderDetails.getVendorName());
        holder.orderTotalAmtTxtView.setText(String.valueOf(orderDetails.getOrderTotalAmount()));
    }

    @Override
    public int getItemCount() {
        return orderDetailsArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout parentLayout;
        TextView orderIDTxtView;
        TextView orderStatusTxtView;
        TextView orderTimeStampTxtView;
        TextView vendorNameTxtView;
        TextView orderTotalAmtTxtView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parentLayout = itemView.findViewById(R.id.caterer_order_info_parent);
            orderIDTxtView = itemView.findViewById(R.id.li_caterer_order_info_id);
            orderStatusTxtView = itemView.findViewById(R.id.li_caterer_order_info_status);
            vendorNameTxtView = itemView.findViewById(R.id.li_caterer_order_info_vendor_name);
            orderTimeStampTxtView = itemView.findViewById(R.id.li_caterer_order_info_timestamp);
            orderTotalAmtTxtView = itemView.findViewById(R.id.li_caterer_order_info_order_total);

            parentLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == parentLayout.getId()) {
                Intent intent = new Intent(itemView.getContext(), OrderDetailsActivity.class);
                intent.putExtra(Constants.IntentExtrasKeys.ORDER_DETAILS_BRANCH, FirebaseUtils.VENDOR_PENDING_ORDERS);
                intent.putExtra(Constants.IntentExtrasKeys.ORDER_ID, orderDetailsArrayList.get(getAdapterPosition()).getOrderId());
                itemView.getContext().startActivity(intent);

            }
        }
    }
}
