package com.caterassist.app.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.caterassist.app.R;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A simple {@link Fragment} subclass.
 */
public class CatererDashboardFragment extends Fragment {

    private RecyclerView favouriteVendorsRecyclerView;
    private RecyclerView ordersInProgress;
    private RecyclerView itemCategories;
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
        fetchPendingOrders();
        fetchFavouriteVendors();
        fetchItemCategories();
        return fragmentView;
    }

    private void fetchItemCategories() {

    }

    private void fetchFavouriteVendors() {

    }

    private void fetchPendingOrders() {
    }

    private void initViews() {
        favouriteVendorsRecyclerView = fragmentView.findViewById(R.id.frag_cate_dash_fav_vendors);
        ordersInProgress = fragmentView.findViewById(R.id.frag_cate_dash_orders_progress);
        itemCategories = fragmentView.findViewById(R.id.frag_cate_dash_item_categories);
    }

}
