package com.onscreensync.tvapp.apirequests;

import com.onscreensync.tvapp.apiresponses.ContentDataApiResponse;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.GET;

public interface ContentDataApiRequest {
    @GET("content/data")
    Call<ContentDataApiResponse> getData(@Header("Authorization") String authHeader);
}
