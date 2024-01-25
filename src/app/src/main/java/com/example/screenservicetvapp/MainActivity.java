package com.example.screenservicetvapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.screenservicetvapp.workers.AppStatusWorker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class MainActivity extends FragmentActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;
    private DeviceService deviceService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(R.style.Theme_ScreenServiceTVApp);
        setContentView(R.layout.activity_main);

        requestBootCompletedPermission();

        deviceService = new DeviceService(this);
        deviceService.updateName();

        Handler handler = new Handler();
        final Runnable r = () -> startRun();
        handler.postDelayed(r, 3000);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
    }

    private void startRun() {
        LocalStorageService storageService = new LocalStorageService(this);
        // Retrieve access token
        String accessToken = storageService.getAccessToken();
        Intent intent = accessToken == null
                ? new Intent(this, CodeActivationActivity.class)
                : new Intent(this, ContentActivity.class);
        startIntent(intent);

        //startWorkerRun();
        //setUpFirebase();
    }

    private void setUpFirebase() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        Log.d("FCMToken", token);
                        Toast.makeText(MainActivity.this, "Your token: " + token, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startWorkerRun() {

        // Enqueue a periodic work request

        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
                AppStatusWorker.class,
                15, // Repeat interval in minutes
                TimeUnit.SECONDS,
                5,
                TimeUnit.SECONDS)
                .build();
/*
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "screen.service.tv.app.worker",
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWorkRequest);*/
        //WorkManager.getInstance(this).cancelAllWork();
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