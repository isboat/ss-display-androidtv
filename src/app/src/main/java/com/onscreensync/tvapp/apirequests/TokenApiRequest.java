package com.onscreensync.tvapp.apirequests;

import com.onscreensync.tvapp.apirequests.TokenApiRequestBody;
import com.onscreensync.tvapp.apiresponses.TokenApiResponse;

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
