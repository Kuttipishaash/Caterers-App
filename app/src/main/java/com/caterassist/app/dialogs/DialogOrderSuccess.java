package com.caterassist.app.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.caterassist.app.R;
import com.caterassist.app.activities.OrderDetailsActivity;
import com.caterassist.app.models.OrderDetails;
import com.caterassist.app.utils.Constants;
import com.caterassist.app.utils.FirebaseUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;


public class DialogOrderSuccess extends DialogFragment implements View.OnClickListener {
    private View rootView;
    private TextView vendorNameTxtView;
    private TextView orderDateTxtView;
    private TextView orderTimeTxtView;
    private TextView orderItemCountTxtView;
    private TextView orderTotalAmtTxtView;
    private Button viewOrderBtn;

    private OrderDetails orderDetails;
    private int itemCount;

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public void setOrderDetails(OrderDetails orderDetails) {
        this.orderDetails = orderDetails;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_dialog_order_success, container, false);

        initViews();
        setupViews();
        return rootView;
    }

    private void setupViews() {
        String timestamp[] = orderDetails.getOrderTime().split(" ");
        orderDateTxtView.setText(timestamp[0]);
        orderTimeTxtView.setText(timestamp[1]);
        vendorNameTxtView.setText(orderDetails.getVendorName());
        orderItemCountTxtView.setText(String.valueOf(itemCount));
        orderTotalAmtTxtView.setText(String.valueOf(orderDetails.getOrderTotalAmount()));
    }

    private void initViews() {
        orderDateTxtView = rootView.findViewById(R.id.order_suc_dialog_date);
        orderTimeTxtView = rootView.findViewById(R.id.order_suc_dialog_time);
        vendorNameTxtView = rootView.findViewById(R.id.order_suc_dialog_vend_name);
        orderItemCountTxtView = rootView.findViewById(R.id.order_suc_dialog_item_count);
        orderTotalAmtTxtView = rootView.findViewById(R.id.order_suc_dialog_total_amt);
        viewOrderBtn = rootView.findViewById(R.id.order_suc_view_order);

        rootView.findViewById(R.id.order_suc_diaglog_dismiss).setOnClickListener(this);
        viewOrderBtn.setOnClickListener(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.order_suc_diaglog_dismiss) {
            dismiss();
        } else if (v.getId() == R.id.order_suc_view_order) {
            Intent intent = new Intent(getContext(), OrderDetailsActivity.class);

            intent.putExtra(Constants.IntentExtrasKeys.ORDER_DETAILS_BRANCH, FirebaseUtils.ORDERS_CATERER_BRANCH);
            intent.putExtra(Constants.IntentExtrasKeys.ORDER_ID, orderDetails.getOrderId());
            intent.putExtra(Constants.IntentExtrasKeys.ORDER_INFO, orderDetails);
            getContext().startActivity(intent);

//            String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.ORDERS_CATERER_BRANCH +
//                    FirebaseAuth.getInstance().getUid() + + "/" +FirebaseUtils.ORDER_INFO_BRANCH;
//            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
//            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    OrderDetails orderDetails = dataSnapshot.getValue(OrderDetails.class);
//                    if(orderDetails!=null){
//                        Intent intent = new Intent(getContext(), OrderDetailsActivity.class);
//
//                            intent.putExtra(Constants.IntentExtrasKeys.ORDER_DETAILS_BRANCH, FirebaseUtils.ORDERS_CATERER_BRANCH);
//                        intent.putExtra(Constants.IntentExtrasKeys.ORDER_ID, orderDetails.getOrderId());
//                        intent.putExtra(Constants.IntentExtrasKeys.ORDER_INFO, orderDetails);
//                        getContext().startActivity(intent);
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
        }
    }
}