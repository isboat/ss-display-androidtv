package com.example.screenservicetvapp;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DeviceService {

    public DeviceService(Context context) {
        this.loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Choose the desired log level

        // Create an instance of OkHttpClient with the interceptor
        this.okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        storageService = new LocalStorageService(context);
    }
    private HttpLoggingInterceptor loggingInterceptor;
    private OkHttpClient okHttpClient;

    private LocalStorageService storageService;

    public void updateName() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.ENDPOINT_BASEURL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();



        DeviceApiRequest deviceApiRequest = retrofit.create(DeviceApiRequest.class);
        Call<DeviceApiResponse> call = deviceApiRequest.getName("Bearer " + storageService.getAccessToken());

        call.enqueue(new Callback<DeviceApiResponse>() {
            @Override
            public void onResponse(Call<DeviceApiResponse> call, Response<DeviceApiResponse> response) {
                if (response.isSuccessful()) {
                    DeviceApiResponse responseData = response.body();
                    storageService.setData(Constants.DEVICE_NAME, responseData.getName());
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
