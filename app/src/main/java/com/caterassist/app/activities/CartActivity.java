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
import com.caterassist.app.models.Order;
import com.caterassist.app.models.OrderDetails;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.utils.AppUtils;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class CartActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "CartActivity";

    private DatabaseReference cartItemsReference;
    private ChildEventListener cartItemsEventListener;
    private ArrayList<CartItem> cartItemsArrayList;
    private LinearLayoutManager cartItemsLayoutManager;
    private CartAdapter cartItemsAdapter;
    private UserDetails vendorDetails;

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cartItemsEventListener != null) {
            cartItemsReference.removeEventListener(cartItemsEventListener);
        }
    }

    private void fetchCartItems() {
        final String databasePath = FirebaseUtils.getDatabaseMainBranchName() +
                FirebaseUtils.CART_BRANCH_NAME +
                FirebaseAuth.getInstance().getUid() + "/" +
                FirebaseUtils.CART_ITEMS_BRANCH;

        cartItemsReference = FirebaseDatabase.getInstance().getReference(databasePath);
        DatabaseReference vendorIDReference = cartItemsReference.getParent().child(FirebaseUtils.CART_VENDOR_BRANCH);
        vendorIDReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                vendorDetails = dataSnapshot.getValue(UserDetails.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //TODO: Handle failure
            }
        });


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
                if (vendorDetails != null) {
                    checkout();
                }
                break;
            case R.id.act_cart_btn_clear_cart:
                if (cartItemsReference != null) {
                    Objects.requireNonNull(cartItemsReference.getParent()).setValue(null);
                }
                break;
        }
    }

    private void checkout() {

        double orderTotalAmt = 0.0;
        for (CartItem cartItem : cartItemsArrayList) {
            orderTotalAmt += cartItem.getTotalAmount();
        }
        final OrderDetails orderDetails = new OrderDetails();
        orderDetails.setVendorId(vendorDetails.getUserID());
        orderDetails.setCatererID(FirebaseAuth.getInstance().getUid());
        orderDetails.setOrderStatus(0);
        orderDetails.setVendorName(vendorDetails.getUserName());
        orderDetails.setVendorPhone(vendorDetails.getUserPhone());
        orderDetails.setOrderTotalAmount(orderTotalAmt);
        orderDetails.setCatererName(AppUtils.getCurrentUserName(this));
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        orderDetails.setOrderTime(formatter.format(date));

        String userOrdersItemsDatabasePath = FirebaseUtils.getDatabaseMainBranchName() +
                FirebaseUtils.ORDERS_CATERER_BRANCH +
                FirebaseAuth.getInstance().getUid();
        final DatabaseReference checkoutReferecne = FirebaseDatabase.getInstance().getReference(userOrdersItemsDatabasePath);
        Order order = new Order();
        order.setOrderItems(cartItemsArrayList);
        order.setOrderInfo(orderDetails);
        checkoutReferecne.push().setValue(order)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Objects.requireNonNull(cartItemsReference.getParent()).setValue(null)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toasty.success(CartActivity.this, "Checkout successful", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toasty.success(CartActivity.this, "Checkout failed", Toast.LENGTH_SHORT).show();
                                        checkoutReferecne.child(FirebaseUtils.ORDER_INFO_BRANCH).setValue(null);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toasty.success(CartActivity.this, "Checkout failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
