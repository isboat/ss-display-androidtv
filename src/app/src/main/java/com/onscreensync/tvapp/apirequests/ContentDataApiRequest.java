package com.onscreensync.tvapp.apirequests;

import com.onscreensync.tvapp.apiresponses.ContentDataApiResponse;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ContentDataApiRequest {
    //@GET("content/data")
    @GET("{fullUrl}")
    Call<ContentDataApiResponse> getData(@Path(value = "fullUrl", encoded = true) String fullUrl, @Header("Authorization") String authHeader);
}
