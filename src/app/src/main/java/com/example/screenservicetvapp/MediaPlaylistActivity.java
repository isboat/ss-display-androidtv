package com.example.screenservicetvapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
    private int itemDuration; //"00:00:20"
    TextView messageTextView;
    FrameLayout frameLayout;
    private int currentIndex = 0;
    private boolean stopIteration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_playlist);

        messageTextView = findViewById(R.id.media_playlist_activity_message_textview);
        frameLayout = findViewById(R.id.media_playlist_activity_framelayout);

        Intent intent = getIntent();
        itemDuration = parseItemDuration(intent.getStringExtra("itemDuration"));
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

    private int parseItemDuration(String itemDuration) {
        if(ObjectExtensions.isNullOrEmpty(itemDuration)) return 1000;
        String[] timeParts = itemDuration.split(":");
        if(timeParts.length != 3) return 1000;

        int hr = ObjectExtensions.convertToInt(timeParts[0]);
        int min = ObjectExtensions.convertToInt(timeParts[1]);
        int sec = ObjectExtensions.convertToInt(timeParts[2]);

        int hrSec = hr*60*60;
        int minSec = min*60;
        return (hrSec + minSec + sec)*1000;
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
    }

    private void iterateWithDelay(ContentDataMediaAsset[] assetItems) {
        final Handler handler = new Handler();
        final int delayMillis = 1000; // 5 seconds

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!stopIteration) {
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
                        handler.postDelayed(this, itemDuration);
                    } else {
                            // Reset or start again
                            currentIndex = 0;
                            handler.postDelayed(this, delayMillis);
                    }
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
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(!isFinishing() && !fragmentManager.isDestroyed()) {
            fragment.setArguments(bundle);

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.media_playlist_activity_framelayout, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            stopIteration = true;
        }
    }
}