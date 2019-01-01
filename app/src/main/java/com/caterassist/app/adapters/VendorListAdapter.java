package com.caterassist.app.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caterassist.app.R;
import com.caterassist.app.activities.ViewVendorItemsActivity;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.utils.Constants;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class VendorListAdapter extends RecyclerView.Adapter<VendorListAdapter.ViewHolder> implements Filterable {
    private ArrayList<UserDetails> vendorsList;
    private ArrayList<UserDetails> filteredVendorsList;
    private VendorListAdapterListener vendorListAdapterListener;

    public void setVendorsList(ArrayList<UserDetails> vendorsList) {
        this.vendorsList = vendorsList;
        this.filteredVendorsList = vendorsList;
    }

    public void setVendorListAdapterListener(VendorListAdapterListener vendorListAdapterListener) {
        this.vendorListAdapterListener = vendorListAdapterListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_vendors, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserDetails vendorDetails = filteredVendorsList.get(position);
        holder.vendorNameTextView.setText(vendorDetails.getUserName());
        holder.vendorLocationTextView.setText(vendorDetails.getUserLocationName());
        String imageUrl = vendorDetails.getUserImageUrl();
        if (imageUrl != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child(imageUrl).getDownloadUrl().addOnSuccessListener(uri -> {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.drawable.placeholder);
                requestOptions.error(R.drawable.ic_error_placeholder);
                Glide.with(holder.itemView.getContext())
                        .setDefaultRequestOptions(requestOptions)
                        .load(uri)
                        .into(holder.vendorImageView);
            }).addOnFailureListener(exception -> holder.vendorImageView.setImageResource(R.drawable.ic_error_placeholder));
        }
    }

    @Override
    public int getItemCount() {
        return filteredVendorsList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    filteredVendorsList = vendorsList;
                } else {
                    ArrayList<UserDetails> filteredList = new ArrayList<>();
                    for (UserDetails vendor : vendorsList) {
                        if (vendor.getUserName().toLowerCase().contains(charString.toLowerCase()) || vendor.getUserLocationName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(vendor);
                        }
                    }
                    filteredVendorsList = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredVendorsList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredVendorsList = (ArrayList<UserDetails>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView vendorImageView;
        TextView vendorNameTextView;
        TextView vendorLocationTextView;
        ImageButton callVendorBtn;
        LinearLayout parentLayout;
        ImageButton addFavouriteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            vendorImageView = itemView.findViewById(R.id.li_vendors_image);
            vendorNameTextView = itemView.findViewById(R.id.li_vendor_name);
            vendorLocationTextView = itemView.findViewById(R.id.li_vendor_loc);
            parentLayout = itemView.findViewById(R.id.li_vendor_parent_layout);
            callVendorBtn = itemView.findViewById(R.id.li_vendor_call);
            addFavouriteButton = itemView.findViewById(R.id.li_vendor_add_favourite);
            callVendorBtn.setOnClickListener(this);
            parentLayout.setOnClickListener(this);
            addFavouriteButton.setOnClickListener(this);


        }

        @SuppressLint("MissingPermission")
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.li_vendor_parent_layout) {
                String uID = filteredVendorsList.get(getAdapterPosition()).getUserID();
                Intent viewVendorItemsIntent = new Intent(itemView.getContext(), ViewVendorItemsActivity.class);
                viewVendorItemsIntent.putExtra(Constants.IntentExtrasKeys.VIEW_VENDOR_ITEMS_INTENT_VENDOR_UID, uID);
                itemView.getContext().startActivity(viewVendorItemsIntent);
            } else if (v.getId() == R.id.li_vendor_call) {
                String phoneNumber = filteredVendorsList.get(getAdapterPosition()).getUserPhone();
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                itemView.getContext().startActivity(callIntent);
            } else if (v.getId() == R.id.li_vendor_add_favourite) {
                String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.FAVOURITE_VENDORS_BRANCH_NAME + FirebaseAuth.getInstance().getUid();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
                databaseReference.child(filteredVendorsList.get(getAdapterPosition()).getUserID()).setValue(filteredVendorsList.get(getAdapterPosition()));
            }
        }
    }

    public interface VendorListAdapterListener {
        void onVendorListItemSelected(UserDetails userDetails);
    }
}
