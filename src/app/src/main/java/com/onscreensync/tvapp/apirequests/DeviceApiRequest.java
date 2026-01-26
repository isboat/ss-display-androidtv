package com.onscreensync.tvapp.apirequests;

import com.onscreensync.tvapp.apiresponses.DeviceApiResponse;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface DeviceApiRequest {

    @GET("{fullUrl}")
    Call<DeviceApiResponse> getName(@Path(value = "fullUrl", encoded = true) String fullUrl, @Header("Authorization") String authHeader);
}
