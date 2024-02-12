package com.onscreensync.tvapp.apirequests;
import com.onscreensync.tvapp.apiresponses.CodeActivationApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface CodeActivationApiRequest {
    @POST("device/code")
    Call<CodeActivationApiResponse> deviceCode(@Body CodeActivationRequestBody requestBody);
}
