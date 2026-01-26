package com.onscreensync.tvapp.signalR;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.POST;

public interface SignalRServerApiRequest {
    //@POST("signalr/negotiate")
    @POST("{fullUrl}")
    Call<NegotiateApiResponse> negotiate(
            @Path(value = "fullUrl", encoded = true) String fullUrl,
            @Query("deviceId") String deviceId,
            @Header("Authorization") String authHeader);

    //@POST("signalr/add-connection")
    @POST("{fullUrl}")
    Call<AddToGroupApiResponse> addToGroup(
            @Path(value = "fullUrl", encoded = true) String fullUrl,
            @Query("deviceId") String deviceId,
            @Query("deviceName") String deviceName,
            @Query("connectionId") String connectionId,
            @Header("Authorization") String authHeader);

    //@POST("signalr/remove-connection")
    @POST("{fullUrl}")
    Call<RemoveConnectionApiResponse> removeConnection(
            @Path(value = "fullUrl", encoded = true) String fullUrl,
            @Query("deviceId") String deviceId,
            @Query("deviceName") String deviceName,
            @Query("connectionId") String connectionId,
            @Header("Authorization") String authHeader);
}
