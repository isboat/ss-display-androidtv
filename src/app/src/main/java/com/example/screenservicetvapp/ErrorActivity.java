package com.example.screenservicetvapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

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
    }
}