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
    private String loadingMessage;

    public LoadingDialog(@NonNull Context context, String message) {
        super(context);
        this.loadingMessage = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loading_dialog);
        this.setCancelable(false);
        initViews();
    }

    private void initViews() {
        loadingMessageTxtView = findViewById(R.id.loading_message);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        loadingMessageTxtView.setText(loadingMessage);
    }

}
