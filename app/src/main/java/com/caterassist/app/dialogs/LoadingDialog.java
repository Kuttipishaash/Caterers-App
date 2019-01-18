package com.caterassist.app.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.caterassist.app.R;

import androidx.annotation.NonNull;

public class LoadingDialog extends Dialog {
    private TextView loadingMessageTxtView;
    private ProgressBar loadingProgressBar;

    public LoadingDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loading_dialog);
        initViews();
        this.setCancelable(false);
    }

    public void setLoadingMessage(String message) {
        loadingMessageTxtView.setText(message);
    }

    private void initViews() {
        loadingMessageTxtView = findViewById(R.id.loading_message);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
    }

}
