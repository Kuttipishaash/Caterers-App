package com.caterassist.app.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.adapters.HistoryOrderInfoAdapter;
import com.caterassist.app.models.OrderDetails;
import com.caterassist.app.utils.AppUtils;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class OrderHistoryActivity extends Activity {
    private static final String TAG = "CatererOrderInfo";
    HistoryOrderInfoAdapter historyOrderInfoAdapter;
    boolean isVendor;
    Query query;
    private RecyclerView orderHistoryRecycView;
    private ArrayList<OrderDetails> orderDetailsArrayList;
    private ChildEventListener childEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        isVendor = AppUtils.isCurrentUserVendor(this);
        orderDetailsArrayList = new ArrayList<>();
        orderHistoryRecycView = findViewById(R.id.act_order_hist_recyc_view);
        fetchOrderDetails();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (childEventListener != null) {
            query.removeEventListener(childEventListener);
        }
    }

    private void fetchOrderDetails() {
        historyOrderInfoAdapter = new HistoryOrderInfoAdapter();
        historyOrderInfoAdapter.setOrderDetailsArrayList(orderDetailsArrayList);
        historyOrderInfoAdapter.setVendor(isVendor);
        RecyclerView.LayoutManager catererOrdersLayoutManager = new LinearLayoutManager(OrderHistoryActivity.this, RecyclerView.VERTICAL, false);
        orderHistoryRecycView.setLayoutManager(catererOrdersLayoutManager);
        orderHistoryRecycView.setAdapter(historyOrderInfoAdapter);
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + getOrdersBranchName() + FirebaseAuth.getInstance().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
        query = databaseReference.orderByChild(FirebaseUtils.ORDER_INFO_SORT_CHILD);
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                DataSnapshot orderDetailsSnapshot = dataSnapshot.child(FirebaseUtils.ORDER_INFO_BRANCH);
                OrderDetails orderDetails = orderDetailsSnapshot.getValue(OrderDetails.class);
                orderDetails.setOrderId(dataSnapshot.getKey());
                orderDetailsArrayList.add(orderDetails);
                historyOrderInfoAdapter.notifyDataSetChanged();
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
                        historyOrderInfoAdapter.notifyDataSetChanged();
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
                        historyOrderInfoAdapter.notifyDataSetChanged();
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
        };
        query.addChildEventListener(childEventListener);
    }


    private String getOrdersBranchName() {
        if (isVendor) {
            return FirebaseUtils.ORDERS_VENDOR_BRANCH;
        } else {
            return FirebaseUtils.ORDERS_CATERER_BRANCH;
        }
    }
}
