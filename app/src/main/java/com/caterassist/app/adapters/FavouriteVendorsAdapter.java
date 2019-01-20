package com.caterassist.app.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class FavouriteVendorsAdapter extends RecyclerView.Adapter<FavouriteVendorsAdapter.ViewHolder> {
    private ArrayList<UserDetails> favouriteVendorArrayList;
    private Activity activity;

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setFavouriteVendorArrayList(ArrayList<UserDetails> favouriteVendorArrayList) {
        this.favouriteVendorArrayList = favouriteVendorArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_favourite_vendor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserDetails favouriteVendor = favouriteVendorArrayList.get(position);
        holder.vendorName.setText(favouriteVendor.getUserName());
        String imageUrl = favouriteVendor.getUserImageUrl();
        if (imageUrl != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child(imageUrl).getDownloadUrl().addOnSuccessListener(uri -> {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.drawable.placeholder);
                requestOptions.error(R.drawable.ic_error_placeholder);

                Glide.with(activity.getApplicationContext())
                        .setDefaultRequestOptions(requestOptions)
                        .load(uri)
                        .into(holder.vendorImage);
            }).addOnFailureListener(exception -> holder.vendorImage.setImageResource(R.drawable.ic_error_placeholder));
        }
    }

    @Override
    public int getItemCount() {
        return favouriteVendorArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CardView parentView;
        private TextView vendorName;
        private ImageButton emailVendorButton;
        private ImageButton callVendorButton;
        private ImageView removeVendorButton;
        private ImageView vendorImage;
        private Context context;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            parentView = itemView.findViewById(R.id.list_item_favourite_vendor);
            vendorImage = itemView.findViewById(R.id.li_item_fav_vendor_image);
            vendorName = itemView.findViewById(R.id.li_item_fav_vendor_name);
            emailVendorButton = itemView.findViewById(R.id.li_item_fav_vendor_mail);
            callVendorButton = itemView.findViewById(R.id.li_item_fav_vendor_call);
            removeVendorButton = itemView.findViewById(R.id.li_item_fav_vendor_remove);

            emailVendorButton.setOnClickListener(this);
            callVendorButton.setOnClickListener(this);
            removeVendorButton.setOnClickListener(this);
            parentView.setOnClickListener(this);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            switch (v.getId()) {
                case R.id.li_item_fav_vendor_mail:
                    String emailAddress = favouriteVendorArrayList.get(position).getUserEmail();
                    Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                    sendIntent.setData(Uri.parse("mailto:"));
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Enquiry about vending services.");
                    sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "");
                    itemView.getContext().startActivity(sendIntent);
                    break;
                case R.id.li_item_fav_vendor_call:
                    String phoneNumber = favouriteVendorArrayList.get(position).getUserPhone();
                    Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                    itemView.getContext().startActivity(callIntent);
                    break;
                case R.id.list_item_favourite_vendor:
                    String uID = favouriteVendorArrayList.get(position).getUserID();
                    Intent viewVendorItemsIntent = new Intent(context, ViewVendorItemsActivity.class);
                    viewVendorItemsIntent.putExtra(Constants.IntentExtrasKeys.VIEW_VENDOR_ITEMS_INTENT_VENDOR_UID, uID);
                    context.startActivity(viewVendorItemsIntent);
                    break;
                case R.id.li_item_fav_vendor_remove:
                    removeVendorFromFavourites(favouriteVendorArrayList.get(position).getUserID());
                    break;
            }
        }

        private void removeVendorFromFavourites(String vendorUid) {
            String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.FAVOURITE_VENDORS_BRANCH_NAME
                    + FirebaseAuth.getInstance().getUid() + "/" + vendorUid;
            Toast.makeText(context, databasePath, Toast.LENGTH_SHORT).show();
            DatabaseReference favouriteVendorReference = FirebaseDatabase.getInstance().getReference(databasePath);
            favouriteVendorReference.setValue(null);
            Toast.makeText(context, "Remove clicked", Toast.LENGTH_SHORT).show();
        }
    }
}
