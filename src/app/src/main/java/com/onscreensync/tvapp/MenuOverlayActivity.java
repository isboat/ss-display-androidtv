package com.onscreensync.tvapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import com.onscreensync.tvapp.datamodels.MediaAssetDataModel;
import com.onscreensync.tvapp.datamodels.MenuItemDataModel;
import com.onscreensync.tvapp.datamodels.MenuMetadata;
import com.onscreensync.tvapp.fragments.BasicMenuFragment;
import com.onscreensync.tvapp.fragments.ImageMediaFragment;
import com.onscreensync.tvapp.fragments.VideoMediaFragment;
import com.onscreensync.tvapp.utils.ObjectExtensions;

public class MenuOverlayActivity extends AppCompatActivity {

    RelativeLayout relativeLayout;
    private MenuItemDataModel[] menuItems;
    private MenuMetadata menuMetadata;
    private MediaAssetDataModel mediaAsset;
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
        menuItems = ObjectExtensions.getParcelableArrayExtra(getIntent(), "menuItems", MenuItemDataModel.class);
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