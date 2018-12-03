package com.caterassist.app.fragments;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.adapters.FavouriteVendorsAdapter;
import com.caterassist.app.adapters.VendorListAdapter;
import com.caterassist.app.models.FavouriteVendor;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A simple {@link Fragment} subclass.
 */
public class CatererDashboardFragment extends Fragment {

    private static final String TAG = "CatererDashboardFrag";

    private DatabaseReference favouriteVendorsReference;
    private ChildEventListener favouriteVendorsEventListener;
    private RecyclerView favouriteVendorsRecyclerView;
    ArrayList<UserDetails> allVendorsArrayList;
    private LinearLayoutManager favouriteVendorsLayoutManager;
    private FavouriteVendorsAdapter favouriteVendorsAdapter;
    private ArrayList<UserDetails> favouriteVendorArrayList;
    private DatabaseReference allVendorsRef;
    private LinearLayoutManager allVendorsLayoutManager;
    private VendorListAdapter allVendorsAdapter;
    private RecyclerView allVendorsRecyclerView;


    private View fragmentView;

    public CatererDashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_caterer_dashboard, container, false);
        //TODO: Remove the toast
        Toast.makeText(this.getContext(), "This is caterer fragment", Toast.LENGTH_SHORT).show();
        initViews();
        fetchFavouriteVendors();
        fetchAllVendors();
        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (favouriteVendorsEventListener != null) {
            favouriteVendorsReference.removeEventListener(favouriteVendorsEventListener);
        }
    }

    private void fetchAllVendors() {
        allVendorsArrayList = new ArrayList<>();
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.USER_INFO_BRANCH_NAME;
        allVendorsRef = FirebaseDatabase.getInstance().getReference(databasePath);
        allVendorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserDetails userDetails = snapshot.getValue(UserDetails.class);
                    if (userDetails.getIsVendor()) {
                        userDetails.setUserID(snapshot.getKey());
                        allVendorsArrayList.add(userDetails);
                    }
                }
                allVendorsAdapter = new VendorListAdapter();
                allVendorsAdapter.setVendorsList(allVendorsArrayList);
                allVendorsLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
                allVendorsRecyclerView.setLayoutManager(allVendorsLayoutManager);
                allVendorsRecyclerView.setAdapter(allVendorsAdapter);
                allVendorsRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                        DividerItemDecoration.VERTICAL));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchFavouriteVendors() {
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() +
                FirebaseUtils.FAVOURITE_VENDORS_BRANCH_NAME +
                FirebaseAuth.getInstance().getUid();
        favouriteVendorsReference = FirebaseDatabase.getInstance().getReference(databasePath);
        favouriteVendorsEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new favouriteVendor has been added, add it to the displayed list
                UserDetails favouriteVendor = dataSnapshot.getValue(UserDetails.class);
                favouriteVendor.setUserID(dataSnapshot.getKey());
                favouriteVendorArrayList.add(favouriteVendor);
                favouriteVendorsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A favouriteVendor has changed, use the key to determine if we are displaying this
                // favouriteVendor and if so displayed the changed favouriteVendor.
                UserDetails favouriteVendor = dataSnapshot.getValue(UserDetails.class);
                String favouriteVendorKey = dataSnapshot.getKey();
                for (int i = 0; i < favouriteVendorArrayList.size(); i++) {
                    if (favouriteVendorArrayList.get(i).getUserID().equals(favouriteVendorKey)) {
                        favouriteVendorArrayList.remove(i);
                        favouriteVendor.setUserID(dataSnapshot.getKey());
                        favouriteVendorArrayList.add(i, favouriteVendor);
                        favouriteVendorsAdapter.notifyDataSetChanged();
                        break;
                    }
                }
                // ...
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A favouriteVendor has changed, use the key to determine if we are displaying this
                // favouriteVendor and if so remove it.
                String favouriteVendorKey = dataSnapshot.getKey();
                for (int i = 0; i < favouriteVendorArrayList.size(); i++) {
                    if (favouriteVendorArrayList.get(i).getUserID().equals(favouriteVendorKey)) {
                        favouriteVendorArrayList.remove(i);
                        favouriteVendorsAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A favouriteVendor has changed position, use the key to determine if we are
                // displaying this favouriteVendor and if so move it.
                FavouriteVendor favouriteVendor = dataSnapshot.getValue(FavouriteVendor.class);
                String favouriteVendorKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(getContext(), "Failed to load favourite vendors.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        favouriteVendorsReference.addChildEventListener(favouriteVendorsEventListener);
        favouriteVendorsAdapter = new FavouriteVendorsAdapter();
        favouriteVendorsAdapter.setFavouriteVendorArrayList(favouriteVendorArrayList);
        favouriteVendorsLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        favouriteVendorsRecyclerView.setLayoutManager(favouriteVendorsLayoutManager);
        favouriteVendorsRecyclerView.setAdapter(favouriteVendorsAdapter);

    }

    private void initViews() {
        favouriteVendorsRecyclerView = fragmentView.findViewById(R.id.frag_cate_dash_fav_vendors);
        allVendorsRecyclerView = fragmentView.findViewById(R.id.frag_cate_all_vendors);
        favouriteVendorArrayList = new ArrayList<>();
    }

}
