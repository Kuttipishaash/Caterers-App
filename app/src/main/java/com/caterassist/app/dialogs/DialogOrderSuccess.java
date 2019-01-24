package com.caterassist.app.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
    private static final String TAG = "DialogOrdSuccess";
    private View rootView;
    private TextView vendorNameTxtView;
    private TextView orderDateTxtView;
    private TextView orderTimeTxtView;
    private TextView orderItemCountTxtView;
    private TextView orderTotalAmtTxtView;
    private Button viewOrderBtn;
    private Button callVendorBtn;
    private Button dismissButton;

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
        callVendorBtn = rootView.findViewById(R.id.order_suc_call_vendor);
        dismissButton = rootView.findViewById(R.id.order_suc_diaglog_dismiss);

        dismissButton.setOnClickListener(this);
        viewOrderBtn.setOnClickListener(this);
        callVendorBtn.setOnClickListener(this);
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
        if (v.getId() == dismissButton.getId()) {
            dismiss();
        } else if (v.getId() == viewOrderBtn.getId()) {
            Intent intent = new Intent(getContext(), OrderDetailsActivity.class);

            intent.putExtra(Constants.IntentExtrasKeys.ORDER_DETAILS_BRANCH, FirebaseUtils.ORDERS_CATERER_BRANCH);
            intent.putExtra(Constants.IntentExtrasKeys.ORDER_ID, orderDetails.getOrderId());
            intent.putExtra(Constants.IntentExtrasKeys.ORDER_INFO, orderDetails);
            if (getContext() != null) {
                getContext().startActivity(intent);
            } else {
                Log.e(TAG, "orderViewIntent: Failed due to null context");
            }
        } else if (v.getId() == callVendorBtn.getId()) {
            String phoneNumber = orderDetails.getVendorPhone();
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
            if (getContext() != null) {
                getContext().startActivity(callIntent);
            } else {
                Log.e(TAG, "callIntent: Failed due to null context");
            }
        }
    }
}