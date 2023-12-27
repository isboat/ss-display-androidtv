package com.example.screenservicetvapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CurrentDateTimeActivity extends AppCompatActivity {

    private TextView dateTimeTextView;
    private String dateTimeFormat;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_time);

        // Retrieve the Intent that started this activity
        Intent intent = getIntent();
        dateTimeFormat = intent.getStringExtra("dateTimeFormat");

        if(ObjectExtensions.isNullOrEmpty(dateTimeFormat)) dateTimeFormat = "EEE, d MMM yyyy, HH:mm:ss";

        handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                displayCurrentDateTime();
                handler.postDelayed(this, 1000);
            }
        };

        handler.postDelayed(r, 1000);

    }

    private void displayCurrentDateTime()
    {
        DateFormat df = new SimpleDateFormat(dateTimeFormat);
        String date = df.format(Calendar.getInstance().getTime());

        dateTimeTextView = findViewById(R.id.datetime_activity_text_view);
        dateTimeTextView.setText(Html.fromHtml(date));
    }
}