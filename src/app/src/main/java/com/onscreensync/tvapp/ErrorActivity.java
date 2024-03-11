package com.onscreensync.tvapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;
import android.widget.TextView;

import com.onscreensync.tvapp.signalR.SignalrHubConnectionBuilder;

public class ErrorActivity extends AppCompatActivity {

    private EditText errorMessageTextView;
    private TextView errorTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        // Display the received data in a TextView (assuming you have a TextView in your layout)
        errorMessageTextView = findViewById(R.id.error_page_error_message_txt);
        errorTitleTextView = findViewById(R.id.error_page_title_txt);

        // Retrieve the Intent that started this activity
        Intent intent = getIntent();

        // Retrieve the data from the Intent using getStringExtra
        String errorTitle = intent.getStringExtra("errorTitle");
        String errorMessage = intent.getStringExtra("errorMessage");

        errorMessageTextView.setText(errorMessage);
        errorTitleTextView.setText(errorTitle);
        errorMessageTextView.setFocusable(false);
    }

    @Override
    public void onBackPressed() {
        SignalrHubConnectionBuilder.getInstance().removeConnectionFromGroup();

        final Handler handler = new Handler();
        handler.postDelayed(() -> {

            finishAffinity();
            finish();
            // Call System.exit(0) to terminate the entire process
            System.exit(0);
        }, 2000);
    }
}