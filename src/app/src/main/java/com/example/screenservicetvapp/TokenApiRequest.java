package com.example.screenservicetvapp;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface TokenApiRequest {

    @POST("device/token")
    Call<TokenApiResponse> tokenRequest(@Body TokenApiRequestBody requestBody);

    @POST("device/token")
    Call<TokenApiResponse> refreshTokenRequest(@Body TokenApiRequestBody requestBody, @Header("Authorization") String authHeader);
}
