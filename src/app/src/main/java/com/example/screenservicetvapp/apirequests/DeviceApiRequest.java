package com.example.screenservicetvapp.apirequests;

import com.example.screenservicetvapp.apiresponses.DeviceApiResponse;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.GET;

public interface DeviceApiRequest {

    @GET("device/name")
    Call<DeviceApiResponse> getName(@Header("Authorization") String authHeader);
}
