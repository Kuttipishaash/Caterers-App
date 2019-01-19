package com.caterassist.app.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.caterassist.app.BuildConfig;
import com.caterassist.app.DialogOrderSuccess;
import com.caterassist.app.R;
import com.caterassist.app.adapters.CartAdapter;
import com.caterassist.app.dialogs.LoadingDialog;
import com.caterassist.app.models.CartItem;
import com.caterassist.app.models.Order;
import com.caterassist.app.models.OrderDetails;
import com.caterassist.app.models.UserDetails;
import com.caterassist.app.utils.AppUtils;
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
    private LoadingDialog loadingDialog;
    private Handler handler;
    private Runnable runnable;

    private TextView vendorNameTextView, totalAmountTextView, noOfItemTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        initViews();
        fetchCartItems();
        setNoItemView();
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
        cartItemsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (loadingDialog != null || loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (loadingDialog != null || loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
            }
        });
        cartItemsAdapter = new CartAdapter();
        cartItemsAdapter.setCartItemsArrayList(cartItemsArrayList);
        cartItemsLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        cartItemsRecyclerView.setLayoutManager(cartItemsLayoutManager);
        cartItemsRecyclerView.setAdapter(cartItemsAdapter);
        cartItemsRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
    }

    private void initViews() {
        cartItemsRecyclerView = findViewById(R.id.act_cart_recyc_cart_items);
        checkoutButton = findViewById(R.id.act_cart_btn_checkout);
        clearCartButton = findViewById(R.id.act_cart_btn_clear_cart);

        vendorNameTextView = findViewById(R.id.li_cart_vend_name);
        totalAmountTextView = findViewById(R.id.li_cart_total);
        noOfItemTextView = findViewById(R.id.li_no_of_items);

        cartItemsArrayList = new ArrayList<>();
        checkoutButton.setOnClickListener(this);
        clearCartButton.setOnClickListener(this);

        loadingDialog = new LoadingDialog(this, "Loading cart items...");
        loadingDialog.show();
        final int interval = 10000; // 1 Second
        handler = new Handler();
        runnable = () -> {
            if (loadingDialog != null)
                if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                    Toast.makeText(CartActivity.this, "Please check your internet connection and try again!", Toast.LENGTH_SHORT).show();
                }
        };
        handler.postAtTime(runnable, System.currentTimeMillis() + interval);
        handler.postDelayed(runnable, interval);
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


    private void setNoItemView() {
        View includeView = findViewById(R.id.include_cart_empty);
        includeView.setVisibility(View.GONE);
        if (cartItemsReference == null && cartItemsArrayList.size() == 0) {
            includeView.setVisibility(View.VISIBLE);
        }
    }

    private void showDialogOrderSuccess() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DialogOrderSuccess newFragment = new DialogOrderSuccess();
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
        }
    }

    private void clearCart() {

        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.dialog_title_clear_cart))
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
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.dialog_title_place_order))
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
                            orderDetails.setVendorName(vendorDetails.getUserName());
                            orderDetails.setVendorPhone(vendorDetails.getUserPhone());
                            orderDetails.setVendorEmail(vendorDetails.getUserEmail());
                            orderDetails.setOrderTotalAmount(orderTotalAmt);
                            orderDetails.setCatererName(catererDetails.getUserName());
                            orderDetails.setCatererEmail(catererDetails.getUserEmail());
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                            Date date = new Date();
                            orderDetails.setOrderTime(formatter.format(date));

                            String userOrdersItemsDatabasePath = FirebaseUtils.getDatabaseMainBranchName() +
                                    FirebaseUtils.ORDERS_CATERER_BRANCH +
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
                                                            showDialogOrderSuccess();
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

            body.append("<tr style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\" valign=\"top\">");

            body.append("<td style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; border-color: #DDD; width: 26%;\" width=\"26%\" valign=\"top\" align=\"left\"><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">");
            body.append(item.getId());
            body.append("</span>");
            body.append("</td>");

            body.append("<td style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; border-color: #DDD; width: 26%;\" width=\"26%\" valign=\"top\" align=\"left\"><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">");
            body.append(item.getName());
            body.append("</span>");
            body.append("</td>");

            body.append("<td style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; border-radius: 0.25em; border-style: solid; border-color: #DDD; text-align: right; width: 12%;\" width=\"12%\" valign=\"top\" align=\"right\"><span data-prefix=\"\" style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">₹</span><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">");
            body.append(item.getRate());
            body.append("</span>");
            body.append("</td>");

            body.append("<td style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; border-color: #DDD; width: 26%;\" width=\"26%\" valign=\"top\" align=\"left\"><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">");
            body.append(item.getQuantity());
            body.append("</span>");
            body.append("</td>");

            body.append("<td style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; border-radius: 0.25em; border-style: solid; border-color: #DDD; text-align: right; width: 12%;\" width=\"12%\" valign=\"top\" align=\"right\"><span data-prefix=\"\" style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">₹</span><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">");
            body.append(item.getTotalAmount());
            body.append("</span>");
            body.append("</td>");

            body.append("</tr>");
            Log.e(TAG, "items: body");
        }

        String html = "\n" +
                "<body style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; text-decoration: none; vertical-align: top; margin: 0 auto; overflow: hidden; padding: 4%;\">\n" +
                "    <header style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; margin: 0 0 3em;\">\n" +
                "        <h1 style=\"border: 0; box-sizing: content-box; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; text-decoration: none; vertical-align: top; font: bold 100% sans-serif; letter-spacing: 0.5em; text-transform: uppercase; background: darkgrey; border-radius: 0.25em; color: #FFF; margin: 0 0 1em; text-align: center; padding: 0.5em 0.5em;\">Order Invoice</h1>\n" +
                "        <address style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-weight: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; font-size: 75%; font-style: normal; text-align: right; line-height: 1.25; margin: 0 1em 1em 0;\">\n" +
                "            <p style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; margin: 0 0 0.25em;\">Cater Bazar</p>\n" +
                "            <p style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; margin: 0 0 0.25em;\">Ernakulam<br style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">Kerala, India</p>\n" +
                "            <p style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; margin: 0 0 0.25em;\">(91) 555-1234</p>\n" +
                "        </address>\n" +
                "        <span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; display: block; float: right; margin: 0 0 1em 1em; max-height: 25%; max-width: 60%; position: relative;\">\n" +
                "            <h1 style=\"border: 0; box-sizing: content-box; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; text-decoration: none; vertical-align: top; font: bold 100% sans-serif; letter-spacing: 0.5em; text-transform: uppercase; background: darkgrey; border-radius: 0.25em; color: #FFF; margin: 0 0 1em; text-align: center; padding: 0.5em 0.5em;\">" + orderDetails.getCatererName() + "</h1>\n" +
                "        </span>\n" +
                "    </header>\n" +
                "\n" +
                "    <article style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; margin: 0 0 3em;\">\n" +
                "        <h2 style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; vertical-align: top; font: bold 100% sans-serif; letter-spacing: 0.5em; text-decoration: black; text-transform: uppercase;\">" + catererDetails.getUserName() + "</h2>\n" +
                "        <p style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">" + catererDetails.getUserLocationName() + "</p>\n" +
                "        <table class=\"details\" style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; font-size: 75%; table-layout: fixed; border-collapse: separate; border-spacing: 2px; margin: 0 0 3em; float: left; width: 40%;\" width=\"40%\" valign=\"top\">\n" +
                "            <tr style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\" valign=\"top\">\n" +
                "                <th style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB;\" valign=\"top\" align=\"left\"><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">Mobile</span></th>\n" +
                "                <td style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; border-color: #DDD; width: 60%;\" width=\"60%\" valign=\"top\" align=\"left\"><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">" + catererDetails.getUserPhone() + "</span></td>\n" +
                "            </tr>\n" +
                "            <tr style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\" valign=\"top\">\n" +
                "                <th style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB;\" valign=\"top\" align=\"left\"><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">Email</span></th>\n" +
                "                <td style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; border-color: #DDD; width: 60%;\" width=\"60%\" valign=\"top\" align=\"left\"><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">" + catererDetails.getUserEmail() + "</span></td>\n" +
                "            </tr>\n" +
                "        </table>\n" +
                "        <table class=\"meta\" style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; font-size: 75%; table-layout: fixed; border-collapse: separate; border-spacing: 2px; margin: 0 0 3em; float: right; width: 36%;\" width=\"36%\" valign=\"top\">\n" +
                "            <tr style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\" valign=\"top\">\n" +
                "                <th style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB; width: 40%;\" valign=\"top\" align=\"left\"><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">Invoice #</span></th>\n" +
                "                <td style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; border-color: #DDD; width: 60%;\" width=\"60%\" valign=\"top\" align=\"left\"><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">" + orderDetails.getOrderId() + "</span></td>\n" +
                "            </tr>\n" +
                "            <tr style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\" valign=\"top\">\n" +
                "                <th style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB; width: 40%;\" valign=\"top\" align=\"left\"><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">Date</span></th>\n" +
                "                <td style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; border-color: #DDD; width: 60%;\" width=\"60%\" valign=\"top\" align=\"left\"><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">" + orderDetails.getOrderTime() + "</span></td>\n" +
                "            </tr>\n" +
                "        </table>\n" +
                "        <table class=\"inventory\" style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; font-size: 75%; table-layout: fixed; width: 100%; border-collapse: separate; border-spacing: 2px; margin: 0 0 3em;\" width=\"100%\" valign=\"top\">\n" +
                "            <thead style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\" valign=\"top\">\n" +
                "                <tr style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\" valign=\"top\">\n" +
                "                    <th style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB; font-weight: bold; text-align: center;\" valign=\"top\" align=\"center\"><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">Item ID</span></th>\n" +
                "                    <th style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB; font-weight: bold; text-align: center;\" valign=\"top\" align=\"center\"><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">Item Name</span></th>\n" +
                "                    <th style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB; font-weight: bold; text-align: center;\" valign=\"top\" align=\"center\"><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">Rate</span></th>\n" +
                "                    <th style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB; font-weight: bold; text-align: center;\" valign=\"top\" align=\"center\"><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">Quantity</span></th>\n" +
                "                    <th style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB; font-weight: bold; text-align: center;\" valign=\"top\" align=\"center\"><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">Price</span></th>\n" +
                "                </tr>\n" +
                "            </thead>\n" +
                "            <tbody style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\" valign=\"top\">\n" +
                body +
                "            </tbody>\n" +
                "        </table>\n" +
                "        <table class=\"balance\" style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top; font-size: 75%; table-layout: fixed; border-collapse: separate; border-spacing: 2px; float: right; width: 36%;\" width=\"36%\" valign=\"top\">\n" +
                "            <tr style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\" valign=\"top\">\n" +
                "                <th style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB; width: 50%;\" valign=\"top\" align=\"left\"><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">No. of Items</span></th>\n" +
                "                <td style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; border-radius: 0.25em; border-style: solid; border-color: #DDD; width: 50%; text-align: right;\" width=\"50%\" valign=\"top\" align=\"right\"><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">" + orderItems.size() + "</span></td>\n" +
                "            </tr>\n" +
                "            <tr style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\" valign=\"top\">\n" +
                "                <th style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; text-align: left; border-radius: 0.25em; border-style: solid; background: #EEE; border-color: #BBB; width: 50%;\" valign=\"top\" align=\"left\"><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">Total</span></th>\n" +
                "                <td style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; text-decoration: none; vertical-align: top; border-width: 1px; padding: 0.5em; position: relative; border-radius: 0.25em; border-style: solid; border-color: #DDD; width: 50%; text-align: right;\" width=\"50%\" valign=\"top\" align=\"right\"><span data-prefix=\"\" style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">₹</span><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">" +
                orderDetails.getOrderTotalAmount() + "</span></td>\n" +
                "            </tr>\n" +
                "        </table>\n" +
                "    </article>\n" +
                "        <aside style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">\n" +
                "            <h1 style=\"box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; padding: 0; text-decoration: none; vertical-align: top; font: bold 100% sans-serif; letter-spacing: 0.5em; text-align: center; text-transform: uppercase; border: none; border-width: 0 0 1px; margin: 0 0 1em; border-color: #999; border-bottom-style: solid;\"><span style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">Additional Notes</span></h1>\n" +
                "            <div style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">\n" +
                "                <p style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">This is a automated invoice generated by Cater Bazar. Please do not reply to this mail. <br style=\"border: 0; box-sizing: content-box; color: inherit; font-family: inherit; font-size: inherit; font-style: inherit; font-weight: inherit; line-height: inherit; list-style: none; margin: 0; padding: 0; text-decoration: none; vertical-align: top;\">www.caterbazar.com</p>\n" +
                "            </div>\n" +
                "        </aside>\n" +
                "</body>";
        return html;
    }
}
