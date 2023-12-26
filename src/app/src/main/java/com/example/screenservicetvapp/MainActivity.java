package com.example.screenservicetvapp;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalStorageService storageService = new LocalStorageService(this);

        // Retrieve access token
        String accessToken = storageService.getAccessToken();
        Log.d("MainActivity", "accessToken: " + accessToken);
        // Assume you want to start AnotherActivity when a certain condition is met
        if (accessToken == null) {
            this.navigateToCodeActivationScreen();
        } else {
            this.navigateToContentScreen();
        }
    }

    private void navigateToCodeActivationScreen() {
        Intent intent = new Intent(this, CodeActivationActivity.class);

        // You can also pass data to the new activity using putExtra
        intent.putExtra("key", "value");

        // Start the new activity
        startActivity(intent);

        finish(); // Close the current activity
    }

    private void navigateToContentScreen() {
        Intent intent = new Intent(this, ContentActivity.class);

        // You can also pass data to the new activity using putExtra
        intent.putExtra("key", "value");

        // Start the new activity
        startActivity(intent);

        finish(); // Close the current activity
    }

    private String getTokenFromLocalStorage() {
        // Implement your logic to retrieve the token from local storage
        // For simplicity, returning a hardcoded value here
        return "sample_token";
    }

}