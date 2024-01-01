package com.example.screenservicetvapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;

public class MainActivity extends FragmentActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;
    private DeviceService deviceService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestBootCompletedPermission();

        deviceService = new DeviceService(this);
        deviceService.updateName();
        
        LocalStorageService storageService = new LocalStorageService(this);

        // Retrieve access token
        String accessToken = storageService.getAccessToken();
        Intent intent = accessToken == null
                ? new Intent(this, CodeActivationActivity.class)
                : new Intent(this, ContentActivity.class);
        startIntent(intent);
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