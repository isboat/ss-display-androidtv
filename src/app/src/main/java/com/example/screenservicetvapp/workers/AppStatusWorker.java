package com.example.screenservicetvapp.workers;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.screenservicetvapp.MainActivity;

public class AppStatusWorker extends Worker {

    public AppStatusWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("AppStatusWorker", "doWork");
        // Perform your periodic task here
        String response = performGetRequest();

        // Check the response and launch the app if needed
        if ("launch".equals(response)) {
            launchApp();
        }

        return Result.success(); // or Result.failure() in case of an error
    }

    private String performGetRequest() {
        Log.d("AppStatusWorker", "performGetRequest");
        // Implement your logic to make a GET request using HttpURLConnection or other HTTP client
        // and return the response
        // For simplicity, a dummy response is used here
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            //throw new RuntimeException(e);
        }
        return "launch";
    }

    private void launchApp() {
        Log.d("AppStatusWorker", "launchApp");
        // Launch your app or specific activity here
        Context appContent = this.getApplicationContext();
        Intent launchIntent = new Intent(appContent, MainActivity.class);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appContent.startActivity(launchIntent);
    }
}