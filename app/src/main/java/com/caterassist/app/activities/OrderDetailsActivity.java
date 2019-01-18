package com.caterassist.app.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.adapters.OrderItemsAdapter;
import com.caterassist.app.models.CartItem;
import com.caterassist.app.models.OrderDetails;
import com.caterassist.app.utils.AppUtils;
import com.caterassist.app.utils.Constants;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class OrderDetailsActivity extends Activity implements View.OnClickListener {

    private RecyclerView orderItemsRecyclerView;
    private TextView userTypeTxtView;
    private TextView userNameTxtView;
    private TextView orderDateTxtView;
    private TextView orderTimeTxtView;
    private TextView orderIDTxtView;
    private TextView orderTotalAmtTxtView;
    private TextView orderStatusTxtView;
    private Button deleteOrderBtn;
    private Button viewVendorBtn;


    private ArrayList<CartItem> cartItemArrayList;
    private OrderItemsAdapter orderItemsAdapter;
    private String orderBranchName;
    private String orderId;
    private OrderDetails orderDetails;

    private LinearLayoutManager orderItemsLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        Intent intent = getIntent();
        orderBranchName = intent.getStringExtra(Constants.IntentExtrasKeys.ORDER_DETAILS_BRANCH);
        orderId = intent.getStringExtra(Constants.IntentExtrasKeys.ORDER_ID);
        orderDetails = (OrderDetails) intent.getSerializableExtra(Constants.IntentExtrasKeys.ORDER_INFO);
        if (orderBranchName != null && orderId != null) {
            initViews();
            setOrderInfo();
            fetchItems();
        } else {
            Toasty.error(this, "Some error occured! Try again...", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setOrderInfo() {
        if (AppUtils.isCurrentUserVendor(this)) {
            userTypeTxtView.setText("Caterer Name: ");
            userNameTxtView.setText(orderDetails.getCatererName());
        } else {
            viewVendorBtn.setVisibility(View.VISIBLE);
            userTypeTxtView.setText("Vendor Name: ");
            userNameTxtView.setText(orderDetails.getVendorName());
        }
        orderIDTxtView.setText(orderId);
        orderTotalAmtTxtView.setText(String.valueOf(orderDetails.getOrderTotalAmount()));
        String timeStamp[] = String.valueOf(orderDetails.getOrderTime()).split(" ");
        orderDateTxtView.setText(timeStamp[0]);
        orderTimeTxtView.setText(timeStamp[1]);
        String status;
        //TODO Set color in this switch case
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
        orderStatusTxtView.setText(status);
    }

    private void fetchItems() {
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + orderBranchName +
                FirebaseAuth.getInstance().getUid() + "/" + orderId + "/" + FirebaseUtils.ORDER_ITEMS_BRANCH;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CartItem cartItem = snapshot.getValue(CartItem.class);
                    cartItemArrayList.add(cartItem);
                    orderItemsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toasty.error(OrderDetailsActivity.this, "Error occured while fetching items!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        userTypeTxtView = findViewById(R.id.act_ord_det_caterer_or_vendor);
        userNameTxtView = findViewById(R.id.act_ord_det_order_placed_user);
        orderIDTxtView = findViewById(R.id.act_ord_det_order_id);
        orderDateTxtView = findViewById(R.id.act_ord_det_order_date);
        orderTimeTxtView = findViewById(R.id.act_ord_det_order_time);
        orderTotalAmtTxtView = findViewById(R.id.act_ord_det_order_total_amt);
        orderStatusTxtView = findViewById(R.id.act_ord_det_order_status);
        deleteOrderBtn = findViewById(R.id.act_ord_det_order_delete_btn);
        orderItemsRecyclerView = findViewById(R.id.act_ord_det_order_items_recyc_view);
        viewVendorBtn = findViewById(R.id.act_order_det_view_vend);

        deleteOrderBtn.setOnClickListener(this);
        viewVendorBtn.setOnClickListener(this);

        cartItemArrayList = new ArrayList<>();
        orderItemsAdapter = new OrderItemsAdapter();
        orderItemsAdapter.setCartItemArrayList(cartItemArrayList);
        orderItemsLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        orderItemsRecyclerView.setLayoutManager(orderItemsLayoutManager);
        orderItemsRecyclerView.setAdapter(orderItemsAdapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == deleteOrderBtn.getId()) {
            if (orderDetails.getOrderStatus() > 1) {
                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_title_delete_order_history))
                        .setMessage(
                                getResources().getString(R.string.dialog_message_delete_order_history))
                        .setPositiveButton(
                                getResources().getString(R.string.dialog_btn_yes),
                                (dialog, which) -> deleteItem())
                        .setNegativeButton((getResources().getString(R.string.dialog_btn_no))
                                , (dialog, which) -> dialog.dismiss()).show();
            } else {
                Toasty.warning(this, "You cannot delete an unfulfilled order!").show();
            }
        } else if (v.getId() == viewVendorBtn.getId()) {
            Intent viewVendorIntent = new Intent(OrderDetailsActivity.this, ViewVendorItemsActivity.class);
            viewVendorIntent.putExtra(Constants.IntentExtrasKeys.VIEW_VENDOR_ITEMS_INTENT_VENDOR_UID, orderDetails.getVendorId());
            startActivity(viewVendorIntent);
            finish();
        }
    }

    private void deleteItem() {
        String orderID = orderDetails.getOrderId();
        String branch = AppUtils.isCurrentUserVendor(this) ? FirebaseUtils.ORDERS_VENDOR_BRANCH : FirebaseUtils.ORDERS_CATERER_BRANCH;
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + branch +
                FirebaseAuth.getInstance().getUid() + "/" + orderID;
        DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference(databasePath);
        orderReference.setValue(null)
                .addOnSuccessListener(aVoid -> {
                    Toasty.success(this, "Order deleted from history.").show();
                    finish();
                })
                .addOnFailureListener(e -> Toasty.error(this, "Failed to delete order from history!").show());
    }
}
