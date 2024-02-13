package com.onscreensync.tvapp.signalR;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.TransportEnum;

import io.reactivex.rxjava3.core.Single;


public class SignalRService extends Service {

    private static final String TAG = "SignalRService";
    private static final String SIGNALR_ENDPOINT = "Your_SignalR_Endpoint";

    private HubConnection hubConnection;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        hubConnection = HubConnectionBuilder.create(SIGNALR_ENDPOINT)
                .withTransport(TransportEnum.WEBSOCKETS)
                .build();

        startHubConnection();
    }

    private void startHubConnection() {
        hubConnection.start()
                .doOnError(error -> Log.e(TAG, "Error starting SignalR connection: " + error))
                .doFinally(() -> Log.d(TAG, "SignalR connection started"))
                .subscribe();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        hubConnection.stop();
    }

}
