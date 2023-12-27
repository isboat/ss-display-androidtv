package com.example.screenservicetvapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MediaPlaylistActivity extends AppCompatActivity {

    private ContentDataMediaAsset[] assetItems;
    private String itemDuration;
    TextView messageTextView;
    FrameLayout frameLayout;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_playlist);

        messageTextView = findViewById(R.id.media_playlist_activity_message_textview);
        frameLayout = findViewById(R.id.media_playlist_activity_framelayout);

        Intent intent = getIntent();
        itemDuration = intent.getStringExtra("itemDuration");
        assetItems = ObjectExtensions.getParcelableArrayExtra(getIntent(), "assetItems", ContentDataMediaAsset.class);

        messageTextView.setVisibility(TextView.VISIBLE);
        if(assetItems != null) {
            if(assetItems.length == 0) {
                messageTextView.setText("No item in the playlist, please add items and republish");
            } else {
                messageTextView.setVisibility(TextView.INVISIBLE);
                iterateWithDelay(assetItems);
            }
        }
    }

    private void iterateWithDelay(ContentDataMediaAsset[] assetItems) {
        final Handler handler = new Handler();
        final int delayMillis = 1000; // 5 seconds

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Your iteration logic here
                if (currentIndex < assetItems.length) {
                    ContentDataMediaAsset currentItem = assetItems[currentIndex];
                    // Do something with the current item
                    int assetType = currentItem.getType();
                    switch (assetType) {
                        case 1: // Image
                            loadImageMediaFragment(currentItem.getAssetUrl());
                            break;
                        case 2: // Video
                            loadVideoMediaFragment(currentItem.getAssetUrl());
                            break;
                        default:
                            messageTextView.setText("No such media type");
                            break;
                    }

                    // Move to the next index
                    currentIndex++;

                    // Continue iterating after the delay
                    handler.postDelayed(this, delayMillis);
                } else {
                    // All elements have been processed
                    // You can perform any actions after the iteration is complete
                }
            }
        }, delayMillis);
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

    private void loadFragment(Fragment fragment, Bundle bundle) {
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.media_playlist_activity_framelayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}