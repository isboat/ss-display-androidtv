package com.onscreensync.tvapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.widget.TextView;

import com.onscreensync.tvapp.signalR.SignalrHubConnectionBuilder;
import com.onscreensync.tvapp.utils.ObjectExtensions;

public class TextEditorActivity extends AppCompatActivity {

    private String textEditorData;
    private TextView textEditorTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);

        // Retrieve the Intent that started this activity
        Intent intent = getIntent();
        textEditorData = intent.getStringExtra("textEditorData");

        if(ObjectExtensions.isNullOrEmpty(textEditorData)) textEditorData = "Error: No text found in the data, republish.";
        textEditorTextView = findViewById(R.id.text_editor_activity_text_view);
        textEditorTextView.setText(Html.fromHtml(textEditorData));
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