package com.onscreensync.tvapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.onscreensync.tvapp.utils.ObjectExtensions;

public class ContentInfoMessageActivity extends AppCompatActivity {
    private TextView messageTextView;
    private TextView deviceNameTextView;
    private String message;
    private String deviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contentnfo_message);

        // Retrieve the Intent that started this activity
        Intent intent = getIntent();
        message = intent.getStringExtra("message");
        deviceName = intent.getStringExtra("deviceName");

        messageTextView = findViewById(R.id.activity_content_info_message_textview);
        messageTextView.setText(message);

        deviceNameTextView = findViewById(R.id.activity_content_info_subtext_textview);
        deviceNameTextView.setText(deviceName);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
    }
}