package com.onscreensync.tvapp.services;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.onscreensync.tvapp.Constants;
import com.onscreensync.tvapp.DisplayApiConfigConstants;
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
    private LocalStorageService storageService;

    private AccessTokenService accessTokenService;
    private Context context;
    private MutableLiveData<DeviceApiResponse> data;

    public DeviceService(Context context) {
        this.context = context;
        this.loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Choose the desired log level

        // Create an instance of OkHttpClient with the interceptor
        this.okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        this.storageService = new LocalStorageService(context);
        this.accessTokenService = new AccessTokenService(context);
    }

    public MutableLiveData<DeviceApiResponse> updateDeviceInfo() {
        String accessToken = this.storageService.getAccessToken();
        if(data == null) {
            data = new MutableLiveData<>();
        }

        if(ObjectExtensions.isNullOrEmpty(accessToken))
        {
            data.postValue(null);
            return data;
        };

        LifecycleOwner lifecycleOwner = (LifecycleOwner) this.context;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(this.storageService.getData(DisplayApiConfigConstants.BASEURL))
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        String url = this.storageService.getData(DisplayApiConfigConstants.DEVICE_INFO_URL);
        DeviceApiRequest deviceApiRequest = retrofit.create(DeviceApiRequest.class);
        Call<DeviceApiResponse> call = deviceApiRequest.getName(url, "Bearer " + accessToken);

        call.enqueue(new Callback<DeviceApiResponse>() {
            @Override
            public void onResponse(Call<DeviceApiResponse> call, Response<DeviceApiResponse> response) {
                if (response.isSuccessful()) {

                    DeviceApiResponse responseData = response.body();
                    storageService.setData(Constants.DEVICE_NAME, responseData.getName());
                    storageService.setData(Constants.DEVICE_ID, responseData.getId());
                    storageService.setData(Constants.TENANT_ID, responseData.getTenantId());

                    data.postValue(response.body());

                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        String error = jObjError.getString("error");
                        Log.d("ContentActErrorTest", error);
                    } catch (Exception e) {
                        Log.d("CodeActivationJSON", e.getMessage());
                    }

                    int responseCode = response.code();
                    if (responseCode == 401) {
                        accessTokenService.refreshAccessToken().observe(lifecycleOwner, deviceApiResponse -> {
                            if(deviceApiResponse != null)
                            {
                                if(!ObjectExtensions.isNullOrEmpty(deviceApiResponse.getAccessToken()))
                                {
                                    updateDeviceInfo();
                                }
                            }
                        });
                    }
                    else {
                        data.postValue(null);
                    }
                }
            }

            @Override
            public void onFailure(Call<DeviceApiResponse> call, Throwable t) {
                data.postValue(null);
            }
        });

        return data;
    }
}
