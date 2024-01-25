package com.example.screenservicetvapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import com.example.screenservicetvapp.fragments.BasicMenuFragment;

public class MenuOverlayActivity extends AppCompatActivity {

    RelativeLayout relativeLayout;
    private ContentDataMenuItem[] menuItems;
    private MenuMetadata menuMetadata;
    private MediaAsset mediaAsset;
    private String textFont;
    private String textColor;
    private String backgroundOpacity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_overlay);

        relativeLayout = findViewById(R.id.menu_overlay_activity_relativelayout);

        // Retrieve the Intent that started this activity
        Intent intent = getIntent();
        menuMetadata = intent.getParcelableExtra("menuMetadata");
        textColor = intent.getStringExtra("textColor");
        textFont = intent.getStringExtra("textFont");
        backgroundOpacity = intent.getStringExtra("backgroundOpacity");
        menuItems = ObjectExtensions.getParcelableArrayExtra(getIntent(), "menuItems", ContentDataMenuItem.class);
        loadBasicMenuFragment();

        mediaAsset = intent.getParcelableExtra("mediaAsset");

        if(mediaAsset != null) {
            int mediaType = mediaAsset.getType();
            switch (mediaType) {
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

    private void loadBasicMenuFragment() {
        if(menuMetadata == null || menuItems == null) return;

        Bundle bundle = new Bundle();
        bundle.putParcelable("menuMetadata", menuMetadata);
        bundle.putParcelableArray("menuItems", menuItems);
        bundle.putString("textColor", textColor);
        bundle.putString("textFont", textFont);
        bundle.putString("backgroundOpacity", backgroundOpacity);
        bundle.putBoolean("setTransparentBackground", true);

        Fragment fragment = new BasicMenuFragment();
        loadFragment(fragment, bundle, R.id.menu_overlay_activity_menu_frameLayout);
    }

    private void loadImageMediaFragment(String assetUrl) {
        Bundle bundle = new Bundle();
        bundle.putString("assetUrl", assetUrl);
        Fragment fragment = new ImageMediaFragment();
        loadFragment(fragment, bundle, R.id.menu_overlay_activity_media_frameLayout);
    }

    private void loadVideoMediaFragment(String assetUrl) {
        Bundle bundle = new Bundle();
        bundle.putString("assetUrl", assetUrl);
        Fragment fragment = new VideoMediaFragment();
        loadFragment(fragment, bundle, R.id.menu_overlay_activity_media_frameLayout);
    }

    private void loadFragment(Fragment fragment, Bundle bundle, int elementId) {
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(elementId, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}