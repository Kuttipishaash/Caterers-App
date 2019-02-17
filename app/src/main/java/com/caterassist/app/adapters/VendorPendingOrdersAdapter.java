package com.caterassist.app.adapters;

import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.activities.CatererProfileActivity;
import com.caterassist.app.activities.OrderDetailsActivity;
import com.caterassist.app.models.OrderDetails;
import com.caterassist.app.utils.Constants;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class VendorPendingOrdersAdapter extends RecyclerView.Adapter<VendorPendingOrdersAdapter.ViewHolder> {
    private ArrayList<OrderDetails> orderDetailsArrayList;
    private static final String TAG = "VendPendingOrdersAdpt";

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

        String[] timeStampStr = orderDetails.getOrderTime().split(" ", 2);

        holder.orderTimeStampTxtView.setText(timeStampStr[0]);
        holder.orderTimeStampTimeTxtView.setText(timeStampStr[1]);

        holder.vendorNameTxtView.setText(orderDetails.getCatererName());

        String totalAmount = "₹" + String.valueOf(orderDetails.getOrderTotalAmount());
        holder.orderTotalAmtTxtView.setText(totalAmount);
        holder.caterLocation.setText(orderDetails.getCatererPhone());

        String buttonText;
        String statusText;
        switch (orderDetails.getOrderStatus()) {
            case 0:
                buttonText = "Approve";
                holder.rejectOrderBtn.setVisibility(View.VISIBLE);
                holder.updateStatusBtn.setVisibility(View.VISIBLE);
                statusText = "Awaiting approval!";
                break;
            case 1:
                buttonText = "Complete";
                holder.rejectOrderBtn.setVisibility(View.GONE);
                holder.updateStatusBtn.setVisibility(View.VISIBLE);
                statusText = "Approved";
                break;
            case 2:
                buttonText = "";
                holder.rejectOrderBtn.setVisibility(View.GONE);
                holder.updateStatusBtn.setVisibility(View.GONE);
                statusText = "Completed!";
                break;
            default:
                buttonText = "";
                statusText = "Status Unavailable";
                holder.rejectOrderBtn.setVisibility(View.GONE);
                holder.updateStatusBtn.setVisibility(View.GONE);
                break;
        }
        holder.updateStatusBtn.setText(buttonText);
        holder.orderStatusTxtView.setText(statusText);
        if (orderDetails.getExtraNotes() != null && !orderDetails.getExtraNotes().equals("")) {
            holder.extraNotesLayout.setVisibility(View.VISIBLE);
            holder.extraNotesTxtView.setText(orderDetails.getExtraNotes());
        }
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
        TextView orderTimeStampTimeTxtView;
        TextView vendorNameTxtView;
        TextView extraNotesTxtView;
        TextView orderTotalAmtTxtView;
        ImageView caterProfileImage;
        TextView caterLocation;
        Button updateStatusBtn;
        Button rejectOrderBtn;
        ImageButton deleteOrderBtn;
        ImageButton viewCatererBtn;
        LinearLayout extraNotesLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parentLayout = itemView.findViewById(R.id.caterer_order_info_parent);
            orderIDTxtView = itemView.findViewById(R.id.li_caterer_order_info_id);
            orderStatusTxtView = itemView.findViewById(R.id.li_caterer_order_info_status);
            vendorNameTxtView = itemView.findViewById(R.id.li_caterer_order_info_vendor_name);
            extraNotesTxtView = itemView.findViewById(R.id.li_order_info_extra_notes);
            extraNotesLayout = itemView.findViewById(R.id.li_order_info_extra_notes_layout);
            orderTimeStampTxtView = itemView.findViewById(R.id.li_caterer_order_info_timestamp);
            orderTimeStampTimeTxtView = itemView.findViewById(R.id.li_caterer_order_info_timestamp_time);
            orderTotalAmtTxtView = itemView.findViewById(R.id.li_caterer_order_info_order_total);
            updateStatusBtn = itemView.findViewById(R.id.li_caterer_order_status_update);
            rejectOrderBtn = itemView.findViewById(R.id.li_caterer_order_reject);
            caterLocation = itemView.findViewById(R.id.li_caterer_order_info_vendor_location);
            caterProfileImage = itemView.findViewById(R.id.li_caterer_order_info_image_view);
            deleteOrderBtn = itemView.findViewById(R.id.li_caterer_order_info_delete);
            viewCatererBtn = itemView.findViewById(R.id.li_caterer_order_info_view_vendor);

            parentLayout.setOnClickListener(this);
            updateStatusBtn.setOnClickListener(this);
            rejectOrderBtn.setOnClickListener(this);
            viewCatererBtn.setOnClickListener(this);

            deleteOrderBtn.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == parentLayout.getId()) {
                Intent intent = new Intent(itemView.getContext(), OrderDetailsActivity.class);
                intent.putExtra(Constants.IntentExtrasKeys.ORDER_DETAILS_BRANCH, FirebaseUtils.VENDOR_PENDING_ORDERS);
                intent.putExtra(Constants.IntentExtrasKeys.ORDER_ID, orderDetailsArrayList.get(getAdapterPosition()).getOrderId());
                intent.putExtra(Constants.IntentExtrasKeys.ORDER_INFO, orderDetailsArrayList.get(getAdapterPosition()));
                itemView.getContext().startActivity(intent);
            } else if (v.getId() == updateStatusBtn.getId()) {
                updateOrderStatus();
            } else if (v.getId() == rejectOrderBtn.getId()) {
                rejectOrder();
            } else if (v.getId() == viewCatererBtn.getId()) {
                Intent viewCatererIntent = new Intent(itemView.getContext(), CatererProfileActivity.class);
                viewCatererIntent.putExtra(Constants.IntentExtrasKeys.USER_ID, orderDetailsArrayList.get(getAdapterPosition()).getCatererID());
                itemView.getContext().startActivity(viewCatererIntent);
            }
        }

        private void rejectOrder() {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(itemView.getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth);
            } else {
                builder = new AlertDialog.Builder(itemView.getContext());
            }
            final OrderDetails orderDetails = orderDetailsArrayList.get(getAdapterPosition());
            String message = "Reject order?";
            String subMessage = " reject the following order ";
            builder.setTitle(message)
                    .setMessage("This action cannot be undone. You are about to" + subMessage + "\nOrder id :" + orderDetails.getOrderId()
                            + "\nOrdered by: " + orderDetails.getCatererName()
                            + "\nOn: " + orderDetails.getOrderTime())
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        rejectOrder(orderDetails.getCatererID(), orderDetails.getVendorName(), orderDetails.getOrderId(), orderDetails.getVendorId())
                                .addOnCompleteListener(task -> {
                                    if (!task.isSuccessful()) {
                                        Exception e = task.getException();
                                        if (e instanceof FirebaseFunctionsException) {
                                            FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                            FirebaseFunctionsException.Code code = ffe.getCode();
                                            Object details = ffe.getDetails();
                                            Log.e(TAG, "onComplete: " + ffe);
                                            if (details != null)
                                                Log.e(TAG, "onComplete: " + details.toString());
                                            Toasty.error(itemView.getContext(), "Order rejection failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toasty.info(itemView.getContext(), "Order will be rejected", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss())
                    .show();


        }

        private Task<String> rejectOrder(String catererID, String vendorName, String orderID, String vendorID) {
            // Create the arguments to the callable function.

            Map<String, Object> data = new HashMap<>();
            data.put("catererID", catererID);
            data.put("orderID", orderID);
            data.put("vendorName", vendorName);
            data.put("vendorID", vendorID);

            return FirebaseFunctions.getInstance()
                    .getHttpsCallable("sendRejectionNotification")
                    .call(data)
                    .continueWith(task -> {
                        String result = (String) task.getResult().getData();
                        return result;
                    });
        }

        private void updateOrderStatus() {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(itemView.getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth);
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
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.VENDOR_PENDING_ORDERS +
                                FirebaseAuth.getInstance().getUid() + "/" +
                                orderDetailsArrayList.get(getAdapterPosition()).getOrderId() + "/" +
                                FirebaseUtils.ORDER_INFO_BRANCH +
                                FirebaseUtils.ORDER_STATUS;
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
                        //TODO: show progress bar
                        databaseReference.setValue(orderDetailsArrayList.get(getAdapterPosition()).getOrderStatus() + 1)
                                .addOnSuccessListener(aVoid -> Toasty.success(itemView.getContext(), "Order status updated successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toasty.error(itemView.getContext(), "Order status update failed! Please try again...", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }
}
