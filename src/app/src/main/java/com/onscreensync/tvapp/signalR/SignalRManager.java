package com.onscreensync.tvapp.signalR;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.TransportEnum;

import io.reactivex.rxjava3.core.Single;

public class SignalRManager {
    private HubConnection hubConnection;

    public SignalRManager(String signalREndpoint, String accessToken) {
        hubConnection = HubConnectionBuilder.create(signalREndpoint)
                .withAccessTokenProvider(Single.defer(() -> {
                    // Your logic here.
                    return Single.just(accessToken);
                }))
                .withTransport(TransportEnum.WEBSOCKETS)
                .build();
    }

    public void start() {
        hubConnection.start().blockingAwait();
    }

    public void stop() {
        hubConnection.stop();
    }

    public HubConnection getHubConnection() {
        return this.hubConnection;
    }
    /*
    * hubConnection.on("ReceiveMessage", (message) ->{

        }, String.class);
    * */
}
