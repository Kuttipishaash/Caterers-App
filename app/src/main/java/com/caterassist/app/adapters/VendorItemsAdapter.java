package com.caterassist.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.caterassist.app.R;
import com.caterassist.app.dialogs.AddToCartDialog;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.models.VendorItem;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class VendorItemsAdapter extends RecyclerView.Adapter<VendorItemsAdapter.VendorItemsViewHolder> {
    private ArrayList<VendorItem> vendorItemArrayList;
    private UserDetails vendorDetails;
    private AppCompatActivity parentActivity;


    public void setParentActivity(AppCompatActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public void setVendorDetails(UserDetails vendorDetails) {
        this.vendorDetails = vendorDetails;
    }

    public void setVendorItemArrayList(ArrayList<VendorItem> vendorItemArrayList) {
        this.vendorItemArrayList = vendorItemArrayList;
    }

    @NonNull
    @Override
    public VendorItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_vendor_item, parent, false);
        return new VendorItemsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VendorItemsViewHolder holder, int position) {
        VendorItem vendorItem = vendorItemArrayList.get(position);
        holder.name.setText(vendorItem.getName());
        holder.category.setText(vendorItem.getCategory());
        holder.rate.setText(String.valueOf(vendorItem.getRatePerUnit()));
        String unitString = "₹/" + vendorItem.getUnit();
        holder.rateunit.setText(unitString);

        String itemInStock = vendorItem.getStock() + " " + vendorItem.getUnit();
        holder.qtyInStock.setText(itemInStock);
        String imageUrl = vendorItem.getImageUrl();
        if (imageUrl != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child(imageUrl).getDownloadUrl().addOnSuccessListener(uri -> {
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.ic_error_placeholder)
                        .override(150, 150);


                Glide.with(parentActivity.getApplicationContext())
                        .setDefaultRequestOptions(requestOptions)
                        .load(uri)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .apply(requestOptions)
                        .into(holder.image);
            }).addOnFailureListener(exception -> holder.image.setImageResource(R.drawable.ic_error_placeholder));
        }
    }

    @Override
    public int getItemCount() {
        return vendorItemArrayList.size();
    }

    class VendorItemsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView image;
        TextView name;
        TextView category;
        TextView rate;
        TextView rateunit;
        TextView qtyInStock;
        LinearLayout addToCart;

        public VendorItemsViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.li_item_vend_item_image);
            name = itemView.findViewById(R.id.li_item_vend_item_name);
            category = itemView.findViewById(R.id.li_item_vend_item_category);
            rate = itemView.findViewById(R.id.li_item_vend_item_rate);
            rateunit = itemView.findViewById(R.id.li_item_vend_item_rate_unit);
            qtyInStock = itemView.findViewById(R.id.li_item_vend_item_stock);
            addToCart = itemView.findViewById(R.id.li_item_vend_item_add_to_cart);

            addToCart.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.li_item_vend_item_add_to_cart:
                    String databasePath = FirebaseUtils.getDatabaseMainBranchName()
                            + FirebaseUtils.CART_BRANCH_NAME
                            + FirebaseAuth.getInstance().getUid() + "/"
                            + FirebaseUtils.CART_VENDOR_BRANCH;
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            UserDetails userDetails = dataSnapshot.getValue(UserDetails.class);
                            if (userDetails != null) {
                                if (userDetails.getUserID().equals(vendorDetails.getUserID())) {
                                    addItemToCart(vendorItemArrayList.get(getAdapterPosition()));
                                } else {
                                    Toasty.warning(itemView.getContext(),
                                            "You cannot add items from a different vendor to the cart.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                addItemToCart(vendorItemArrayList.get(getAdapterPosition()));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    break;
            }
        }

        private void addItemToCart(VendorItem vendorItem) {
            FragmentManager fragmentManager = parentActivity.getSupportFragmentManager();
            AddToCartDialog newFragment = new AddToCartDialog();
            newFragment.setValues(vendorItem, vendorDetails);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
        }
    }
}
