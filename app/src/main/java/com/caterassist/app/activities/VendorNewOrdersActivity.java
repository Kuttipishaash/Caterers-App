package com.caterassist.app.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.adapters.VendorPendingOrdersAdapter;
import com.caterassist.app.models.OrderDetails;
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

public class VendorNewOrdersActivity extends Activity {
    private static final String TAG = "VendorNewOrders";
    Query query;
    ChildEventListener childEventListener;
    private ArrayList<OrderDetails> orderDetailsArrayList;
    private RecyclerView pendingOrdersRecycView;
    private LinearLayoutManager pendingOrdersLayoutManager;
    private VendorPendingOrdersAdapter pendingOrdersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_new_orders);
        pendingOrdersRecycView = findViewById(R.id.act_vend_new_orders_recyc_view);
        fetchOrders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        query.addChildEventListener(childEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        query.removeEventListener(childEventListener);
    }

    private void fetchOrders() {
        orderDetailsArrayList = new ArrayList<>();
        pendingOrdersAdapter = new VendorPendingOrdersAdapter();
        pendingOrdersAdapter.setOrderDetailsArrayList(orderDetailsArrayList);
        pendingOrdersLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        pendingOrdersRecycView.setLayoutManager(pendingOrdersLayoutManager);
        pendingOrdersRecycView.setAdapter(pendingOrdersAdapter);

        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.VENDOR_PENDING_ORDERS + FirebaseAuth.getInstance().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
        query = databaseReference.orderByChild(FirebaseUtils.ORDER_INFO_SORT_CHILD);
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                DataSnapshot orderDetailsSnapshot = dataSnapshot.child(FirebaseUtils.ORDER_INFO_BRANCH);
                OrderDetails orderDetails = orderDetailsSnapshot.getValue(OrderDetails.class);
                orderDetails.setOrderId(dataSnapshot.getKey());
                orderDetailsArrayList.add(orderDetails);
                pendingOrdersAdapter.notifyDataSetChanged();
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
                        pendingOrdersAdapter.notifyDataSetChanged();
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
                        pendingOrdersAdapter.notifyDataSetChanged();
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
                Toast.makeText(VendorNewOrdersActivity.this, "Failed to load pending orders.",
                        Toast.LENGTH_SHORT).show();
            }
        };
    }
}
