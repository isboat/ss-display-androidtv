package com.onscreensync.tvapp.services;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.onscreensync.tvapp.Constants;
import com.onscreensync.tvapp.apirequests.DeviceApiRequest;
import com.onscreensync.tvapp.apirequests.TokenApiRequest;
import com.onscreensync.tvapp.apirequests.TokenApiRequestBody;
import com.onscreensync.tvapp.apiresponses.DeviceApiResponse;
import com.onscreensync.tvapp.apiresponses.TokenApiResponse;
import com.onscreensync.tvapp.utils.ObjectExtensions;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AccessTokenService {
    private static final String TAG = "AccessTokenService";
    private HttpLoggingInterceptor loggingInterceptor;
    private OkHttpClient okHttpClient;
    private LocalStorageService storageService;

    public AccessTokenService(Context content) {
        this.loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Choose the desired log level

        // Create an instance of OkHttpClient with the interceptor
        this.okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        this.storageService = new LocalStorageService(content);
    }

    public MutableLiveData<TokenApiResponse> refreshAccessToken() {
        final MutableLiveData<TokenApiResponse> data = new MutableLiveData<>();
        String refreshToken = storageService.getRefreshToken();
        if(refreshToken == null) {
            data.postValue(new TokenApiResponse());
            return data;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.ENDPOINT_BASEURL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TokenApiRequest tokenApiRequest = retrofit.create(TokenApiRequest.class);
        Call<TokenApiResponse> call = tokenApiRequest.refreshTokenRequest(
                new TokenApiRequestBody("", "string", "", Constants.TOKEN_REFRESH_GRANT_TYPE), "Bearer " + refreshToken);

        call.enqueue(new Callback<TokenApiResponse>() {
            @Override
            public void onResponse(Call<TokenApiResponse> call, Response<TokenApiResponse> response) {
                if (response.isSuccessful()) {
                    TokenApiResponse responseData = response.body();
                    storageService.setAccessToken(responseData.getAccessToken());
                    storageService.setRefreshToken(responseData.getRefreshToken());

                    data.postValue(responseData);

                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        String error = jObjError.getString("error");
                        Log.d(TAG, error);
                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                    data.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<TokenApiResponse> call, Throwable t) {
                // Handle API request failure
                data.postValue(null);
            }
        });

        return data;
    }
}
