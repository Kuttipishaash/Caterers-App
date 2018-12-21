package com.caterassist.app.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.activities.ProfileActivity;
import com.caterassist.app.activities.VendorNewOrdersActivity;
import com.caterassist.app.adapters.VendingItemsAdapter;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.models.VendorItem;
import com.caterassist.app.utils.AppUtils;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A simple {@link Fragment} subclass.
 */
public class VendorDashboardFragments extends Fragment implements View.OnClickListener {

    private static final String TAG = "VendorDashboard";

    private DatabaseReference vendingItemsReference;
    private ChildEventListener vendingItemsEventListener;
    private ArrayList<VendorItem> vendingItemsArrayList;
    private LinearLayoutManager vendingItemsLayoutManager;
    private VendingItemsAdapter vendingItemsAdapter;
    TextView awaitingOrderNumberTxtView;

    private RecyclerView vendingItemsRecyclerView;
    private Toolbar toolbar;
    private Integer approvalAwaitingOrders;
    private FloatingActionButton awaitingOrdersFab, viewProfileFab;
    private View parentView;

    public VendorDashboardFragments() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        UserDetails userDetails = AppUtils.getUserInfoSharedPreferences(getContext());
        String title = "Hi," + userDetails.getUserName();
        toolbar.setTitle(title);
        String subtitle = userDetails.getUserLocationName() + ", " + userDetails.getUserDistrictName();
        toolbar.setSubtitle(subtitle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        parentView = inflater.inflate(R.layout.fragment_vendor_dashboards, container, false);

        initViews();

        Toast.makeText(getActivity(), "This is VendorFragment", Toast.LENGTH_SHORT).show();
        vendingItemsArrayList = new ArrayList<>();
        UserDetails userDetails = AppUtils.getUserInfoSharedPreferences(getContext());
        String title = "Hi," + userDetails.getUserName();
        toolbar.setTitle(title);
        String subtitle = userDetails.getUserLocationName() + ", " + userDetails.getUserDistrictName();
        toolbar.setSubtitle(subtitle);
        fetchItems();
        fetchPendingOrders();
        return parentView;
    }

    private void initViews() {
        vendingItemsRecyclerView = parentView.findViewById(R.id.frag_vend_recyc_vending_items);
        toolbar = parentView.findViewById(R.id.vendor_dash_toolbar);
        awaitingOrderNumberTxtView = parentView.findViewById(R.id.frag_vend_dash_awaiting_orders);
        awaitingOrdersFab = parentView.findViewById(R.id.frag_vend_dash_awaiting_orders_fab);
        viewProfileFab = parentView.findViewById(R.id.vendor_view_profile);

        awaitingOrdersFab.setOnClickListener(this);
        viewProfileFab.setOnClickListener(this);
    }

    private void fetchPendingOrders() {
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.ORDERS_AWAITING_APPROVAL + FirebaseAuth.getInstance().getUid();
        DatabaseReference awaitingOrdersReference = FirebaseDatabase.getInstance().getReference(databasePath);
        awaitingOrdersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    approvalAwaitingOrders = dataSnapshot.getValue(Integer.class);
                    if (approvalAwaitingOrders.intValue() == 0) {
                        awaitingOrderNumberTxtView.setText("No Pending Orders");
                    } else {
                        String pendingText = approvalAwaitingOrders + " pending orders";
                        awaitingOrderNumberTxtView.setText(pendingText);
                    }
                } catch (NullPointerException e) {
                    approvalAwaitingOrders = 0;
                    awaitingOrderNumberTxtView.setText("No Pending Orders");
                    Log.e(TAG, "onDataChange: Approval awaiting order variable null in firebase");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchItems() {
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() +
                FirebaseUtils.VENDOR_LIST_BRANCH_NAME +
                FirebaseAuth.getInstance().getUid();

        vendingItemsReference = FirebaseDatabase.getInstance().getReference(databasePath);
        vendingItemsEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new VendorItem has been added, add it to the displayed list
                VendorItem cartItem = dataSnapshot.getValue(VendorItem.class);
                cartItem.setId(dataSnapshot.getKey());
                vendingItemsArrayList.add(cartItem);
                vendingItemsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A VendorItem has changed, use the key to determine if we are displaying this
                // VendorItem and if so displayed the changed VendorItem.
                VendorItem cartItem = dataSnapshot.getValue(VendorItem.class);
                String cartItemKey = dataSnapshot.getKey();
                for (int i = 0; i < vendingItemsArrayList.size(); i++) {
                    if (vendingItemsArrayList.get(i).getId().equals(cartItemKey)) {
                        vendingItemsArrayList.remove(i);
                        cartItem.setId(dataSnapshot.getKey());
                        vendingItemsArrayList.add(i, cartItem);
                        vendingItemsAdapter.notifyDataSetChanged();
                        break;
                    }
                }
                // ...
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A VendorItem has changed, use the key to determine if we are displaying this
                // VendorItem and if so remove it.
                String cartItemKey = dataSnapshot.getKey();
                for (int i = 0; i < vendingItemsArrayList.size(); i++) {
                    if (vendingItemsArrayList.get(i).getId().equals(cartItemKey)) {
                        vendingItemsArrayList.remove(i);
                        vendingItemsAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A VendorItem has changed position, use the key to determine if we are
                // displaying this VendorItem and if so move it.
                VendorItem cartItem = dataSnapshot.getValue(VendorItem.class);
                String cartItemKey = dataSnapshot.getKey();
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(getContext(), "Failed to load cart items.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        vendingItemsReference.addChildEventListener(vendingItemsEventListener);
        vendingItemsAdapter = new VendingItemsAdapter();
        vendingItemsAdapter.setVendingItemArrayList(vendingItemsArrayList);
        vendingItemsLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        vendingItemsRecyclerView.setLayoutManager(vendingItemsLayoutManager);
        vendingItemsRecyclerView.setAdapter(vendingItemsAdapter);
        vendingItemsRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.frag_vend_dash_awaiting_orders_fab) {
            startActivity(new Intent(getActivity(), VendorNewOrdersActivity.class));
        } else if (v.getId() == R.id.vendor_view_profile) {
            startActivity(new Intent(getActivity(), ProfileActivity.class));
        }
    }
}
