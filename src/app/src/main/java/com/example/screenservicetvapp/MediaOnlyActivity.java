package com.example.screenservicetvapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

public class MediaOnlyActivity extends AppCompatActivity {

    FrameLayout frameLayout;
    String externalMediaSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_only);

        frameLayout = (FrameLayout) findViewById(R.id.media_only_framelayout);

        // Retrieve the Intent that started this activity
        Intent intent = getIntent();
        ContentDataMediaAsset mediaAsset = intent.getParcelableExtra("mediaAsset");
        externalMediaSource = intent.getStringExtra("externalMediaSource");

        if(!ObjectExtensions.isNullOrEmpty(externalMediaSource)) {
            loadExternalMediaFragment(externalMediaSource);
        } else {
            boolean isImg = GetMediaType(mediaAsset);
            Log.d("INElse", "isImg" + isImg);
            if(isImg) {
                loadImageMediaFragment(mediaAsset.getAssetUrl());
            }
        }
    }

    private boolean GetMediaType(ContentDataMediaAsset mediaAsset) {
        return mediaAsset.getType() == 1; // image is 1, video is 2
    }

    private void loadImageMediaFragment(String assetUrl) {
        Bundle bundle = new Bundle();
        bundle.putString("assetUrl", assetUrl);
        Fragment fragment = new ImageMediaFragment();
        loadFragment(fragment, bundle);
    }

    private void loadExternalMediaFragment(String externalMediaSource) {
        Bundle bundle = new Bundle();
        bundle.putString("externalMediaSource", externalMediaSource);

        Fragment fragment = new ExternalMediaFragment();
        loadFragment(fragment, bundle);
    }

    private void loadFragment(Fragment fragment, Bundle bundle) {
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.media_only_framelayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}