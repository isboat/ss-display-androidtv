package com.onscreensync.tvapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;

import com.onscreensync.tvapp.datamodels.MediaAssetDataModel;
import com.onscreensync.tvapp.fragments.ExternalMediaFragment;
import com.onscreensync.tvapp.fragments.ImageMediaFragment;
import com.onscreensync.tvapp.fragments.VideoMediaFragment;
import com.onscreensync.tvapp.signalR.SignalrHubConnectionBuilder;
import com.onscreensync.tvapp.utils.ObjectExtensions;

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
        MediaAssetDataModel mediaAsset = intent.getParcelableExtra("mediaAsset");
        externalMediaSource = intent.getStringExtra("externalMediaSource");

        if(!ObjectExtensions.isNullOrEmpty(externalMediaSource)) {
            loadExternalMediaFragment(externalMediaSource);
        } else {
            int mediaType = mediaAsset.getType();
            switch(mediaType) {
                case 1: // Image
                    loadImageMediaFragment(mediaAsset.getAssetUrl());
                    break;
                case 2: // Video
                    loadVideoMediaFragment(mediaAsset.getAssetUrl());
                    break;
                default:
                    Log.d("MediaOnlyActivity", "No such media type");
                    break;
            }
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
    private void loadVideoMediaFragment(String assetUrl) {
        Bundle bundle = new Bundle();
        bundle.putString("assetUrl", assetUrl);
        Fragment fragment = new VideoMediaFragment();
        loadFragment(fragment, bundle);
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