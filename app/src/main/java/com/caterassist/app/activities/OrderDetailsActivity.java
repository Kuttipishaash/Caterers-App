package com.caterassist.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

public class OrderDetailsActivity extends Activity {

    private RecyclerView orderItemsRecyclerView;
    private TextView userTypeTxtView;
    private TextView userNameTxtView;
    private TextView orderTimestampTxtView;
    private TextView orderIDTxtView;
    private TextView orderTotalAmtTxtView;


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
            userTypeTxtView.setText("Vendor Name: ");
            userNameTxtView.setText(orderDetails.getVendorName());
        }
        orderIDTxtView.setText(orderId);
        orderTotalAmtTxtView.setText(String.valueOf(orderDetails.getOrderTotalAmount()));
        orderTimestampTxtView.setText(String.valueOf(orderDetails.getOrderTime()));
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
                Toasty.error(OrderDetailsActivity.this, "Some error occured!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        userTypeTxtView = findViewById(R.id.act_ord_det_caterer_or_vendor);
        userNameTxtView = findViewById(R.id.act_ord_det_order_placed_user);
        orderIDTxtView = findViewById(R.id.act_ord_det_order_id);
        orderTimestampTxtView = findViewById(R.id.act_ord_det_order_timestamp);
        orderTotalAmtTxtView = findViewById(R.id.act_ord_det_order_total_amt);
        orderItemsRecyclerView = findViewById(R.id.act_ord_det_order_items_recyc_view);
        cartItemArrayList = new ArrayList<>();
        orderItemsAdapter = new OrderItemsAdapter();
        orderItemsAdapter.setCartItemArrayList(cartItemArrayList);
        orderItemsLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        orderItemsRecyclerView.setLayoutManager(orderItemsLayoutManager);
        orderItemsRecyclerView.setAdapter(orderItemsAdapter);
    }
}
