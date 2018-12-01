package com.caterassist.app.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.adapters.CatererOrderInfoAdapter;
import com.caterassist.app.adapters.VendorOrderInfoAdapter;
import com.caterassist.app.models.OrderDetails;
import com.caterassist.app.utils.AppUtils;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class OrderHistoryActivity extends Activity {
    private static final String TAG = "CatererOrderInfo";
    CatererOrderInfoAdapter catererOrderInfoAdapter;
    VendorOrderInfoAdapter vendorOrderInfoAdapter;
    boolean isVendor;
    private RecyclerView orderHistoryRecycView;
    private ArrayList<OrderDetails> orderDetailsArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        isVendor = AppUtils.isCurrentUserVendor(this);
        orderDetailsArrayList = new ArrayList<>();
        orderHistoryRecycView = findViewById(R.id.act_order_hist_recyc_view);
        if (isVendor) {
            fetchVendorOrderDetails();

        } else {
            fetchCatererOrderDetails();

        }
    }

    private void fetchCatererOrderDetails() {
        catererOrderInfoAdapter = new CatererOrderInfoAdapter();
        catererOrderInfoAdapter.setOrderDetailsArrayList(orderDetailsArrayList);
        RecyclerView.LayoutManager catererOrdersLayoutManager = new LinearLayoutManager(OrderHistoryActivity.this, RecyclerView.VERTICAL, false);
        orderHistoryRecycView.setLayoutManager(catererOrdersLayoutManager);
        orderHistoryRecycView.setAdapter(catererOrderInfoAdapter);
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.ORDERS_CATERER_BRANCH + FirebaseAuth.getInstance().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                DataSnapshot orderDetailsSnapshot = dataSnapshot.child(FirebaseUtils.ORDER_INFO_BRANCH);
                OrderDetails orderDetails = orderDetailsSnapshot.getValue(OrderDetails.class);
                orderDetails.setOrderId(dataSnapshot.getKey());
                orderDetailsArrayList.add(orderDetails);
                catererOrderInfoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                DataSnapshot orderDetailsSnapshot = dataSnapshot.child(FirebaseUtils.ORDER_INFO_BRANCH);
                OrderDetails orderDetails = orderDetailsSnapshot.getValue(OrderDetails.class);
                String orderKey = dataSnapshot.getKey();
                for (int i = 0; i < orderDetailsArrayList.size(); i++) {
                    if (orderDetailsArrayList.get(i).getOrderId().equals(orderKey)) {
                        orderDetailsArrayList.remove(i);
                        orderDetails.setOrderId(dataSnapshot.getKey());
                        orderDetailsArrayList.add(i, orderDetails);
                        catererOrderInfoAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String orderKey = dataSnapshot.getKey();
                for (int i = 0; i < orderDetailsArrayList.size(); i++) {
                    if (orderDetailsArrayList.get(i).getOrderId().equals(orderKey)) {
                        orderDetailsArrayList.remove(i);
                        catererOrderInfoAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                DataSnapshot orderDetailsSnapshot = dataSnapshot.child(FirebaseUtils.ORDER_INFO_BRANCH);
                OrderDetails orderDetails = orderDetailsSnapshot.getValue(OrderDetails.class);
                String vendorItemKey = dataSnapshot.getKey();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(OrderHistoryActivity.this, "Failed to load caterer orders.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchVendorOrderDetails() {
        vendorOrderInfoAdapter = new VendorOrderInfoAdapter();
        vendorOrderInfoAdapter.setOrderDetailsArrayList(orderDetailsArrayList);
        RecyclerView.LayoutManager catererOrdersLayoutManager = new LinearLayoutManager(OrderHistoryActivity.this, RecyclerView.VERTICAL, false);
        orderHistoryRecycView.setLayoutManager(catererOrdersLayoutManager);
        orderHistoryRecycView.setAdapter(vendorOrderInfoAdapter);
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.ORDERS_VENDOR_BRANCH + FirebaseAuth.getInstance().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                DataSnapshot orderDetailsSnapshot = dataSnapshot.child(FirebaseUtils.ORDER_INFO_BRANCH);
                OrderDetails orderDetails = orderDetailsSnapshot.getValue(OrderDetails.class);
                orderDetails.setOrderId(dataSnapshot.getKey());
                orderDetailsArrayList.add(orderDetails);
                vendorOrderInfoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                DataSnapshot orderDetailsSnapshot = dataSnapshot.child(FirebaseUtils.ORDER_INFO_BRANCH);
                OrderDetails orderDetails = orderDetailsSnapshot.getValue(OrderDetails.class);
                String orderKey = dataSnapshot.getKey();
                for (int i = 0; i < orderDetailsArrayList.size(); i++) {
                    if (orderDetailsArrayList.get(i).getOrderId().equals(orderKey)) {
                        orderDetailsArrayList.remove(i);
                        orderDetails.setOrderId(dataSnapshot.getKey());
                        orderDetailsArrayList.add(i, orderDetails);
                        vendorOrderInfoAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String orderKey = dataSnapshot.getKey();
                for (int i = 0; i < orderDetailsArrayList.size(); i++) {
                    if (orderDetailsArrayList.get(i).getOrderId().equals(orderKey)) {
                        orderDetailsArrayList.remove(i);
                        vendorOrderInfoAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                DataSnapshot orderDetailsSnapshot = dataSnapshot.child(FirebaseUtils.ORDER_INFO_BRANCH);
                OrderDetails orderDetails = orderDetailsSnapshot.getValue(OrderDetails.class);
                String vendorItemKey = dataSnapshot.getKey();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(OrderHistoryActivity.this, "Failed to load caterer orders.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String getOrdersBranchName() {
        if (isVendor) {
            //TODO: Change to vendor branch
            return FirebaseUtils.ORDERS_CATERER_BRANCH;
        } else {
            return FirebaseUtils.ORDERS_CATERER_BRANCH;
        }
    }
}
