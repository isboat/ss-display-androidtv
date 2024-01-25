package com.example.screenservicetvapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.example.screenservicetvapp.utils.ObjectExtensions;

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
        finishAffinity();
        finish();
    }
}