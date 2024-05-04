package com.onscreensync.tvapp.apirequests;
import com.onscreensync.tvapp.apiresponses.CodeActivationApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CodeActivationApiRequest {
    //@POST("device/code")
    @POST("{fullUrl}")
    Call<CodeActivationApiResponse> deviceCode(@Path(value = "fullUrl", encoded = true) String fullUrl, @Body CodeActivationRequestBody requestBody);
}
