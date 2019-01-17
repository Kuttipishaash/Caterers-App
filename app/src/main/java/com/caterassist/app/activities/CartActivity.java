package com.caterassist.app.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.caterassist.app.BuildConfig;
import com.caterassist.app.R;
import com.caterassist.app.adapters.CartAdapter;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class CartActivity extends Activity implements View.OnClickListener {

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
        cartItemsRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
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
                if (vendorDetails != null && cartItemsArrayList.size() > 0) {
                    checkout();
                }
                break;
            case R.id.act_cart_btn_clear_cart:
                if (cartItemsReference != null) {
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

            body.append("<tr>");

            body.append("<td>");
            body.append("<span>");
            body.append(item.getId());
            body.append("</span>");
            body.append("</td>");

            body.append("<td>");
            body.append("<span>");
            body.append(item.getName());
            body.append("</span>");
            body.append("</td>");

            body.append("<td>");
            body.append("<span data-prefix>");
            body.append("₹");
            body.append("</span>");
            body.append("<span>");
            body.append(item.getRate());
            body.append("</span>");
            body.append("</td>");

            body.append("<td>");
            body.append("<span>");
            body.append(item.getQuantity());
            body.append("</span>");
            body.append("</td>");

            body.append("<td>");
            body.append("<span data-prefix>");
            body.append("₹");
            body.append("</span>");
            body.append("<span>");
            body.append(item.getTotalAmount());
            body.append("</span>");
            body.append("</td>");

            body.append("</tr>");
            Log.e(TAG, "items: body");
        }

        String html = "<body>" +
                "<style>" +
                "    * {" +
                "        border: 0;" +
                "        box-sizing: content-box;" +
                "        color: inherit;" +
                "        font-family: inherit;" +
                "        font-size: inherit;" +
                "        font-style: inherit;" +
                "        font-weight: inherit;" +
                "        line-height: inherit;" +
                "        list-style: none;" +
                "        margin: 0;" +
                "        padding: 0;" +
                "        text-decoration: none;" +
                "        vertical-align: top;" +
                "    }" +


                "    h1 {" +
                "        font: bold 100% sans-serif;" +
                "        letter-spacing: 0.5em;" +
                "        text-align: center;" +
                "        text-transform: uppercase;" +
                "    }" +

                "    /* table */" +

                "    table {" +
                "        font-size: 75%;" +
                "        table-layout: fixed;" +
                "        width: 100%;" +
                "    }" +

                "    table {" +
                "        border-collapse: separate;" +
                "        border-spacing: 2px;" +
                "    }" +

                "    th," +
                "    td {" +
                "        border-width: 1px;" +
                "        padding: 0.5em;" +
                "        position: relative;" +
                "        text-align: left;" +
                "    }" +

                "    th," +
                "    td {" +
                "        border-radius: 0.25em;" +
                "        border-style: solid;" +
                "    }" +

                "    th {" +
                "        background: #EEE;" +
                "        border-color: #BBB;" +
                "    }" +

                "    td {" +
                "        border-color: #DDD;" +
                "    }" +

                "    /* page */" +

                "    html {" +
                "        font: 16px/1 'Open Sans', sans-serif;" +
                "        overflow: auto;" +
                "        padding: 0.5in;" +
                "    }" +


                "    body {" +
                "        margin: 0 auto;" +
                "        overflow: hidden;" +
                "        padding: 0.5in;" +
                "    }" +


                "    /* header */" +

                "    header {" +
                "        margin: 0 0 3em;" +
                "    }" +

                "    header:after {" +
                "        clear: both;" +
                "        content: \'\';" +
                "        display: table;" +
                "    }" +

                "    header h1 {" +
                "        background: #000;" +
                "        border-radius: 0.25em;" +
                "        color: #FFF;" +
                "        margin: 0 0 1em;" +
                "        padding: 0.5em 0;" +
                "    }" +

                "    header address {" +
                "        float: left;" +
                "        font-size: 75%;" +
                "        font-style: normal;" +
                "        line-height: 1.25;" +
                "        margin: 0 1em 1em 0;" +
                "    }" +

                "    header address p {" +
                "        margin: 0 0 0.25em;" +
                "    }" +

                "    header span," +
                "    header img {" +
                "        display: block;" +
                "        float: right;" +
                "    }" +

                "    header span {" +
                "        margin: 0 0 1em 1em;" +
                "        max-height: 25%;" +
                "        max-width: 60%;" +
                "        position: relative;" +
                "    }" +

                "    header img {" +
                "        max-height: 100%;" +
                "        max-width: 100%;" +
                "    }" +

                "    /* article */" +

                "    article," +
                "    article address," +
                "    table.meta," +
                "    table.inventory {" +
                "        margin: 0 0 3em;" +
                "    }" +

                "    article:after {" +
                "        clear: both;" +
                "        content: \'\';" +
                "        display: table;" +
                "    }" +

                "    article h1 {" +
                "        clip: rect(0 0 0 0);" +
                "        position: absolute;" +
                "    }" +

                "    article address {" +
                "        float: left;" +
                "        font-size: 125%;" +
                "        font-weight: bold;" +
                "    }" +

                "    /* table meta & balance */" +

                "    table.meta," +
                "    table.balance {" +
                "        float: right;" +
                "        width: 36%;" +
                "    }" +

                "    table.meta:after," +
                "    table.balance:after {" +
                "        clear: both;" +
                "        content: \'\';" +
                "        display: table;" +
                "    }" +

                "    /* table meta */" +

                "    table.meta th {" +
                "        width: 40%;" +
                "    }" +

                "    table.meta td {" +
                "        width: 60%;" +
                "    }" +

                "    /* table items */" +

                "    table.inventory {" +
                "        clear: both;" +
                "        width: 100%;" +
                "    }" +

                "    table.inventory th {" +
                "        font-weight: bold;" +
                "        text-align: center;" +
                "    }" +

                "    table.inventory td:nth-child(1) {" +
                "        width: 26%;" +
                "    }" +

                "    table.inventory td:nth-child(2) {" +
                "        width: 38%;" +
                "    }" +

                "    table.inventory td:nth-child(3) {" +
                "        text-align: right;" +
                "        width: 12%;" +
                "    }" +

                "    table.inventory td:nth-child(4) {" +
                "        text-align: right;" +
                "        width: 12%;" +
                "    }" +

                "    table.inventory td:nth-child(5) {" +
                "        text-align: right;" +
                "        width: 12%;" +
                "    }" +

                "    /* table balance */" +

                "    table.balance th," +
                "    table.balance td {" +
                "        width: 50%;" +
                "    }" +

                "    table.balance td {" +
                "        text-align: right;" +
                "    }" +

                "    /* aside */" +

                "    aside h1 {" +
                "        border: none;" +
                "        border-width: 0 0 1px;" +
                "        margin: 0 0 1em;" +
                "    }" +
                "    aside h1 {" +
                "        border-color: #999;" +
                "        border-bottom-style: solid;" +
                "    }" +
                "</style>" +
                "    <header>" +
                "        <h1>Order Invoice</h1>" +
                "        <address >" +
                "            <p>" + catererDetails.getUserName() + "</p>" +
                "            <p>" + catererDetails.getUserLocationName() + "<br>Kerala, India</p>" +
                "            <p>" + catererDetails.getUserPhone() + "</p>" +
                "        </address>" +
                "        <span>" +
                "            <h1>Cater Bazar</h1>" +
                "        </span>" +
                "    </header>" +
                "    <article>" +
                "        <h1>Recipient</h1>" +
                "        <address >" +
                "            <p>" + orderDetails.getCatererName() + "</p>" +
                "        </address>" +
                "        <table class=\'meta\'>" +
                "            <tr>" +
                "                <th><span >Invoice #</span></th>" +
                "                <td><span >" + orderDetails.getOrderId() + "</span></td>" +
                "            </tr>" +
                "            <tr>" +
                "                <th><span >Date</span></th>" +
                "                <td><span >" + orderDetails.getOrderTime() + "</span></td>" +
                "            </tr>" +
                "        </table>" +
                "        <table class=\'inventory\'>" +
                "            <thead>" +
                "                <tr>" +
                "                    <th><span >Item ID</span></th>" +
                "                    <th><span >Item Name</span></th>" +
                "                    <th><span >Rate</span></th>" +
                "                    <th><span >Quantity</span></th>" +
                "                    <th><span >Price</span></th>" +
                "                </tr>" +
                "            </thead>" +
                "            <tbody>" +
                body +
                "            </tbody>" +
                "        </table>" +
                "        <table class=\'balance\'>" +
                "                <tr>" +
                "                        <th><span >No. of Items</span></th>" +
                "                        <td><span>" + orderItems.size() + "</span></td>" +
                "                    </tr>" +
                "                <tr>" +
                "                    <th><span >Total</span></th>" +
                "                    <td><span data-prefix>₹</span><span>" + orderDetails.getOrderTotalAmount() + "</span></td>" +
                "                </tr>" +
                "            </table>" +
                "    </article>" +
                "    <aside>" +
                "        <h1><span >Additional Notes</span></h1>" +
                "        <div >" +
                "            <p>This is a automated mail generated by Cater Bazar. Please do not reply to this mail.</p>" +
                "        </div>" +
                "    </aside>" +
                "</body>";
        return html;
    }
}
