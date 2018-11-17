package com.caterassist.app.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.adapters.CartAdapter;
import com.caterassist.app.models.CartItem;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CartActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "CartActivity";

    private DatabaseReference cartItemsReference;
    private ChildEventListener cartItemsEventListener;
    private ArrayList<CartItem> cartItemsArrayList;
    private LinearLayoutManager cartItemsLayoutManager;
    private CartAdapter cartItemsAdapter;


    private RecyclerView cartItemsRecyclerView;
    private LinearLayout checkoutButton;
    private LinearLayout clearCartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        initViews();
        fetchCartItems();
    }

    private void fetchCartItems() {
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() +
                FirebaseUtils.CART_BRANCH_NAME +
                FirebaseAuth.getInstance().getUid() + "/" +
                FirebaseUtils.CART_ITEMS_BRANCH;

        cartItemsReference = FirebaseDatabase.getInstance().getReference(databasePath);
        cartItemsEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new CartItem has been added, add it to the displayed list
                CartItem cartItem = dataSnapshot.getValue(CartItem.class);
                cartItem.setId(dataSnapshot.getKey());
                cartItemsArrayList.add(cartItem);
                cartItemsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A CartItem has changed, use the key to determine if we are displaying this
                // CartItem and if so displayed the changed CartItem.
                CartItem cartItem = dataSnapshot.getValue(CartItem.class);
                String cartItemKey = dataSnapshot.getKey();
                for (int i = 0; i < cartItemsArrayList.size(); i++) {
                    if (cartItemsArrayList.get(i).getId().equals(cartItemKey)) {
                        cartItemsArrayList.remove(i);
                        cartItem.setId(dataSnapshot.getKey());
                        cartItemsArrayList.add(i, cartItem);
                        cartItemsAdapter.notifyDataSetChanged();
                        break;
                    }
                }
                // ...
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A CartItem has changed, use the key to determine if we are displaying this
                // CartItem and if so remove it.
                String cartItemKey = dataSnapshot.getKey();
                for (int i = 0; i < cartItemsArrayList.size(); i++) {
                    if (cartItemsArrayList.get(i).getId().equals(cartItemKey)) {
                        cartItemsArrayList.remove(i);
                        cartItemsAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A CartItem has changed position, use the key to determine if we are
                // displaying this CartItem and if so move it.
                CartItem cartItem = dataSnapshot.getValue(CartItem.class);
                String cartItemKey = dataSnapshot.getKey();
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(CartActivity.this, "Failed to load cart items.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        cartItemsReference.addChildEventListener(cartItemsEventListener);
        cartItemsAdapter = new CartAdapter();
        cartItemsAdapter.setCartItemsArrayList(cartItemsArrayList);
        cartItemsLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        cartItemsRecyclerView.setLayoutManager(cartItemsLayoutManager);
        cartItemsRecyclerView.setAdapter(cartItemsAdapter);
    }

    private void initViews() {
        cartItemsRecyclerView = findViewById(R.id.act_cart_recyc_cart_items);
        checkoutButton = findViewById(R.id.act_cart_btn_checkout);
        clearCartButton = findViewById(R.id.act_cart_btn_clear_cart);
        cartItemsArrayList = new ArrayList<>();
        checkoutButton.setOnClickListener(this);
        clearCartButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.act_cart_btn_checkout:
                checkout();
                break;
            case R.id.act_cart_btn_clear_cart:
                if (cartItemsReference != null) {
                    Objects.requireNonNull(cartItemsReference.getParent()).setValue(null);
                }
                break;
        }
    }

    private void checkout() {
        String userOrdersDatabasePath = FirebaseUtils.getDatabaseMainBranchName() +
                FirebaseUtils.ORDERS_CATERER_BRANCH +
                FirebaseAuth.getInstance().getUid();
        DatabaseReference checkoutReferecne = FirebaseDatabase.getInstance().getReference(userOrdersDatabasePath);
        checkoutReferecne.push().setValue(cartItemsArrayList);
        //TODO Upload order info also
    }
}
