package com.onscreensync.tvapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.onscreensync.tvapp.services.DeviceService;
import com.onscreensync.tvapp.services.LocalStorageService;
import com.onscreensync.tvapp.utils.ObjectExtensions;

public class MainActivity extends FragmentActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;
    private LocalStorageService storageService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(R.style.Theme_ScreenServiceTVApp);
        setContentView(R.layout.activity_main);

        requestBootCompletedPermission();
        this.storageService = new LocalStorageService(this);

        Handler handler = new Handler();
        final Runnable r = () -> startRun();
        handler.postDelayed(r, 3000);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
        System.exit(0);
    }

    private void startRun() {
        // Retrieve access token
        String accessToken = storageService.getAccessToken();
        Intent intent;
        if(ObjectExtensions.isNullOrEmpty(accessToken))
        {
            intent = new Intent(this, CodeActivationActivity.class);
        } else {
            updateDeviceInfo();
            intent = new Intent(this, ContentActivity.class);
        }
        startIntent(intent);
    }

    private void updateDeviceInfo() {
        DeviceService deviceService = new DeviceService(this);
        deviceService.updateDeviceInfo();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Handle permission results here if needed
    }

    private void requestBootCompletedPermission() {

        // Request the RECEIVE_BOOT_COMPLETED permission if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void startIntent(Intent intent) {
        // Start the new activity
        startActivity(intent);
        finish(); // Close the current activity
    }
}