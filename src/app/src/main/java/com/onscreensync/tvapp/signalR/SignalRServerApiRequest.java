package com.onscreensync.tvapp.signalR;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Query;
import retrofit2.http.POST;

public interface SignalRServerApiRequest {
    @POST("signalr/negotiate")
    Call<NegotiateApiResponse> negotiate(@Query("deviceId") String deviceId, @Header("Authorization") String authHeader);
}
