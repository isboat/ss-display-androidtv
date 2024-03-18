package com.onscreensync.tvapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.onscreensync.tvapp.datamodels.MediaAssetDataModel;
import com.onscreensync.tvapp.datamodels.PlaylistItemSerializedDataModel;
import com.onscreensync.tvapp.datamodels.TextADInformationAsset;
import com.onscreensync.tvapp.signalR.SignalrHubConnectionBuilder;
import com.onscreensync.tvapp.utils.JsonUtils;
import com.onscreensync.tvapp.utils.ObjectExtensions;
import com.onscreensync.tvapp.utils.UiHelper;
import com.squareup.picasso.Picasso;

public class PlaylistActivity extends AppCompatActivity {

    private PlaylistItemSerializedDataModel[] itemsSerialized;

    private int itemDuration; //"00:00:20"
    TextView adsTextView;
    ImageView imageAssetImageView;
    VideoView videoAssetVideoView;
    private int currentIndex = 0;

    private MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_playlist);

        adsTextView = findViewById(R.id.media_playlist_activity_ads_textview);
        imageAssetImageView = findViewById(R.id.media_playlist_image_asset);

        videoAssetVideoView = findViewById(R.id.media_playlist_video_asset_view);
        setupMediaPlayer();

        Intent intent = getIntent();
        itemDuration = parseItemDuration(intent.getStringExtra("itemDuration"));
        itemsSerialized = ObjectExtensions.getParcelableArrayExtra(getIntent(), "itemsSerialized", PlaylistItemSerializedDataModel.class);

        adsTextView.setVisibility(TextView.VISIBLE);

        if(itemsSerialized != null) {
            if(itemsSerialized.length == 0) {
                Toast.makeText(getApplicationContext(), "No item in the playlist, please add items and republish.", Toast.LENGTH_LONG).show();
            } else {
                adsTextView.setVisibility(TextView.INVISIBLE);
                currentIndex = -1;
                playNext();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "No item in the playlist, please add items and republish.", Toast.LENGTH_LONG).show();
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
        SignalrHubConnectionBuilder.getInstance().removeConnectionFromGroup();

        final Handler handler = new Handler();
        handler.postDelayed(() -> {

            finishAffinity();
            finish();
            // Call System.exit(0) to terminate the entire process
            System.exit(0);
        }, 2000);
    }

    private void playNext() {
        currentIndex++;
        if (currentIndex >= itemsSerialized.length) {
            currentIndex = 0; // Start from the beginning if reached the end
        }

        PlaylistItemSerializedDataModel currentItem = itemsSerialized[currentIndex];

        adsTextView.setVisibility(TextView.INVISIBLE);
        imageAssetImageView.setVisibility(ImageView.INVISIBLE);
        videoAssetVideoView.setVisibility(VideoView.INVISIBLE);

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
                Toast.makeText(getApplicationContext(), "No such Item type.", Toast.LENGTH_LONG).show();
                playNext();
                break;
        }
    }

    private void playAdTextMedia(TextADInformationAsset currentItem) {
        if(adsTextView != null)
        {
            String description = currentItem.getDescription();
            String backgroundColor = currentItem.getBackgroundColor();
            String textColor = currentItem.getTextColor();
            String textFont = currentItem.getTextFont();

            if(ObjectExtensions.isNullOrEmpty(description)) description = "Error: No text found in the data, republish.";
            adsTextView.setText(Html.fromHtml(description));
            UiHelper.setTextViewFont(adsTextView, textFont);
            UiHelper.setTextViewColor(adsTextView, textColor);

            adsTextView.setBackgroundColor(UiHelper.parseColor(backgroundColor));
        }
    }

    private void playImageMedia(MediaAssetDataModel currentItem) {
        if(imageAssetImageView != null)
        {
            String assetUrl = currentItem.getAssetUrl();
            Picasso.get().load(assetUrl).into(imageAssetImageView);
        }
    }

    private void setupMediaPlayer() {

        if(videoAssetVideoView != null) {
            // Set up a MediaController to enable play, pause, etc. controls
            mediaController = new MediaController(this);
            mediaController.setAnchorView(videoAssetVideoView);
            videoAssetVideoView.setMediaController(mediaController);

            //Video Loop
            videoAssetVideoView.setOnCompletionListener(mp -> playNext());

            videoAssetVideoView.setOnPreparedListener(mp -> mp.setLooping(false));
        }
    }
    private void playVideoMedia(String videoAssetUrl) {
        mediaController.resetPivot();
        // Set the video URL using setVideoPath or setVideoURI
        videoAssetVideoView.setVideoPath(videoAssetUrl);

        // Start playing the video
        videoAssetVideoView.start();
    }

    private void waitBeforeNext() {
        final Handler handler = new Handler();
        final int delayMillis = itemDuration;

        handler.postDelayed(() -> playNext(), delayMillis);
    }

    private void runTextAssetItemModel(PlaylistItemSerializedDataModel itemSerialized) {
        TextADInformationAsset currentItem = JsonUtils.fromJson(itemSerialized.getValue(), TextADInformationAsset.class);
        if(currentItem == null)
        {
            Toast.makeText(getApplicationContext(), "ItemSerialized is null.", Toast.LENGTH_LONG).show();
            playNext();
        }
        adsTextView.setVisibility(TextView.VISIBLE);
        playAdTextMedia(currentItem);
        waitBeforeNext();
    }

    private void runAssetItemModel(PlaylistItemSerializedDataModel itemSerialized) {
        MediaAssetDataModel currentItem = JsonUtils.fromJson(itemSerialized.getValue(), MediaAssetDataModel.class);
        if(currentItem == null)
        {
            Toast.makeText(getApplicationContext(), "ItemSerialized is null.", Toast.LENGTH_LONG).show();
            return;
        }
        // Do something with the current item
        int assetType = currentItem.getType();

        switch (assetType) {
            case 1: // Image
                imageAssetImageView.setVisibility(ImageView.VISIBLE);
                playImageMedia(currentItem);
                waitBeforeNext();
                break;
            case 2: // Video
                videoAssetVideoView.setVisibility(VideoView.VISIBLE);
                playVideoMedia(currentItem.getAssetUrl());
                break;
            default:
                Toast.makeText(getApplicationContext(), "No such media type.", Toast.LENGTH_LONG).show();
                break;
        }
    }
}