package com.caterassist.app.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.caterassist.app.BuildConfig;
import com.caterassist.app.R;
import com.caterassist.app.adapters.CartAdapter;
import com.caterassist.app.dialogs.DialogOrderSuccess;
import com.caterassist.app.dialogs.LoadingDialog;
import com.caterassist.app.models.CartItem;
import com.caterassist.app.models.Order;
import com.caterassist.app.models.OrderDetails;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.utils.AppUtils;
import com.caterassist.app.utils.Constants;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class CartActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CartActivity";
    private DatabaseReference cartItemsReference;
    private ChildEventListener cartItemsEventListener;
    private ArrayList<CartItem> cartItemsArrayList;
    private ArrayList<CartItem> cartItemsListToMail;
    private LinearLayoutManager cartItemsLayoutManager;
    private CartAdapter cartItemsAdapter;
    private UserDetails vendorDetails;
    private RecyclerView cartItemsRecyclerView;
    private LinearLayout checkoutButton;
    private LinearLayout clearCartButton;
    private Button dashboardButton;
    private LinearLayout includeView;
    private RelativeLayout cartInfoParent;
    private LoadingDialog loadingDialog;
    private Handler handler;
    private Runnable runnable;
    private LinearLayout bottomButtons;
    private TextView vendorNameTextView, totalAmountTextView, noOfItemTextView, extraNotesTxtView;


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
                if (vendorDetails != null) {
                    vendorNameTextView.setText(vendorDetails.getUserName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
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
                checkCartEmpty();
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
                        checkCartEmpty();
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
                Toasty.error(CartActivity.this, "Failed to load cart items.",
                        Toast.LENGTH_SHORT).show();
                checkCartEmpty();
            }
        };
        cartItemsReference.addChildEventListener(cartItemsEventListener);
        cartItemsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (loadingDialog != null) {
                    if (loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                }
                checkCartEmpty();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (loadingDialog != null) {
                    if (loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                }
                checkCartEmpty();
            }
        });
        cartItemsAdapter = new CartAdapter();
        cartItemsAdapter.setCartItemsArrayList(cartItemsArrayList);
        cartItemsAdapter.setActivity(this);
        cartItemsLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        cartItemsRecyclerView.setLayoutManager(cartItemsLayoutManager);
        cartItemsRecyclerView.setAdapter(cartItemsAdapter);
        cartItemsRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
    }

    private void checkCartEmpty() {
        int cartSize = cartItemsArrayList.size();
        noOfItemTextView.setText(String.valueOf(cartSize));
        double totalAmount = 0;
        for (CartItem item :
                cartItemsArrayList) {
            totalAmount += item.getTotalAmount();
        }
        String amount = "₹" + String.valueOf(totalAmount);
        totalAmountTextView.setText(amount);
        if (cartSize == 0) {
            includeView.setVisibility(View.VISIBLE);
            cartItemsRecyclerView.setVisibility(View.GONE);
            cartInfoParent.setVisibility(View.GONE);
            bottomButtons.setVisibility(View.GONE);
        } else {
            includeView.setVisibility(View.GONE);
            cartInfoParent.setVisibility(View.VISIBLE);
            bottomButtons.setVisibility(View.VISIBLE);
            cartItemsRecyclerView.setVisibility(View.VISIBLE);
        }
    }


    private void initViews() {
        includeView = findViewById(R.id.include_cart_empty);
        includeView.setVisibility(View.GONE);
        cartInfoParent = findViewById(R.id.cart_order_info_parent);
        bottomButtons = findViewById(R.id.cart_bottom_buttons);
        bottomButtons.setVisibility(View.GONE);
        cartInfoParent.setVisibility(View.GONE);
        cartItemsRecyclerView = findViewById(R.id.act_cart_recyc_cart_items);
        checkoutButton = findViewById(R.id.act_cart_btn_checkout);
        clearCartButton = findViewById(R.id.act_cart_btn_clear_cart);
        dashboardButton = findViewById(R.id.cart_go_to_dash);

        vendorNameTextView = findViewById(R.id.li_cart_vend_name);
        totalAmountTextView = findViewById(R.id.li_cart_total);
        noOfItemTextView = findViewById(R.id.li_no_of_items);
        extraNotesTxtView = findViewById(R.id.act_cart_extra_notes);

        cartItemsArrayList = new ArrayList<>();
        checkoutButton.setOnClickListener(this);
        clearCartButton.setOnClickListener(this);
        dashboardButton.setOnClickListener(this);

        loadingDialog = new LoadingDialog(this, "Loading cart items...");
        loadingDialog.show();
        handler = new Handler();
        runnable = () -> {
            if (loadingDialog != null)
                if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                    Toasty.error(CartActivity.this, "Please check your internet connection and try again!", Toast.LENGTH_SHORT).show();
                    checkCartEmpty();
                }
        };
        handler.postAtTime(runnable, System.currentTimeMillis() + Constants.UtilConstants.LOADING_TIMEOUT);
        handler.postDelayed(runnable, Constants.UtilConstants.LOADING_TIMEOUT);
    }

    @Override
    public void onBackPressed() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        if (cartItemsEventListener != null) {
            cartItemsReference.removeEventListener(cartItemsEventListener);
        }
        super.onBackPressed();

    }

    private void showDialogOrderSuccess(Order order) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DialogOrderSuccess newFragment = new DialogOrderSuccess();
        newFragment.setOrderDetails(order.getOrderInfo());
        newFragment.setItemCount(order.getOrderItems().size());
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.act_cart_btn_checkout:
                if (vendorDetails != null && cartItemsArrayList.size() > 0) {

                    checkout();
                }
                break;
            case R.id.act_cart_btn_clear_cart:
                if (cartItemsReference != null && cartItemsArrayList.size() > 0) {
                    clearCart();
                }
                break;
            case R.id.cart_go_to_dash:
                startActivity(new Intent(CartActivity.this, CatererHomeActivity.class));
                finish();
                break;
        }
    }

    private void clearCart() {

        androidx.appcompat.app.AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new androidx.appcompat.app.AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth);
        } else {
            builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        }
        builder.setTitle(getResources().getString(R.string.dialog_title_clear_cart))
                .setMessage(
                        getResources().getString(R.string.dialog_message_clear_cart))
                .setPositiveButton(
                        getResources().getString(R.string.dialog_btn_yes),
                        (dialog, which) -> {
                            if (cartItemsReference.getParent() != null) {
                                (cartItemsReference.getParent()).setValue(null)
                                        .addOnSuccessListener(aVoid -> Toasty.success(CartActivity.this,
                                                getString(R.string.toast_cart_cleared),
                                                Toast.LENGTH_SHORT)
                                                .show())
                                        .addOnFailureListener(e -> Toasty.error(CartActivity.this,
                                                getString(R.string.toast_cannot_clear_cart),
                                                Toast.LENGTH_SHORT)
                                                .show());
                            }

                        })
                .setNegativeButton(
                        getResources().getString(R.string.dialog_btn_no),
                        (dialog, which) -> dialog.dismiss()).show();
    }

    private void checkout() {
        androidx.appcompat.app.AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new androidx.appcompat.app.AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(getResources().getString(R.string.dialog_title_place_order))
                .setMessage(
                        getResources().getString(R.string.dialog_message_place_order))
                .setPositiveButton(
                        getResources().getString(R.string.dialog_btn_yes),
                        (dialog, which) -> {
                            double orderTotalAmt = 0.0;
                            for (CartItem cartItem : cartItemsArrayList) {
                                orderTotalAmt += cartItem.getTotalAmount();
                            }
                            final OrderDetails orderDetails = new OrderDetails();
                            UserDetails catererDetails = AppUtils.getUserInfoSharedPreferences(this);
                            orderDetails.setVendorId(vendorDetails.getUserID());
                            orderDetails.setCatererID(FirebaseAuth.getInstance().getUid());
                            orderDetails.setOrderStatus(0);
                            orderDetails.setExtraNotes(extraNotesTxtView.getText().toString());
                            orderDetails.setVendorName(vendorDetails.getUserName());
                            orderDetails.setVendorPhone(vendorDetails.getUserPhone());
                            orderDetails.setVendorEmail(vendorDetails.getUserEmail());

                            orderDetails.setOrderTotalAmount(orderTotalAmt);
                            orderDetails.setCatererName(catererDetails.getUserName());
                            orderDetails.setCatererEmail(catererDetails.getUserEmail());
                            orderDetails.setCatererPhone(catererDetails.getUserPhone());
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                            Date date = new Date();
                            orderDetails.setOrderTime(formatter.format(date));

                            String userOrdersItemsDatabasePath = FirebaseUtils.getDatabaseMainBranchName() +
                                    FirebaseUtils.CATERER_PENDING_ORDERS +
                                    FirebaseAuth.getInstance().getUid();
                            DatabaseReference checkoutReference = FirebaseDatabase.getInstance().getReference(userOrdersItemsDatabasePath);
                            cartItemsListToMail = new ArrayList<>();

                            Iterator<CartItem> iterator = cartItemsArrayList.iterator();

                            while (iterator.hasNext()) {
                                //Add the object clones
                                cartItemsListToMail.add((CartItem) iterator.next().clone());
                            }
                            Order order = new Order();
                            order.setOrderItems(cartItemsListToMail);
                            order.setOrderInfo(orderDetails);
                            checkoutReference = checkoutReference.push();

                            order.getOrderInfo().setOrderId(checkoutReference.getKey());
                            DatabaseReference finalCheckoutReference = checkoutReference;
                            finalCheckoutReference.setValue(order)
                                    .addOnSuccessListener(aVoid -> {

                                                sendEmail(order);
                                                Objects.requireNonNull(cartItemsReference.getParent()).setValue(null)
                                                        .addOnSuccessListener(aVoid1 -> {
                                                            Toasty.success(CartActivity.this,
                                                                    getString(R.string.toast_checkout_success),
                                                                    Toast.LENGTH_SHORT).show();
                                                            showDialogOrderSuccess(order);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toasty.success(CartActivity.this,
                                                                    getString(R.string.toast_checkout_failed),
                                                                    Toast.LENGTH_SHORT).show();
                                                            finalCheckoutReference.child(FirebaseUtils.ORDER_INFO_BRANCH).setValue(null);
                                                        });
                                            }
                                    )
                                    .addOnFailureListener(e -> Toasty.success(CartActivity.this,
                                            getString(R.string.toast_checkout_failed),
                                            Toast.LENGTH_SHORT).show());
                        })
                .setNegativeButton(
                        getResources().getString(R.string.dialog_btn_no),
                        (dialog, which) -> dialog.dismiss()).show();

    }

    private void sendEmail(Order order) {


        Thread thread = new Thread(() -> {
            try {
                OrderDetails orderDetails = order.getOrderInfo();
                OkHttpClient client = new OkHttpClient();
                String content = "{\"tags\":[\"Test\"]," +
                        "\"sender\":" +
                        "{\"name\":\"Cater Assistant\",\"email\":\"caterassistant@gmail.com\"}," +
                        "\"replyTo\":" +
                        "{\"email\":\"" + orderDetails.getCatererEmail() + "\",\"name\":\"" + orderDetails.getCatererName() + "\"}," +
                        "\"subject\":\"New Order from " + orderDetails.getCatererName() + "\"," +
                        "\"to\":[{\"email\":\"" + orderDetails.getVendorEmail() + "\",\"name\":\"" + orderDetails.getVendorName() + "\"}]," +
                        "\"htmlContent\":\"" + createHTML(order.getOrderItems(), orderDetails) + "\"}";
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, content);
                Request request = new Request.Builder()
                        .url("https://api.sendinblue.com/v3/smtp/email")
                        .addHeader("api-key", BuildConfig.SendInBlueAPIKEY)
                        .post(body)
                        .build();
                Log.e(TAG, "sendEmail: Sending");
                Log.e(TAG, "email To: " + orderDetails.getVendorEmail());
                Log.e(TAG, "email Reply To: " + orderDetails.getCatererEmail());
                Response response = client.newCall(request).execute();
                Log.e(TAG, "responseMessage:" + response.message());
                Log.e(TAG, "response:" + response.code());
                response.body().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private String createHTML(ArrayList<CartItem> orderItems, OrderDetails orderDetails) {
        UserDetails catererDetails = AppUtils.getUserInfoSharedPreferences(this);
        StringBuilder body = new StringBuilder();
        for (CartItem item : orderItems) {

            body.append("<tr style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\' valign=\'top\'>");

            body.append("<td style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; border-color: #DDD; width: 26%;\' width=\'26%\' valign=\'top\' align=\'left\'><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>");
            body.append(item.getId());
            body.append("</span>");
            body.append("</td>");

            body.append("<td style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; border-color: #DDD; width: 26%;\' width=\'26%\' valign=\'top\' align=\'left\'><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>");
            body.append(item.getName());
            body.append("</span>");
            body.append("</td>");

            body.append("<td style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; border-radius: 0.25em; border-style: solid; border-color: #DDD; text-align: right; width: 12%;\' width=\'12%\' valign=\'top\' align=\'right\'><span data-prefix=\'\' style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>₹</span><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>");
            body.append(item.getRate());
            body.append("</span>");
            body.append("</td>");

            body.append("<td style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; border-color: #DDD; width: 26%;\' width=\'26%\' valign=\'top\' align=\'left\'><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>");
            body.append(item.getQuantity());
            body.append("</span>");
            body.append("</td>");

            body.append("<td style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; border-radius: 0.25em; border-style: solid; border-color: #DDD; text-align: right; width: 12%;\' width=\'12%\' valign=\'top\' align=\'right\'><span data-prefix=\'\' style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>₹</span><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>");
            body.append(item.getTotalAmount());
            body.append("</span>");
            body.append("</td>");

            body.append("</tr>");
            Log.e(TAG, "items: body");
        }

        String html = "<body style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; text-decoration: none; vertical-align: top; margin: 0 auto; overflow: hidden; padding: 4%;\'>" +
                "    <header style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; margin: 0 0 3em;\'>" +
                "        <h1 style=\'border: 0; box-sizing: content-box; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; text-decoration: none; vertical-align: top; font: bold 100% sans-serif; letter-spacing: 0.5em; text-transform: uppercase; background: darkgrey; border-radius: 0.25em; color: #FFF; margin: 0 0 1em; text-align: center; padding: 0.5em 0.5em;\'>Order Invoice</h1>" +
                "        <address style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-weight: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; font-size: 75%; font-style: normal; text-align: right; line-height: 1.25; margin: 0 1em 1em 0;\'>" +
                "            <p style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; margin: 0 0 0.25em;\'>Cater Bazar</p>" +
                "            <p style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; margin: 0 0 0.25em;\'>Ernakulam<br style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>Kerala, India</p>" +
                "            <p style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; margin: 0 0 0.25em;\'>(91) 555-1234</p>" +
                "        </address>" +
                "        <span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; display: block; float: right; margin: 0 0 1em 1em; max-height: 25%; max-width: 60%; position: relative;\'>" +
                "            <h1 style=\'border: 0; box-sizing: content-box; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; text-decoration: none; vertical-align: top; font: bold 100% sans-serif; letter-spacing: 0.5em; text-transform: uppercase; background: darkgrey; border-radius: 0.25em; color: #FFF; margin: 0 0 1em; text-align: center; padding: 0.5em 0.5em;\'>" + orderDetails.getVendorName() + "</h1>" +
                "        </span>" +
                "    </header>" +
                "" +
                "    <article style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; margin: 0 0 3em;\'>" +
                "        <h2 style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; vertical-align: top; font: bold 100% sans-serif; letter-spacing: 0.5em; text-decoration: black; text-transform: uppercase;\'>" + catererDetails.getUserName() + "</h2>" +
                "        <p style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>" + catererDetails.getUserLocationName() + "</p>" +
                "        <table class=\'details\' style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; font-size: 75%; table-layout: fixed; border-collapse: separate; border-spacing: 2px; margin: 0 0 3em; float: left; width: 40%;\' width=\'40%\' valign=\'top\'>" +
                "            <tr style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\' valign=\'top\'>" +
                "                <th style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB;\' valign=\'top\' align=\'left\'><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>Mobile</span></th>" +
                "                <td style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; border-color: #DDD; width: 60%;\' width=\'60%\' valign=\'top\' align=\'left\'><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>" + catererDetails.getUserPhone() + "</span></td>" +
                "            </tr>" +
                "            <tr style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\' valign=\'top\'>" +
                "                <th style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB;\' valign=\'top\' align=\'left\'><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>Email</span></th>" +
                "                <td style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; border-color: #DDD; width: 60%;\' width=\'60%\' valign=\'top\' align=\'left\'><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>" + catererDetails.getUserEmail() + "</span></td>" +
                "            </tr>" +
                "        </table>" +
                "        <table class=\'meta\' style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; font-size: 75%; table-layout: fixed; border-collapse: separate; border-spacing: 2px; margin: 0 0 3em; float: right; width: 36%;\' width=\'36%\' valign=\'top\'>" +
                "            <tr style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\' valign=\'top\'>" +
                "                <th style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB; width: 40%;\' valign=\'top\' align=\'left\'><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>Invoice #</span></th>" +
                "                <td style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; border-color: #DDD; width: 60%;\' width=\'60%\' valign=\'top\' align=\'left\'><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>" + orderDetails.getOrderId() + "</span></td>" +
                "            </tr>" +
                "            <tr style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\' valign=\'top\'>" +
                "                <th style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB; width: 40%;\' valign=\'top\' align=\'left\'><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>Date</span></th>" +
                "                <td style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; border-color: #DDD; width: 60%;\' width=\'60%\' valign=\'top\' align=\'left\'><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>" + orderDetails.getOrderTime() + "</span></td>" +
                "            </tr>" +
                "        </table>" +
                "        <table class=\'inventory\' style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; font-size: 75%; table-layout: fixed; width: 100%; border-collapse: separate; border-spacing: 2px; margin: 0 0 3em;\' width=\'100%\' valign=\'top\'>" +
                "            <thead style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\' valign=\'top\'>" +
                "                <tr style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\' valign=\'top\'>" +
                "                    <th style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB; font-weight: bold; text-align: center;\' valign=\'top\' align=\'center\'><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>Item ID</span></th>" +
                "                    <th style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB; font-weight: bold; text-align: center;\' valign=\'top\' align=\'center\'><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>Item Name</span></th>" +
                "                    <th style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB; font-weight: bold; text-align: center;\' valign=\'top\' align=\'center\'><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>Rate</span></th>" +
                "                    <th style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB; font-weight: bold; text-align: center;\' valign=\'top\' align=\'center\'><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>Quantity</span></th>" +
                "                    <th style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB; font-weight: bold; text-align: center;\' valign=\'top\' align=\'center\'><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>Price</span></th>" +
                "                </tr>" +
                "            </thead>" +
                "            <tbody style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\' valign=\'top\'>" +
                body +
                "            </tbody>" +
                "        </table>" +
                "        <table class=\'balance\' style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top; font-size: 75%; table-layout: fixed; border-collapse: separate; border-spacing: 2px; float: right; width: 36%;\' width=\'36%\' valign=\'top\'>" +
                "            <tr style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\' valign=\'top\'>" +
                "                <th style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB; width: 50%;\' valign=\'top\' align=\'left\'><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>No. of Items</span></th>" +
                "                <td style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; border-radius: 0.25em; border-style: solid; border-color: #DDD; width: 50%; text-align: right;\' width=\'50%\' valign=\'top\' align=\'right\'><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>" + orderItems.size() + "</span></td>" +
                "            </tr>" +
                "            <tr style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\' valign=\'top\'>" +
                "                <th style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB; width: 50%;\' valign=\'top\' align=\'left\'><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>Total</span></th>" +
                "                <td style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; border-radius: 0.25em; border-style: solid; border-color: #DDD; width: 50%; text-align: right;\' width=\'50%\' valign=\'top\' align=\'right\'><span data-prefix=\'\' style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>₹</span><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>" +
                orderDetails.getOrderTotalAmount() + "</span></td>" +
                "            </tr>" +
                "        </table>" +
                "    </article>" +
                "        <aside style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>" +
                "            <h1 style=\'box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; font: bold 100% sans-serif; letter-spacing: 0.5em; text-align: center; text-transform: uppercase; border: none; border-width: 0 0 1px; margin: 0 0 1em; border-color: #999; border-bottom-style: solid;\'><span style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>Additional Notes</span></h1>" +
                "            <div style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>" +
                "                <p style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>This is a automated invoice generated by Cater Bazar. Please do not reply to this mail. <br style=\'border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\'>www.caterbazar.com</p>" +
                "            </div>" +
                "        </aside>" +
                "</body>";
        return html;
    }
}
