package com.caterassist.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.adapters.OrderItemsAdapter;
import com.caterassist.app.models.CartItem;
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

    RecyclerView orderItemsRecyclerView;
    ArrayList<CartItem> cartItemArrayList;
    OrderItemsAdapter orderItemsAdapter;
    String orderBranchName;
    String orderId;
    private LinearLayoutManager orderItemsLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        Intent intent = getIntent();
        orderBranchName = intent.getStringExtra(Constants.IntentExtrasKeys.ORDER_DETAILS_BRANCH);
        orderId = intent.getStringExtra(Constants.IntentExtrasKeys.ORDER_ID);
        if (orderBranchName != null && orderId != null) {
            initViews();
            fetchItems();
        } else {
            Toasty.error(this, "Some error occured! Try again...", Toast.LENGTH_SHORT).show();
            finish();
        }
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
        orderItemsRecyclerView = findViewById(R.id.act_ord_det_order_items_recyc_view);
        cartItemArrayList = new ArrayList<>();
        orderItemsAdapter = new OrderItemsAdapter();
        orderItemsAdapter.setCartItemArrayList(cartItemArrayList);
        orderItemsLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        orderItemsRecyclerView.setLayoutManager(orderItemsLayoutManager);
        orderItemsRecyclerView.setAdapter(orderItemsAdapter);
    }
}
