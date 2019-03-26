package com.caterassist.app.activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.caterassist.app.R;

import androidx.cardview.widget.CardView;
import es.dmoral.toasty.Toasty;

public class ContactUsActivity extends Activity implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = "ContactUs";
    private CardView callCard;
    private CardView emailCard;
    private TextView faqLinkTxtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        initViews();
    }

    private void initViews() {
        callCard = findViewById(R.id.act_contact_call);
        emailCard = findViewById(R.id.act_contact_email);
        faqLinkTxtView = findViewById(R.id.act_contact_faq_link);

        callCard.setOnClickListener(this);
        emailCard.setOnClickListener(this);
        faqLinkTxtView.setOnClickListener(this);

        callCard.setOnLongClickListener(this);
        emailCard.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == callCard.getId()) {
            callCaterBazar();
        } else if (v.getId() == emailCard.getId()) {
            emailCaterBazar();
        } else if (v.getId() == faqLinkTxtView.getId()) {
            startActivity(new Intent(ContactUsActivity.this, FAQActivity.class));
            finish();
        }
    }

    private void emailCaterBazar() {
        String emailAddress = getString(R.string.contact_email);
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Enquiry about CaterBazar.");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        startActivity(emailIntent);
    }

    private void callCaterBazar() {
        String phoneNumber = getString(R.string.contact_phone);
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
        startActivity(callIntent);
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == callCard.getId()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            String label = "Phone number";
            String text = getString(R.string.contact_phone);
            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
            Toasty.info(this, "Phone number copied to clipboard").show();
            return true;
        } else if (v.getId() == emailCard.getId()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            String label = "Email";
            String text = getString(R.string.contact_email);
            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
            Toasty.info(this, "Email address copied to clipboard").show();
            return true;
        } else {
            return false;
        }
    }
}
