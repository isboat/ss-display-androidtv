package com.onscreensync.tvapp.signalR;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.microsoft.signalr.Action1;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.microsoft.signalr.TransportEnum;
import com.onscreensync.tvapp.Constants;
import com.onscreensync.tvapp.apiresponses.DeviceApiResponse;
import com.onscreensync.tvapp.apiresponses.TokenApiResponse;
import com.onscreensync.tvapp.services.AccessTokenService;
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
    private static SignalrHubConnectionBuilder selfInstance;
    private String hubConnectionId;
    private OkHttpClient okHttpClient;
    private HttpLoggingInterceptor loggingInterceptor;
    private Context context;
    private LocalStorageService storageService;
    private AccessTokenService accessTokenService;


    private Action1<String> onReceiveMessageAction;
    private Retrofit retrofit;

    private Handler handler = new Handler();

    public SignalrHubConnectionBuilder(Context context, Action1<String> onReceiveMsgAction) {
        this.context = context;
        this.storageService = new LocalStorageService(context);
        this.accessTokenService = new AccessTokenService(context);

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
        selfInstance = this;
    }
    // Public static method to get the singleton instance
    public static synchronized SignalrHubConnectionBuilder getInstance() {
        return selfInstance;
    }

    public void initializeNegotiation() {
        String deviceId = storageService.getData((Constants.DEVICE_ID));

        Log.d(TAG, "initializeNegotiation called");
        SignalRServerApiRequest signalRServerApiRequest = retrofit.create(SignalRServerApiRequest.class);
        Call<NegotiateApiResponse> call = signalRServerApiRequest.negotiate(deviceId, "Bearer " + this.storageService.getAccessToken());

        LifecycleOwner lifecycleOwner = (LifecycleOwner) this.context;
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
                        Log.d(TAG, "initializeNegotiation " + "Status Error: ResponseData is null");
                    }
                } else {
                    // Handle unsuccessful API request
                    int responseCode = response.code();
                    Log.d(TAG, "initializeNegotiation " + "Status Error: " + response.code());
                    if (responseCode == 401) {
                        accessTokenService.refreshAccessToken().observe(lifecycleOwner, deviceApiResponse -> {
                            if(deviceApiResponse != null)
                            {
                                if(!ObjectExtensions.isNullOrEmpty(deviceApiResponse.getAccessToken()))
                                {
                                    initializeNegotiation();
                                } else {
                                    Log.d(TAG, "initializeNegotiation " + "device api response getAccessToken is null");
                                }
                            }
                            else {
                                Log.d(TAG, "initializeNegotiation " + "device api response is null");
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<NegotiateApiResponse> call, Throwable t) {
                // Handle API request failure
                Log.d(TAG, "initializeNegotiation " + "Handle API request failure");
            }
        });
    }

    public void addToGroup() {
        if(this.hubConnection != null) {
            Log.d(TAG, "addToGroup called");
            String deviceId = storageService.getData(Constants.DEVICE_ID);
            String deviceName = storageService.getData(Constants.DEVICE_NAME);

            String connectionId = this.hubConnection.getConnectionId();
            this.hubConnectionId = connectionId;
            storageService.setData(Constants.CONNECTION_ID, connectionId);

            HubConnectionState connectionState = this.hubConnection.getConnectionState();

            if(connectionState == HubConnectionState.CONNECTED && !ObjectExtensions.isNullOrEmpty(connectionId)) {

                Log.d(TAG, "addToGroup: connectionState" + connectionState);
                SignalRServerApiRequest activationApiRequest = retrofit.create(SignalRServerApiRequest.class);
                Call<AddToGroupApiResponse> call = activationApiRequest.addToGroup(deviceId, deviceName, connectionId, "Bearer " + this.storageService.getAccessToken());

                call.enqueue(new Callback<AddToGroupApiResponse>() {
                    @Override
                    public void onResponse(Call<AddToGroupApiResponse> call, Response<AddToGroupApiResponse> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "addToGroup: response.isSuccessful");

                        } else {
                            // Handle unsuccessful API request
                            Log.d(TAG, "addToGroup: unsuccessful API request");
                        }
                    }

                    @Override
                    public void onFailure(Call<AddToGroupApiResponse> call, Throwable t) {
                        // Handle API request failure
                        Log.d(TAG, "addToGroup: Handle API request failure");
                    }
                });
            }

            Log.d(TAG, "addToGroup call completed");
        }
    }

    public void removeConnectionFromGroup() {

        String deviceId = storageService.getData(Constants.DEVICE_ID);
        String deviceName = storageService.getData(Constants.DEVICE_NAME);

        String connectionId = this.hubConnectionId;
        if(ObjectExtensions.isNullOrEmpty(connectionId)) {
            connectionId = storageService.getData(Constants.CONNECTION_ID);
        }
        SignalRServerApiRequest activationApiRequest = retrofit.create(SignalRServerApiRequest.class);
        Call<RemoveConnectionApiResponse> call = activationApiRequest.removeConnection(deviceId, deviceName, connectionId, "Bearer " + this.storageService.getAccessToken());

        call.enqueue(new Callback<RemoveConnectionApiResponse>() {
            @Override
            public void onResponse(Call<RemoveConnectionApiResponse> call, Response<RemoveConnectionApiResponse> response) {

            }

            @Override
            public void onFailure(Call<RemoveConnectionApiResponse> call, Throwable t) {

            }
        });
    }

    private void manualKeepAlive()
    {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Call your method here
                if(hubConnection != null) {
                    HubConnectionState connectionState = hubConnection.getConnectionState();
                    if(connectionState != HubConnectionState.CONNECTED && connectionState != HubConnectionState.CONNECTING) {
                        initializeNegotiation();
                    }
                    else {
                        hubConnection.send("ManualKeepAlive");
                    }
                }
                else {
                    Log.d(TAG, "ManualKeepAlive inside else hubconnection is null");
                }

                // Schedule the next execution
                handler.postDelayed(this, 10000); // 1000 milliseconds (1 second)
            }
        }, 10000); // Initial delay of 1000 milliseconds (1 second)


    }

    private void initializeHubConnection(String signalrEndpoint, String accessToken)
    {
        Log.d(TAG, "initializeHubConnection called");
        HubConnection hubConnection = HubConnectionBuilder.create(signalrEndpoint)
                .withAccessTokenProvider(Single.defer(() -> {
                    // Your logic here.
                    return Single.just(accessToken);
                }))
                .withTransport(TransportEnum.WEBSOCKETS)
                .build();

        hubConnection.setServerTimeout(1000 * 60 * 10); // 10mins

        this.hubConnection = hubConnection;

        this.hubConnection.start()
                .doOnError(error -> Log.e(TAG, "Error starting SignalR connection: " + error))
                .doOnComplete(() -> {
                    Log.d(TAG, "SignalR connection started");
                    addToGroup();
                    manualKeepAlive();
                })
                //.doFinally(() -> Log.d(TAG, "SignalR connection starting"))
                .subscribe();

        this.hubConnection.on("ReceiveChangeMessage", this.onReceiveMessageAction, String.class);

        this.hubConnection.onClosed((ex) -> {

            removeConnectionFromGroup();

            if (ex != null) {
                ex.printStackTrace();
                Log.d(TAG, "SignalR Closed with errors. " + ex);
                if(ex.toString().contains("Connection reset")) {
                    Log.d(TAG, "Re-negotiation triggered.");
                    initializeNegotiation();
                }
            }
            else
            {
                Log.d(TAG, "SignalR connection on closed");
            }
        });
    }
}
