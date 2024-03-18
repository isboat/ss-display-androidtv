package com.onscreensync.tvapp;

import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.onscreensync.tvapp.signalR.SignalrHubConnectionBuilder;

public class ServiceUnavailableActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_unavailable);

        EditText messageEditText = findViewById(R.id.service_unavailable_activity_multiline_text);
        messageEditText.setFocusable(false);
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