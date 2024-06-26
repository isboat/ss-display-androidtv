package com.onscreensync.tvapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.onscreensync.tvapp.signalR.SignalrHubConnectionBuilder;

public class ContentInfoMessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contentnfo_message);

        // Retrieve the Intent that started this activity
        Intent intent = getIntent();
        String message = intent.getStringExtra("message");
        String deviceName = intent.getStringExtra("deviceName");

        TextView messageTextView = findViewById(R.id.activity_content_info_message_textview);
        messageTextView.setText(message);

        TextView deviceNameTextView = findViewById(R.id.activity_content_info_subtext_textview);
        deviceNameTextView.setText(deviceName);
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