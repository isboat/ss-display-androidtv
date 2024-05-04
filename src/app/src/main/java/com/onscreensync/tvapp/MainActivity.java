package com.onscreensync.tvapp;


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

import com.onscreensync.tvapp.apirequests.CodeActivationApiRequest;
import com.onscreensync.tvapp.apirequests.CodeActivationRequestBody;
import com.onscreensync.tvapp.apirequests.ConfigApiRequest;
import com.onscreensync.tvapp.apiresponses.CodeActivationApiResponse;
import com.onscreensync.tvapp.apiresponses.ConfigApiResponse;
import com.onscreensync.tvapp.apiresponses.configs.DisplayApiConfig;
import com.onscreensync.tvapp.services.DeviceService;
import com.onscreensync.tvapp.services.LocalStorageService;
import com.onscreensync.tvapp.utils.ObjectExtensions;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends FragmentActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;
    private LocalStorageService storageService;
    private HttpLoggingInterceptor loggingInterceptor;
    private OkHttpClient okHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(R.style.Theme_ScreenServiceTVApp);
        setContentView(R.layout.activity_main);

        this.loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Choose the desired log level

        // Create an instance of OkHttpClient with the interceptor
        this.okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        requestBootCompletedPermission();
        this.storageService = new LocalStorageService(this);

        this.loadApiConfig();

        /*Handler handler = new Handler();
        final Runnable r = () -> startRun();
        handler.postDelayed(r, 3000);*/
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
        System.exit(0);
    }

    private void loadApiConfig() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.ONSCREENSYNC_ENDPOINT_BASEURL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ConfigApiRequest configApiRequest = retrofit.create(ConfigApiRequest.class);
        Call<ConfigApiResponse> call = configApiRequest.loadConfig();

        call.enqueue(new Callback<ConfigApiResponse>() {
            @Override
            public void onResponse(Call<ConfigApiResponse> call, Response<ConfigApiResponse> response) {
                if (response.isSuccessful()) {
                    ConfigApiResponse responseData = response.body();
                    if(responseData != null) {
                        DisplayApiConfig displayApiConfig = responseData.getDisplayApiConfig();
                        //Store device name
                        storageService.setData(DisplayApiConfigConstants.BASEURL, displayApiConfig.getBaseEndpoint());                        storageService.setData(DisplayApiConfigConstants.CONTENT_DATA_URL, displayApiConfig.getContentDataUrl());
                        storageService.setData(DisplayApiConfigConstants.DEVICE_CODE_URL, displayApiConfig.getDeviceCodeUrl());
                        storageService.setData(DisplayApiConfigConstants.DEVICE_INFO_URL, displayApiConfig.getDeviceInfoUrl());
                        storageService.setData(DisplayApiConfigConstants.DEVICE_TOKEN_REQUEST_URL, displayApiConfig.getDeviceTokenRequestUrl());
                        storageService.setData(DisplayApiConfigConstants.DEVICE_REFRESH_TOKEN_REQUEST_URL, displayApiConfig.getDeviceRefreshTokenRequestUrl());
                        storageService.setData(DisplayApiConfigConstants.SIGNALR_ADD_CONNECTION_URL, displayApiConfig.getSignalrAddConnectionUrl());
                        storageService.setData(DisplayApiConfigConstants.SIGNALR_NEGOTIATION_URL, displayApiConfig.getSignalrNegotiationUrl());

                        storageService.setData(DisplayApiConfigConstants.SIGNALR_REMOVE_CONNECTION_URL, displayApiConfig.getSignalrRemoveConnectionUrl());
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Get Config Error, ConfigApiResponse is NULL", Toast.LENGTH_LONG).show();
                    }

                    Handler handler = new Handler();
                    final Runnable r = () -> startRun();
                    handler.postDelayed(r, 3000);


                } else {
                    // Handle unsuccessful API request
                    Toast.makeText(MainActivity.this, "Get Config Error, Status Error: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ConfigApiResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Get Config Error, no internet connection", Toast.LENGTH_LONG).show();
            }
        });
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