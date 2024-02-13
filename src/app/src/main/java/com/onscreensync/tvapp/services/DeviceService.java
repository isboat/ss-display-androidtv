package com.onscreensync.tvapp.services;

import android.content.Context;
import android.util.Log;

import com.onscreensync.tvapp.Constants;
import com.onscreensync.tvapp.ContentActivity;
import com.onscreensync.tvapp.apirequests.DeviceApiRequest;
import com.onscreensync.tvapp.apiresponses.DeviceApiResponse;
import com.onscreensync.tvapp.utils.ObjectExtensions;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DeviceService {
    private HttpLoggingInterceptor loggingInterceptor;
    private OkHttpClient okHttpClient;
    private String accessToken;
    private LocalStorageService storageService;

    public DeviceService(String accessToken, Context context) {
        this.loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Choose the desired log level

        // Create an instance of OkHttpClient with the interceptor
        this.okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
        this.storageService = new LocalStorageService(context);
        this.accessToken = accessToken;
    }

    public void updateDeviceInfo() {
        if(ObjectExtensions.isNullOrEmpty(accessToken)) return;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.ENDPOINT_BASEURL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Log.d("DeviceService", "retrofit");

        DeviceApiRequest deviceApiRequest = retrofit.create(DeviceApiRequest.class);
        Log.d("DeviceService", "deviceApiRequest");
        Call<DeviceApiResponse> call = deviceApiRequest.getName("Bearer " + accessToken);

        call.enqueue(new Callback<DeviceApiResponse>() {
            @Override
            public void onResponse(Call<DeviceApiResponse> call, Response<DeviceApiResponse> response) {
                if (response.isSuccessful()) {
                    DeviceApiResponse responseData = response.body();
                    storageService.setData(Constants.DEVICE_NAME, responseData.getName());
                    storageService.setData(Constants.DEVICE_ID, responseData.getId());
                    storageService.setData(Constants.TENANT_ID, responseData.getTenantId());
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        String error = jObjError.getString("error");
                        Log.d("ContentActErrorTest", error);
                    } catch (Exception e) {
                        Log.d("CodeActivationJSON", e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<DeviceApiResponse> call, Throwable t) {
                // Handle API request failure
            }
        });
    }
}
