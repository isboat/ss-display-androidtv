package com.example.screenservicetvapp;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TokenStorageService storageService = new TokenStorageService(this);

        // Retrieve access token
        String accessToken = storageService.getAccessToken();
        Log.d("MainActivity", "accessToken: " + accessToken);
        // Assume you want to start AnotherActivity when a certain condition is met
        if (accessToken == null) {
            this.navigateToCodeActivationScreen();
        }
/*
        listFragment = new ListFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.list_fragment, listFragment);
        transaction.commit();*/
    }

    private void navigateToCodeActivationScreen() {
        Intent intent = new Intent(this, CodeActivationActivity.class);

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