package com.caterassist.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.caterassist.app.R;
import com.caterassist.app.adapters.OrderItemsAdapter;
import com.caterassist.app.dialogs.LoadingDialog;
import com.caterassist.app.models.CartItem;
import com.caterassist.app.models.OrderDetails;
import com.caterassist.app.utils.AppUtils;
import com.caterassist.app.utils.Constants;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class OrderDetailsActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "OrderDetailsAct";

    private RecyclerView orderItemsRecyclerView;
    private TextView userTypeTxtView;
    private TextView userNameTxtView;
    private TextView orderDateTxtView;
    private TextView orderTimeTxtView;
    private TextView orderIDTxtView;
    private TextView orderTotalAmtTxtView;
    private TextView orderStatusTxtView;
    private TextView extraNotesTxtView;
    private LinearLayout noItemsView;
    private LinearLayout extraNotesLayout;
    private LinearLayout extraNotesEditLayout;
    private TextInputEditText extraNotesEditTxt;
    private Button extraNotesEditBtn;
    private LoadingDialog loadingDialog;
    private ImageButton deleteOrderBtn;
    private ImageButton viewVendorBtn;
    private Button dashboardLinkBtn;
    private TextView orderStatusUpdateBtn; //Button to accept, mark order as completed
    private TextView orderRejectBtn;    //Button to reject order

    private boolean isVendor;
    private ArrayList<CartItem> cartItemArrayList;
    private OrderItemsAdapter orderItemsAdapter;
    private String orderBranchName;
    private String orderId;
    private OrderDetails orderDetails;
    private Handler handler;
    private Runnable runnable;
    private LinearLayoutManager orderItemsLayoutManager;
    private Button extraNotesEditSubmitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        getDataFromIntent();
        if (orderBranchName != null && orderId != null) {
            initializeFields();
            setOrderInfo();
            fetchItems();
        } else {
            Toasty.error(this, "Some error occured! Try again...", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        orderBranchName = intent.getStringExtra(Constants.IntentExtrasKeys.ORDER_DETAILS_BRANCH);
        orderId = intent.getStringExtra(Constants.IntentExtrasKeys.ORDER_ID);
        orderDetails = (OrderDetails) intent.getSerializableExtra(Constants.IntentExtrasKeys.ORDER_INFO);
    }

    private void checkItems() {
        if (cartItemArrayList.size() > 0) {
            //TODO: Show the top card
            orderItemsRecyclerView.setVisibility(VISIBLE);
            noItemsView.setVisibility(GONE);
        } else {
            noItemsView.setVisibility(VISIBLE);
            orderItemsRecyclerView.setVisibility(GONE);
            //TODO: Hide the top card
        }
    }

    private void setOrderInfo() {
        if (isVendor) {
            userTypeTxtView.setText("Caterer Name: ");
            userTypeTxtView.setText("Caterer");
            //TODO:Null pointer to fix
            userNameTxtView.setText(orderDetails.getCatererName());
            if (orderDetails.getOrderStatus() < 2) {
                setOrderStatus(true);
                deleteOrderBtn.setVisibility(GONE);
            } else {
                setOrderStatus(false);
                deleteOrderBtn.setVisibility(VISIBLE);
            }
        } else {
            if (orderDetails.getOrderStatus() < 2) {
                extraNotesEditBtn.setVisibility(VISIBLE);
            }
            extraNotesEditBtn.setVisibility(VISIBLE);
            viewVendorBtn.setVisibility(VISIBLE);
            userTypeTxtView.setText("Vendor Name: ");
            viewVendorBtn.setVisibility(VISIBLE);
            userTypeTxtView.setText("Vendor");
            userNameTxtView.setText(orderDetails.getVendorName());
            setOrderStatus(false);
        }
        orderIDTxtView.setText(orderId);
        if (orderDetails.getExtraNotes() != null) {
            if (!orderDetails.getExtraNotes().equals("")) {
                showExtraNotes();
            }
        }
        String totalAmount = "₹" + String.valueOf(orderDetails.getOrderTotalAmount());
        orderTotalAmtTxtView.setText(totalAmount);
        String timeStamp[] = String.valueOf(orderDetails.getOrderTime()).split(" ");
        orderDateTxtView.setText(timeStamp[0]);
        orderTimeTxtView.setText(timeStamp[1]);
    }

    private void showExtraNotes() {
        extraNotesEditLayout.setVisibility(GONE);
        extraNotesLayout.setVisibility(VISIBLE);
        extraNotesTxtView.setText(orderDetails.getExtraNotes());
    }

    private void setOrderStatus(boolean isPendingOrder) {
        if (isPendingOrder) {
            orderRejectBtn.setVisibility(VISIBLE);
            orderStatusUpdateBtn.setVisibility(VISIBLE);
        } else {
            orderRejectBtn.setVisibility(INVISIBLE);
            orderStatusUpdateBtn.setVisibility(INVISIBLE);
        }
        if (orderDetails.getOrderStatus() > 0) {
            orderRejectBtn.setVisibility(INVISIBLE);
        }
        String status;
        String nextStaus = "";
        //TODO Set color in this switch case
        switch (orderDetails.getOrderStatus()) {
            case 0:
                status = "Awaiting approval";
                nextStaus = "Approve";
                break;
            case 1:
                status = "Approved and Processing";
                nextStaus = "Complete";
                break;
            case 2:
                status = "Completed";
                break;
            case 3:
                status = "Rejected";
                break;
            default:
                status = "Status Unavailable";
                break;
        }
        orderStatusTxtView.setText(status);
        orderStatusUpdateBtn.setText(nextStaus);
    }

    @Override
    public void onBackPressed() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        super.onBackPressed();
    }

    private void fetchItems() {
        loadingDialog = new LoadingDialog(this, "Loading order details...");
        loadingDialog.show();
        handler = new Handler();
        runnable = () -> {
            if (loadingDialog != null)
                if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                    Toasty.error(OrderDetailsActivity.this, "Please check your internet connection and try again!", Toast.LENGTH_SHORT).show();
                    checkItems();
                }
        };
        handler.postAtTime(runnable, System.currentTimeMillis() + Constants.UtilConstants.LOADING_TIMEOUT);
        handler.postDelayed(runnable, Constants.UtilConstants.LOADING_TIMEOUT);
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + orderBranchName +
                FirebaseAuth.getInstance().getUid() + "/" + orderId + "/" + FirebaseUtils.ORDER_ITEMS_BRANCH;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CartItem cartItem = snapshot.getValue(CartItem.class);
                    cartItemArrayList.add(cartItem);
                    orderItemsAdapter.notifyDataSetChanged();
                }
                checkItems();
                if (loadingDialog != null) {
                    if (loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (loadingDialog != null) {
                    if (loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                }
                Toasty.error(OrderDetailsActivity.this, "Error occured while fetching items!", Toast.LENGTH_SHORT).show();
                checkItems();
            }
        });
    }

    private void initializeFields() {
        isVendor = AppUtils.isCurrentUserVendor(this);

        noItemsView = findViewById(R.id.error_items_list_empty);
        dashboardLinkBtn = findViewById(R.id.no_dash);
        userTypeTxtView = findViewById(R.id.li_caterer_order_info_user_type);
        userNameTxtView = findViewById(R.id.li_caterer_order_info_vendor_name);
        orderIDTxtView = findViewById(R.id.li_caterer_order_info_id);
        orderDateTxtView = findViewById(R.id.li_caterer_order_info_timestamp);
        orderTimeTxtView = findViewById(R.id.li_caterer_order_info_timestamp_time);
        orderTotalAmtTxtView = findViewById(R.id.li_caterer_order_info_order_total);
        orderStatusTxtView = findViewById(R.id.li_caterer_order_info_status);
        deleteOrderBtn = findViewById(R.id.li_caterer_order_info_delete);
        orderItemsRecyclerView = findViewById(R.id.act_ord_det_order_items_recyc_view);
        viewVendorBtn = findViewById(R.id.li_caterer_order_info_view_vendor);
        orderStatusUpdateBtn = findViewById(R.id.li_caterer_order_status_update);
        orderRejectBtn = findViewById(R.id.li_caterer_order_reject);
        extraNotesLayout = findViewById(R.id.li_order_info_extra_notes_layout);
        extraNotesEditLayout = findViewById(R.id.li_order_info_extra_notes_edit_layout);
        extraNotesEditTxt = findViewById(R.id.li_order_info_extra_notes_edit_txt);
        extraNotesTxtView = findViewById(R.id.li_order_info_extra_notes);
        extraNotesEditBtn = findViewById(R.id.li_order_info_extra_notes_edit_btn);
        extraNotesEditSubmitBtn = findViewById(R.id.li_order_info_extra_notes_edit_submit);

        deleteOrderBtn.setOnClickListener(this);
        viewVendorBtn.setOnClickListener(this);
        dashboardLinkBtn.setOnClickListener(this);
        orderStatusUpdateBtn.setOnClickListener(this);
        orderRejectBtn.setOnClickListener(this);
        extraNotesEditBtn.setOnClickListener(this);
        extraNotesEditSubmitBtn.setOnClickListener(this);

        cartItemArrayList = new ArrayList<>();
        orderItemsAdapter = new OrderItemsAdapter();
        orderItemsAdapter.setCartItemArrayList(cartItemArrayList);
        orderItemsAdapter.setActivity(this);
        orderItemsLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        orderItemsRecyclerView.setLayoutManager(orderItemsLayoutManager);
        orderItemsRecyclerView.setAdapter(orderItemsAdapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == deleteOrderBtn.getId()) {
            launchDeleteConfirmationDialog();
        } else if (v.getId() == viewVendorBtn.getId()) {
            if (isVendor) {
                viewCatererDetails();
            } else {
                viewVendorDetails();
            }
        } else if (v.getId() == dashboardLinkBtn.getId()) {
            finish();
        } else if (v.getId() == orderStatusUpdateBtn.getId()) {
            updateOrderStatus();
        } else if (v.getId() == orderRejectBtn.getId()) {
            rejectOrder();
        } else if (v.getId() == extraNotesEditBtn.getId()) {
            editExtraNotes();
        } else if (v.getId() == extraNotesEditSubmitBtn.getId()) {
            submitEditedExtraNotes();
        }
    }

    private void editExtraNotes() {
        if (orderDetails != null) {
            if (orderDetails.getOrderStatus() < 2) {
                extraNotesLayout.setVisibility(GONE);
                extraNotesEditLayout.setVisibility(VISIBLE);
                extraNotesEditTxt.setText(orderDetails.getExtraNotes());
            } else {
                Log.e(TAG, "editExtraNotes: Trying to add notes for a completed order");
            }
        } else {
            Toasty.error(this, "Order details not available").show();
            Log.e(TAG, "editExtraNotes: OrderDetails is null");
        }

    }

    private void submitEditedExtraNotes() {
        if (extraNotesEditTxt.getText() != null) {
            String newExtraNote = extraNotesEditTxt.getText().toString();
            String ordersDatabaseNode;
            extraNotesEditLayout.setVisibility(GONE);
            //TODO : show progress bar
            if (isVendor) {
                if (orderDetails.getOrderStatus() < 2) {
                    ordersDatabaseNode = FirebaseUtils.VENDOR_PENDING_ORDERS;
                } else {
                    ordersDatabaseNode = FirebaseUtils.ORDERS_VENDOR_BRANCH;
                }
            } else {
                ordersDatabaseNode = FirebaseUtils.ORDERS_CATERER_BRANCH;
            }
            String orderExtraNotesPath = FirebaseUtils.getDatabaseMainBranchName() + ordersDatabaseNode +
                    FirebaseAuth.getInstance().getUid() + "/" + orderDetails.getOrderId() +
                    "/" + FirebaseUtils.ORDER_INFO_BRANCH + FirebaseUtils.ORDER_EXTRA_NOTES;
            DatabaseReference orderDetailsReference = FirebaseDatabase.getInstance().getReference(orderExtraNotesPath);
            orderDetailsReference.setValue(newExtraNote)
                    .addOnSuccessListener(aVoid -> {
                        Toasty.success(OrderDetailsActivity.this, "Edited notes successfully").show();
                        orderDetails.setExtraNotes(newExtraNote);
                        showExtraNotes();

                    })
                    .addOnFailureListener(e -> {
                        Toasty.success(OrderDetailsActivity.this, "Editing notes failed").show();
                        showExtraNotes();
                    });
        } else {
            Log.e(TAG, "submitEditedExtraNotes: Extra notes edit text field is null");
        }

    }

    private void launchDeleteConfirmationDialog() {
        if (orderDetails.getOrderStatus() > 1) {
            androidx.appcompat.app.AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new androidx.appcompat.app.AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setTitle(getResources().getString(R.string.dialog_title_delete_order_history))
                    .setMessage(
                            getResources().getString(R.string.dialog_message_delete_order_history))
                    .setPositiveButton(
                            getResources().getString(R.string.dialog_btn_yes),
                            (dialog, which) -> deleteOrder())
                    .setNegativeButton((getResources().getString(R.string.dialog_btn_no))
                            , (dialog, which) -> dialog.dismiss()).show();
        } else {
            Toasty.warning(this, "You cannot delete an unfulfilled order!").show();
        }
    }

    private void viewVendorDetails() {
        Intent viewVendorIntent = new Intent(OrderDetailsActivity.this, ViewVendorItemsActivity.class);
        viewVendorIntent.putExtra(Constants.IntentExtrasKeys.VIEW_VENDOR_ITEMS_INTENT_VENDOR_UID, orderDetails.getVendorId());
        startActivity(viewVendorIntent);
        finish();
    }

    private void viewCatererDetails() {
        Intent viewCatererIntent = new Intent(OrderDetailsActivity.this, CatererProfileActivity.class);
        viewCatererIntent.putExtra(Constants.IntentExtrasKeys.USER_ID, orderDetails.getCatererID());
        startActivity(viewCatererIntent);
    }

    private void deleteOrder() {
        String orderID = orderDetails.getOrderId();
        String branch = AppUtils.isCurrentUserVendor(this) ? FirebaseUtils.ORDERS_VENDOR_BRANCH : FirebaseUtils.ORDERS_CATERER_BRANCH;
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + branch +
                FirebaseAuth.getInstance().getUid() + "/" + orderID;
        DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference(databasePath);
        orderReference.setValue(null)
                .addOnSuccessListener(aVoid -> {
                    Toasty.success(this, "Order deleted from history.").show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toasty.error(this, "Failed to delete order from history!").show();
                    Log.e(TAG, "deleteOrder: " + e.getMessage());
                });
    }

    private void rejectOrder() {
        androidx.appcompat.app.AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new androidx.appcompat.app.AlertDialog.Builder(OrderDetailsActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth);
        } else {
            builder = new androidx.appcompat.app.AlertDialog.Builder(OrderDetailsActivity.this);
        }
        String message = "Reject order?";
        String subMessage = " reject the following order ";
        builder.setTitle(message)
                .setMessage("This action cannot be undone. You are about to" + subMessage + "\nOrder id :" + orderDetails.getOrderId()
                        + "\nOrdered by: " + orderDetails.getCatererName()
                        + "\nOn: " + orderDetails.getOrderTime())
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    rejectOrder(orderDetails.getCatererID(), orderDetails.getVendorName(), orderDetails.getOrderId(), orderDetails.getVendorId())
                            .addOnCompleteListener(task -> {
                                if (!task.isSuccessful()) {
                                    Exception e = task.getException();
                                    if (e instanceof FirebaseFunctionsException) {
                                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                        FirebaseFunctionsException.Code code = ffe.getCode();
                                        Object details = ffe.getDetails();
                                        Log.e(TAG, "onComplete: " + ffe);
                                        if (details != null)
                                            Log.e(TAG, "onComplete: " + details.toString());
                                        Toasty.error(OrderDetailsActivity.this, "Order rejection failed!", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toasty.info(OrderDetailsActivity.this, "Order will be rejected", Toast.LENGTH_SHORT).show();
                                    finish();

                                }
                            });
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss())
                .show();


    }

    private Task<String> rejectOrder(String catererID, String vendorName, String orderID, String vendorID) {
        // Create the arguments to the callable function.

        Map<String, Object> data = new HashMap<>();
        data.put("catererID", catererID);
        data.put("orderID", orderID);
        data.put("vendorName", vendorName);
        data.put("vendorID", vendorID);

        return FirebaseFunctions.getInstance()
                .getHttpsCallable("sendRejectionNotification")
                .call(data)
                .continueWith(task -> {
                    if (task.getResult() != null) {
                        String result = (String) task.getResult().getData();
                        return result;
                    } else {
                        return "";
                    }
                });
    }

    private void updateOrderStatus() {
        androidx.appcompat.app.AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new androidx.appcompat.app.AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth);
        } else {
            builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        }
        String message;
        String subMessage;
        switch (orderDetails.getOrderStatus()) {
            case 0:
                message = "Approve order?";
                subMessage = " approve the following order ";
                break;
            case 1:
                message = "Mark order as completed?";
                subMessage = " mark the following order as COMPLETED ";
                break;
            default:
                message = "";
                subMessage = " update status of the following order ";
                break;

        }
        builder.setTitle(message)
                .setMessage("This action cannot be undone. You are about to" + subMessage + "\nOrder id :" + orderDetails.getOrderId()
                        + "\nOrdered by: " + orderDetails.getCatererName()
                        + "\nOn: " + orderDetails.getOrderTime())
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.VENDOR_PENDING_ORDERS +
                            FirebaseAuth.getInstance().getUid() + "/" +
                            orderDetails.getOrderId() + "/" +
                            FirebaseUtils.ORDER_INFO_BRANCH +
                            FirebaseUtils.ORDER_STATUS;
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
                    databaseReference.setValue(orderDetails.getOrderStatus() + 1)
                            .addOnSuccessListener(aVoid -> {
                                orderDetails.setOrderStatus(orderDetails.getOrderStatus() + 1);
                                if (orderDetails.getOrderStatus() < 2) {
                                    setOrderStatus(true);
                                    deleteOrderBtn.setVisibility(GONE);
                                    Toasty.success(OrderDetailsActivity.this, "Order status updated successfully", Toast.LENGTH_SHORT).show();
                                } else if (orderDetails.getOrderStatus() == 2) {
                                    Toasty.success(OrderDetailsActivity.this, "Order completed!", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    setOrderStatus(false);
                                    deleteOrderBtn.setVisibility(VISIBLE);
                                }
                            })
                            .addOnFailureListener(e -> Toasty.error(OrderDetailsActivity.this, "Order status update failed! Please try again...", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss())
                .show();
    }
}
