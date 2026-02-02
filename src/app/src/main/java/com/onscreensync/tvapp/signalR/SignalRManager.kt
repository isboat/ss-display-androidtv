package com.onscreensync.tvapp.signalR

import android.util.Log
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.microsoft.signalr.TransportEnum
import com.onscreensync.tvapp.repository.SignalRRepository
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalRManager @Inject constructor(
    private val signalRRepository: SignalRRepository
) {
    private var hubConnection: HubConnection? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var onMessageReceived: ((String) -> Unit)? = null

    fun init(onMessageReceived: (String) -> Unit) {
        this.onMessageReceived = onMessageReceived
        startConnection()
    }

    private fun startConnection() {
        scope.launch {
            val result = signalRRepository.negotiate()
            result.onSuccess { response ->
                val url = response.url
                val accessToken = response.accessToken
                if (url != null && accessToken != null) {
                    setupHubConnection(url, accessToken)
                } else {
                    Log.e("SignalRManager", "Negotiation response contained null url or token")
                    retryConnection()
                }
            }.onFailure { e ->
                Log.e("SignalRManager", "Negotiation failed", e)
                retryConnection()
            }
        }
    }

    private fun setupHubConnection(url: String, accessToken: String) {
        hubConnection = HubConnectionBuilder.create(url)
            .withAccessTokenProvider(Single.just(accessToken))
            .withTransport(TransportEnum.WEBSOCKETS)
            .build()

        hubConnection?.apply {
            serverTimeout = 1000 * 60 * 10 // 10 mins

            on("ReceiveChangeMessage", { message ->
                onMessageReceived?.invoke(message)
            }, String::class.java)

            onClosed { ex ->
                Log.d("SignalRManager", "Connection closed", ex)
                scope.launch {
                    signalRRepository.removeConnection()
                    retryConnection()
                }
            }

            start().doOnComplete {
                Log.d("SignalRManager", "SignalR connected")
                scope.launch {
                    hubConnection?.connectionId?.let { 
                        signalRRepository.addToGroup(it)
                    }
                }
                startKeepAlive()
            }.doOnError { e ->
                Log.e("SignalRManager", "Start error", e)
                retryConnection()
            }.subscribe()
        }
    }

    private fun startKeepAlive() {
        scope.launch {
            while (hubConnection?.connectionState == HubConnectionState.CONNECTED) {
                delay(10000)
                hubConnection?.send("ManualKeepAlive")
            }
        }
    }

    private fun retryConnection() {
        scope.launch {
            delay(5000)
            startConnection()
        }
    }

    fun disconnect() {
        scope.launch {
            signalRRepository.removeConnection()
            hubConnection?.stop()
            hubConnection = null
        }
    }
}
