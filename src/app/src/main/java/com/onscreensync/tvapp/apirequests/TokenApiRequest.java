package com.onscreensync.tvapp.apirequests;

import com.onscreensync.tvapp.apirequests.TokenApiRequestBody;
import com.onscreensync.tvapp.apiresponses.TokenApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface TokenApiRequest {

    //@POST("device/token")
    @POST("{fullUrl}")
    Call<TokenApiResponse> tokenRequest(@Path(value = "fullUrl", encoded = true) String fullUrl, @Body TokenApiRequestBody requestBody);

    //@POST("device/token")
    @POST("{fullUrl}")
    Call<TokenApiResponse> refreshTokenRequest(@Path(value = "fullUrl", encoded = true) String fullUrl, @Body TokenApiRequestBody requestBody, @Header("Authorization") String authHeader);
}
