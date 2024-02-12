package com.onscreensync.tvapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class ServiceUnavailableActivity extends AppCompatActivity {

    private EditText messageEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_unavailable);

        messageEditText = findViewById(R.id.service_unavailable_activity_multiline_text);
        messageEditText.setFocusable(false);
    }
}