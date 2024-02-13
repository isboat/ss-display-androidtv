package com.onscreensync.tvapp.signalR;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Query;
import retrofit2.http.POST;

public interface SignalRServerApiRequest {
    @POST("signalr/negotiate")
    Call<NegotiateApiResponse> negotiate(@Query("deviceId") String deviceId, @Header("Authorization") String authHeader);

    @POST("signalr/add-to-group")
    Call<AddToGroupApiResponse> addToGroup(
            @Query("deviceId") String deviceId,
            @Query("connectionId") String connectionId,
            @Header("Authorization") String authHeader);
}
