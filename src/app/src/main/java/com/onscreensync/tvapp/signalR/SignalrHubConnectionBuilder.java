package com.onscreensync.tvapp.signalR;

import android.content.Context;
import android.util.Log;

import com.microsoft.signalr.Action;
import com.microsoft.signalr.Action1;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.microsoft.signalr.TransportEnum;
import com.onscreensync.tvapp.Constants;
import com.onscreensync.tvapp.services.LocalStorageService;
import com.onscreensync.tvapp.utils.ObjectExtensions;

import io.reactivex.rxjava3.core.Single;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignalrHubConnectionBuilder {
    private static final String TAG = "SignalrHubConnection";

    private HubConnection hubConnection;
    private OkHttpClient okHttpClient;
    private HttpLoggingInterceptor loggingInterceptor;
    private String deviceId;
    private String accessToken;
    private Action1<String> onReceiveMessageAction;
    private Retrofit retrofit;

    public SignalrHubConnectionBuilder(String accessToken, String deviceId, Action1<String> onReceiveMsgAction) {
        this.deviceId = deviceId;
        this.accessToken = accessToken;

        this.onReceiveMessageAction = onReceiveMsgAction;

        this.loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Choose the desired log level

        // Create an instance of OkHttpClient with the interceptor
        this.okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.ENDPOINT_BASEURL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.initializeNegotiation();
    }

    public void initializeNegotiation() {

        SignalRServerApiRequest activationApiRequest = retrofit.create(SignalRServerApiRequest.class);
        Call<NegotiateApiResponse> call = activationApiRequest.negotiate(deviceId, "Bearer " + accessToken);

        call.enqueue(new Callback<NegotiateApiResponse>() {
            @Override
            public void onResponse(Call<NegotiateApiResponse> call, Response<NegotiateApiResponse> response) {
                if (response.isSuccessful()) {
                    NegotiateApiResponse responseData = response.body();
                    if(responseData != null) {
                        initializeHubConnection(responseData.getUrl(), responseData.getAccessToken());
                    }
                    else {
                        //userCodeTextView.setText("Status Error: ResponseData is null");
                    }
                } else {
                    // Handle unsuccessful API request
                    //userCodeTextView.setText("Status Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<NegotiateApiResponse> call, Throwable t) {
                // Handle API request failure
            }
        });
    }

    public void addToGroup() {
        if(this.hubConnection != null) {

            String connectionId = this.hubConnection.getConnectionId();
            HubConnectionState connectionState = this.hubConnection.getConnectionState();

            if(connectionState == HubConnectionState.CONNECTED && !ObjectExtensions.isNullOrEmpty(connectionId)) {

                SignalRServerApiRequest activationApiRequest = retrofit.create(SignalRServerApiRequest.class);
                Call<AddToGroupApiResponse> call = activationApiRequest.addToGroup(deviceId, connectionId, "Bearer " + accessToken);

                call.enqueue(new Callback<AddToGroupApiResponse>() {
                    @Override
                    public void onResponse(Call<AddToGroupApiResponse> call, Response<AddToGroupApiResponse> response) {
                        if (response.isSuccessful()) {

                        } else {
                            // Handle unsuccessful API request
                        }
                    }

                    @Override
                    public void onFailure(Call<AddToGroupApiResponse> call, Throwable t) {
                        // Handle API request failure
                    }
                });
            }
        }
    }
    private void initializeHubConnection(String signalrEndpoint, String accessToken)
    {
        HubConnection hubConnection = HubConnectionBuilder.create(signalrEndpoint)
                .withAccessTokenProvider(Single.defer(() -> {
                    // Your logic here.
                    return Single.just(accessToken);
                }))
                .withTransport(TransportEnum.WEBSOCKETS)
                .build();

        this.hubConnection = hubConnection;

        this.hubConnection.start()
                .doOnError(error -> Log.e(TAG, "Error starting SignalR connection: " + error))
                .doOnComplete(() -> {
                    Log.d(TAG, "SignalR connection started");
                    addToGroup();
                })
                //.doFinally(() -> Log.d(TAG, "SignalR connection starting"))
                .subscribe();

        this.hubConnection.on("ReceiveChangeMessage", this.onReceiveMessageAction, String.class);

        this.hubConnection.onClosed((ex) -> {
            if (ex != null) {
                ex.printStackTrace();
                Log.d(TAG, "SignalR Closed with errors. " + ex);
            }
        });
    }
}
