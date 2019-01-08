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

public class HistoryOrderInfoAdapter extends RecyclerView.Adapter<HistoryOrderInfoAdapter.ViewHolder> {
    ArrayList<OrderDetails> orderDetailsArrayList;
    boolean isVendor;

    public void setVendor(boolean vendor) {
        isVendor = vendor;
    }

    public void setOrderDetailsArrayList(ArrayList<OrderDetails> orderDetailsArrayList) {
        this.orderDetailsArrayList = orderDetailsArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_history_order_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDetails orderDetails = orderDetailsArrayList.get(position);
        holder.orderIDTxtView.setText(orderDetails.getOrderId());
        String status;
        switch (orderDetails.getOrderStatus()) {
            case 0:
                status = "Awaiting approval";
                break;
            case 1:
                status = "Approved and Processing";
                break;
            case 2:
                status = "Completed";
                break;
            case 3:
                status = "Rejected";
                break;
            default:
                status = "Status Unavailable";
                break;
        }
        holder.orderStatusTxtView.setText(status);
        holder.orderTimeStampTxtView.setText(orderDetails.getOrderTime());

        if (isVendor) {
            holder.orderStatusTxtView.setVisibility(View.GONE);
            holder.orderNameLabel.setText("Caterer : ");
            holder.nameTxtView.setText(orderDetails.getCatererName());
        } else {
            holder.orderNameLabel.setText("Vendor : ");
            holder.nameTxtView.setText(orderDetails.getVendorName());
        }

        holder.orderTotalAmtTxtView.setText(String.valueOf(orderDetails.getOrderTotalAmount()));
    }

    @Override
    public int getItemCount() {
        return orderDetailsArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout parentLayout;
        TextView orderIDTxtView;
        TextView orderNameLabel;
        TextView orderStatusTxtView;
        TextView orderTimeStampTxtView;
        TextView nameTxtView;
        TextView orderTotalAmtTxtView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parentLayout = itemView.findViewById(R.id.history_order_info_parent);
            orderIDTxtView = itemView.findViewById(R.id.li_history_order_info_id);
            orderStatusTxtView = itemView.findViewById(R.id.li_history_order_info_status);
            nameTxtView = itemView.findViewById(R.id.li_history_order_info_name);
            orderNameLabel = itemView.findViewById(R.id.li_history_order_info_name_label);
            orderTimeStampTxtView = itemView.findViewById(R.id.li_history_order_info_timestamp);
            orderTotalAmtTxtView = itemView.findViewById(R.id.li_history_order_info_order_total);

            parentLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == parentLayout.getId()) {
                Intent intent = new Intent(itemView.getContext(), OrderDetailsActivity.class);
                if (isVendor) {
                    intent.putExtra(Constants.IntentExtrasKeys.ORDER_DETAILS_BRANCH, FirebaseUtils.ORDERS_VENDOR_BRANCH);
                } else {
                    intent.putExtra(Constants.IntentExtrasKeys.ORDER_DETAILS_BRANCH, FirebaseUtils.ORDERS_CATERER_BRANCH);
                }
                intent.putExtra(Constants.IntentExtrasKeys.ORDER_ID, orderDetailsArrayList.get(getAdapterPosition()).getOrderId());
                itemView.getContext().startActivity(intent);
            }
        }
    }
}
