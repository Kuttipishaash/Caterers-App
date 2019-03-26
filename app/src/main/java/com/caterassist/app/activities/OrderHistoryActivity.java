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
import com.caterassist.app.adapters.HistoryOrderInfoAdapter;
import com.caterassist.app.dialogs.LoadingDialog;
import com.caterassist.app.models.OrderDetails;
import com.caterassist.app.utils.AppUtils;
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

public class OrderHistoryActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "OrderHistory";
    private HistoryOrderInfoAdapter historyOrderInfoAdapter;
    private boolean isVendor;
    private Query query;
    private RecyclerView orderHistoryRecycView;
    private ArrayList<OrderDetails> orderDetailsArrayList;
    private ChildEventListener childEventListener;
    private LinearLayout noItemsView;
    private LoadingDialog loadingDialog;
    private Handler handler;
    private Runnable runnable;
    private Button dashboardLinkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        isVendor = AppUtils.isCurrentUserVendor(this);
        orderDetailsArrayList = new ArrayList<>();
        orderHistoryRecycView = findViewById(R.id.act_order_hist_recyc_view);
        noItemsView = findViewById(R.id.error_hist_orders_list_empty);
        dashboardLinkButton = findViewById(R.id.no_dash);
        dashboardLinkButton.setOnClickListener(this);
        loadingDialog = new LoadingDialog(this, "Loading orders...");
        loadingDialog.show();
        handler = new Handler();
        runnable = () -> {
            if (loadingDialog != null)
                if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                    Toast.makeText(OrderHistoryActivity.this,
                            "Please check your internet connection and try again!",
                            Toast.LENGTH_SHORT).show();
                    checkOrderEmpty();
                }
        };
        handler.postAtTime(runnable, System.currentTimeMillis() + Constants.UtilConstants.LOADING_TIMEOUT);
        handler.postDelayed(runnable, Constants.UtilConstants.LOADING_TIMEOUT);
        fetchOrderDetails();

    }

    private void checkOrderEmpty() {
        if (orderDetailsArrayList.size() > 0) {
            orderHistoryRecycView.setVisibility(VISIBLE);
            noItemsView.setVisibility(GONE);
        } else {
            noItemsView.setVisibility(VISIBLE);
            orderHistoryRecycView.setVisibility(GONE);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (childEventListener != null) {
            query.removeEventListener(childEventListener);
        }
    }

    @Override
    public void onBackPressed() {
        if (childEventListener != null) {
            query.removeEventListener(childEventListener);
        }
        super.onBackPressed();
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
                orderDetails.setOrderID(dataSnapshot.getKey());
                orderDetailsArrayList.add(orderDetails);
                historyOrderInfoAdapter.notifyDataSetChanged();
                checkOrderEmpty();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                DataSnapshot orderDetailsSnapshot = dataSnapshot.child(FirebaseUtils.ORDER_INFO_BRANCH);
                OrderDetails orderDetails = orderDetailsSnapshot.getValue(OrderDetails.class);
                String orderKey = dataSnapshot.getKey();
                for (int i = 0; i < orderDetailsArrayList.size(); i++) {
                    if (orderDetailsArrayList.get(i).getOrderID().equals(orderKey)) {
                        orderDetailsArrayList.remove(i);
                        if (orderDetails != null) {
                            orderDetails.setOrderID(dataSnapshot.getKey());
                            orderDetailsArrayList.add(i, orderDetails);
                            historyOrderInfoAdapter.notifyDataSetChanged();
                        }
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String orderKey = dataSnapshot.getKey();
                for (int i = 0; i < orderDetailsArrayList.size(); i++) {
                    if (orderDetailsArrayList.get(i).getOrderID().equals(orderKey)) {
                        orderDetailsArrayList.remove(i);
                        historyOrderInfoAdapter.notifyDataSetChanged();
                        break;
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
                Toasty.error(OrderHistoryActivity.this, "Failed to load caterer orders.",
                        Toast.LENGTH_SHORT).show();
                checkOrderEmpty();
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


    private String getOrdersBranchName() {
        if (isVendor) {
            return FirebaseUtils.ORDERS_VENDOR_BRANCH;
        } else {
            return FirebaseUtils.ORDERS_CATERER_BRANCH;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == dashboardLinkButton.getId()) {
            finish();
        }
    }
}
