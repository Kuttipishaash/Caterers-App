package com.caterassist.app.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.adapters.VendorPendingOrdersAdapter;
import com.caterassist.app.dialogs.LoadingDialog;
import com.caterassist.app.models.OrderDetails;
import com.caterassist.app.utils.Constants;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class VendorPendingOrdersActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "VendorNewOrders";
    Query query;
    ChildEventListener childEventListener;
    private ArrayList<OrderDetails> orderDetailsArrayList;
    private RecyclerView pendingOrdersRecycView;
    private LinearLayoutManager pendingOrdersLayoutManager;
    private VendorPendingOrdersAdapter pendingOrdersAdapter;
    private LinearLayout noItemsView;
    private LoadingDialog loadingDialog;
    private Handler handler;
    private Runnable runnable;
    private Button dashboardLinkBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_pending_orders);
        noItemsView = findViewById(R.id.error_pending_orders_list_empty);
        dashboardLinkBtn = findViewById(R.id.no_dash);
        dashboardLinkBtn.setOnClickListener(this);
        pendingOrdersRecycView = findViewById(R.id.act_vend_pending_orders_recyc_view);
        loadingDialog = new LoadingDialog(this, "Loading orders...");
        loadingDialog.show();
        handler = new Handler();
        runnable = () -> {
            if (loadingDialog != null)
                if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                    Toast.makeText(VendorPendingOrdersActivity.this,
                            "Please check your internet connection and try again!",
                            Toast.LENGTH_SHORT).show();
                    checkOrderEmpty();
                }
        };
        handler.postAtTime(runnable, System.currentTimeMillis() + Constants.UtilConstants.LOADING_TIMEOUT);
        handler.postDelayed(runnable, Constants.UtilConstants.LOADING_TIMEOUT);
        fetchOrders();
    }

    private void checkOrderEmpty() {
        if (orderDetailsArrayList.size() > 0) {
            pendingOrdersRecycView.setVisibility(VISIBLE);
            noItemsView.setVisibility(GONE);
        } else {
            noItemsView.setVisibility(VISIBLE);
            pendingOrdersRecycView.setVisibility(GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (childEventListener != null) {
            query.removeEventListener(childEventListener);
        }
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
                if (orderDetails != null) {
                    orderDetails.setOrderId(dataSnapshot.getKey());
                    orderDetailsArrayList.add(orderDetails);
                    pendingOrdersAdapter.notifyDataSetChanged();
                }
                checkOrderEmpty();
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
                checkOrderEmpty();
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
                Toasty.error(VendorPendingOrdersActivity.this, "Failed to load pending orders.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        query.addChildEventListener(childEventListener);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (loadingDialog != null) {
                    if (loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                }
                checkOrderEmpty();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (loadingDialog != null) {
                    if (loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                }
                checkOrderEmpty();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == dashboardLinkBtn.getId()) {
            finish();
        }
    }
}
