package com.caterassist.app.adapters;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.activities.OrderDetailsActivity;
import com.caterassist.app.models.OrderDetails;
import com.caterassist.app.utils.Constants;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class VendorPendingOrdersAdapter extends RecyclerView.Adapter<VendorPendingOrdersAdapter.ViewHolder> {
    ArrayList<OrderDetails> orderDetailsArrayList;

    public void setOrderDetailsArrayList(ArrayList<OrderDetails> orderDetailsArrayList) {
        this.orderDetailsArrayList = orderDetailsArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_vendor_new_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDetails orderDetails = orderDetailsArrayList.get(position);
        holder.orderIDTxtView.setText(orderDetails.getOrderId());
        holder.orderTimeStampTxtView.setText(orderDetails.getOrderTime());
        holder.vendorNameTxtView.setText(orderDetails.getVendorName());
        holder.orderTotalAmtTxtView.setText(String.valueOf(orderDetails.getOrderTotalAmount()));
        String buttonText;
        String statusText;
        switch (orderDetails.getOrderStatus()) {
            case 0:
                buttonText = "Approve Order";
                holder.updateStatusBtn.setVisibility(View.VISIBLE);
                statusText = "Order awaiting approval!";
                break;
            case 1:
                buttonText = "Mark as Ready";
                holder.updateStatusBtn.setVisibility(View.VISIBLE);
                statusText = "Approved";
                break;
            case 2:
                buttonText = "Mark as Completed";
                holder.updateStatusBtn.setVisibility(View.VISIBLE);
                statusText = "Order ready";
                break;
            case 3:
                buttonText = "";
                holder.updateStatusBtn.setVisibility(View.GONE);
                statusText = "Order completed!";
                break;
            default:
                buttonText = "";
                statusText = "Status Unavailable";
                holder.updateStatusBtn.setVisibility(View.GONE);
                break;
        }
        holder.updateStatusBtn.setText(buttonText);
        holder.orderStatusTxtView.setText(statusText);
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
        Button updateStatusBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parentLayout = itemView.findViewById(R.id.caterer_order_info_parent);
            orderIDTxtView = itemView.findViewById(R.id.li_caterer_order_info_id);
            orderStatusTxtView = itemView.findViewById(R.id.li_caterer_order_info_status);
            vendorNameTxtView = itemView.findViewById(R.id.li_caterer_order_info_vendor_name);
            orderTimeStampTxtView = itemView.findViewById(R.id.li_caterer_order_info_timestamp);
            orderTotalAmtTxtView = itemView.findViewById(R.id.li_caterer_order_info_order_total);
            updateStatusBtn = itemView.findViewById(R.id.li_caterer_order_status_update);
            parentLayout.setOnClickListener(this);
            updateStatusBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == parentLayout.getId()) {
                Intent intent = new Intent(itemView.getContext(), OrderDetailsActivity.class);
                intent.putExtra(Constants.IntentExtrasKeys.ORDER_DETAILS_BRANCH, FirebaseUtils.VENDOR_PENDING_ORDERS);
                intent.putExtra(Constants.IntentExtrasKeys.ORDER_ID, orderDetailsArrayList.get(getAdapterPosition()).getOrderId());
                itemView.getContext().startActivity(intent);
            } else if (v.getId() == updateStatusBtn.getId()) {
                updateOrderStatus();
            }
        }

        private void updateOrderStatus() {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(itemView.getContext(), android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(itemView.getContext());
            }
            final OrderDetails orderDetails = orderDetailsArrayList.get(getAdapterPosition());
            String message;
            String subMessage;
            switch (orderDetails.getOrderStatus()) {
                case 0:
                    message = "Approve order?";
                    subMessage = " approve the following order ";
                    break;
                case 1:
                    message = "Mark order as ready?";
                    subMessage = " mark the following order as READY ";
                    break;
                case 2:
                    message = "Mark order as completed?";
                    subMessage = " mark the following order as COMPLETED ";
                    break;
                default:
                    message = "";
                    subMessage = " update status of the following order ";
                    break;

            }
            builder.setTitle(message)
                    .setMessage("This action cannot be undone. You are about to" + subMessage + "\nOrder id :" + orderDetails.getOrderId()
                            + "\nOrdered by: " + orderDetails.getCatererName()
                            + "\nOn: " + orderDetails.getOrderTime())
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.VENDOR_PENDING_ORDERS +
                                    FirebaseAuth.getInstance().getUid() + "/" +
                                    orderDetailsArrayList.get(getAdapterPosition()).getOrderId() + "/" +
                                    FirebaseUtils.ORDER_INFO_BRANCH +
                                    FirebaseUtils.ORDER_STATUS;
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
                            //TODO: show progress bar
                            databaseReference.setValue(orderDetailsArrayList.get(getAdapterPosition()).getOrderStatus() + 1)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toasty.success(itemView.getContext(), "Order status updated successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toasty.error(itemView.getContext(), "Order status update failed! Please try again...", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }
}
