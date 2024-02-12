package com.onscreensync.tvapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.onscreensync.tvapp.datamodels.MediaAssetDataModel;
import com.onscreensync.tvapp.datamodels.PlaylistItemSerializedDataModel;
import com.onscreensync.tvapp.datamodels.TextADInformationAsset;
import com.onscreensync.tvapp.fragments.ImageMediaFragment;
import com.onscreensync.tvapp.fragments.TextADInformationFragment;
import com.onscreensync.tvapp.fragments.VideoMediaFragment;
import com.onscreensync.tvapp.utils.JsonUtils;
import com.onscreensync.tvapp.utils.ObjectExtensions;

public class PlaylistActivity extends AppCompatActivity {

    private PlaylistItemSerializedDataModel[] itemsSerialized;

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
        itemsSerialized = ObjectExtensions.getParcelableArrayExtra(getIntent(), "itemsSerialized", PlaylistItemSerializedDataModel.class);

        messageTextView.setVisibility(TextView.VISIBLE);
        if(itemsSerialized != null) {
            if(itemsSerialized.length == 0) {
                messageTextView.setText("No item in the playlist, please add items and republish");
            } else {
                messageTextView.setVisibility(TextView.INVISIBLE);
                iterateItemsWithDelay(itemsSerialized);
            }
        }
        else {
            messageTextView.setText("No item in the playlist, please add items and republish");
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

    private void iterateItemsWithDelay(PlaylistItemSerializedDataModel[] itemsSerialized) {
        final Handler handler = new Handler();
        final int delayMillis = 1000; // 5 seconds

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!stopIteration) {
                    // Your iteration logic here
                    if (currentIndex < itemsSerialized.length) {
                        PlaylistItemSerializedDataModel currentItem = itemsSerialized[currentIndex];
                        // Do something with the current item
                        String objType = currentItem.getKey(); // key contains ObjectType
                        switch (objType) {
                            case "AssetItemModel":
                                runAssetItemModel(currentItem);
                                break;
                            case "TextAssetItemModel":
                                runTextAssetItemModel(currentItem);
                                break;
                            default:
                                messageTextView.setText("No such Item type");
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

    private void runTextAssetItemModel(PlaylistItemSerializedDataModel itemSerialized) {
        TextADInformationAsset currentItem = JsonUtils.fromJson(itemSerialized.getValue(), TextADInformationAsset.class);
        if(currentItem == null)
        {
            messageTextView.setText("ItemSerialized is null.");
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("description", currentItem.getDescription());
        bundle.putString("backgroundColor", currentItem.getBackgroundColor());
        bundle.putString("name", currentItem.getName());
        bundle.putString("textColor", currentItem.getTextColor());
        bundle.putString("textFont", currentItem.getTextFont());
        Fragment fragment = new TextADInformationFragment();
        loadFragment(fragment, bundle);
    }

    private void runAssetItemModel(PlaylistItemSerializedDataModel itemSerialized) {
        MediaAssetDataModel currentItem = JsonUtils.fromJson(itemSerialized.getValue(), MediaAssetDataModel.class);
        if(currentItem == null)
        {
            messageTextView.setText("ItemSerialized is null.");
            return;
        }
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