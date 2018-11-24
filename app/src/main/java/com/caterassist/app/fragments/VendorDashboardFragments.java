package com.caterassist.app.fragments;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.adapters.VendingItemsAdapter;
import com.caterassist.app.models.VendorItem;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A simple {@link Fragment} subclass.
 */
public class VendorDashboardFragments extends Fragment {

    private static final String TAG = "VendorDashboard";

    private DatabaseReference vendingItemsReference;
    private ChildEventListener vendingItemsEventListener;
    private ArrayList<VendorItem> vendingItemsArrayList;
    private LinearLayoutManager vendingItemsLayoutManager;
    private VendingItemsAdapter vendingItemsAdapter;


    private RecyclerView vendingItemsRecyclerView;

    public VendorDashboardFragments() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vendor_dashboards, container, false);
        vendingItemsRecyclerView = view.findViewById(R.id.frag_vend_recyc_vending_items);
        Toast.makeText(getActivity(), "This is VendorFragment", Toast.LENGTH_SHORT).show();
        vendingItemsArrayList = new ArrayList<>();
        fetchItems();
        return view;
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
    }


}
