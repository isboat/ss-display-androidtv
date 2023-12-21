package com.example.screenservicetvapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

public class MediaOnlyActivity extends AppCompatActivity {

    FrameLayout frameLayout;
    String externalMediaSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_only);

        // Retrieve the Intent that started this activity
        Intent intent = getIntent();

        // Retrieve the data from the Intent using getStringExtra
        externalMediaSource = intent.getStringExtra("externalMediaSource");

        Bundle bundle = new Bundle();
        bundle.putString("externalMediaSource", externalMediaSource);

        frameLayout = (FrameLayout) findViewById(R.id.media_only_framelayout);
        Fragment fragment = new ExternalMediaFragment();
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.media_only_framelayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}