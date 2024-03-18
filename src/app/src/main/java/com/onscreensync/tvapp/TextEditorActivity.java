package com.onscreensync.tvapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.widget.TextView;

import com.onscreensync.tvapp.signalR.SignalrHubConnectionBuilder;
import com.onscreensync.tvapp.utils.ObjectExtensions;
import com.onscreensync.tvapp.utils.UiHelper;

public class TextEditorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);

        // Retrieve the Intent that started this activity
        Intent intent = getIntent();
        String textEditorData = intent.getStringExtra("textEditorData");
        String textColor = intent.getStringExtra("textColor");
        String textFont = intent.getStringExtra("textFont");
        String backgroundColor = intent.getStringExtra("backgroundColor");

        if(ObjectExtensions.isNullOrEmpty(textEditorData)) textEditorData = "Error: No text found in the data, republish.";
        TextView textEditorTextView = findViewById(R.id.text_editor_activity_text_view);
        textEditorTextView.setText(Html.fromHtml(textEditorData));

        if(!ObjectExtensions.isNullOrEmpty(textFont)) {
            UiHelper.setTextViewFont(textEditorTextView, textFont);
        }

        if(!ObjectExtensions.isNullOrEmpty(textColor)) {
            UiHelper.setTextViewColor(textEditorTextView, textColor);
        }

        if(!ObjectExtensions.isNullOrEmpty(backgroundColor)) {
            textEditorTextView.setBackgroundColor(UiHelper.parseColor(backgroundColor));
        }
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