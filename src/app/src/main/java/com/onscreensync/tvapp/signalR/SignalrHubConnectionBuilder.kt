package com.onscreensync.tvapp.signalR

import android.content.Context
import android.os.Handler
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.microsoft.signalr.Action1
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.microsoft.signalr.TransportEnum
import com.onscreensync.tvapp.Constants
import com.onscreensync.tvapp.DisplayApiConfigConstants
import com.onscreensync.tvapp.repository.AuthRepository
import com.onscreensync.tvapp.services.LocalStorageService
import com.onscreensync.tvapp.utils.ObjectExtensions
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SignalrHubConnectionBuilder(private val context: Context, private val onReceiveMessageAction: Action1<String>, private val authRepository: AuthRepository) {
    private var hubConnection: HubConnection? = null
    private var hubConnectionId: String? = null
    private val okHttpClient: OkHttpClient
    private val storageService: LocalStorageService = LocalStorageService(context)
    private val retrofit: Retrofit
    private val handler = Handler()

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        this.okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(this.storageService.getData(DisplayApiConfigConstants.BASEURL) ?: "https://default.url")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        this.initializeNegotiation()
        selfInstance = this
    }

    companion object {
        private const val TAG = "SignalrHubConnection"
        private var selfInstance: SignalrHubConnectionBuilder? = null

        @JvmStatic
        @Synchronized
        fun getInstance(): SignalrHubConnectionBuilder? {
            return selfInstance
        }
    }

    fun initializeNegotiation() {
        val deviceId = storageService.getData(Constants.DEVICE_ID) ?: ""
        Log.d(TAG, "initializeNegotiation called")
        val url = storageService.getData(DisplayApiConfigConstants.SIGNALR_NEGOTIATION_URL) ?: ""
        if (url.isEmpty()) {
            Log.e(TAG, "initializeNegotiation: SignalR negotiation URL is empty")
            return
        }
        val signalRServerApiRequest = retrofit.create(SignalRServerApiRequest::class.java)
        val call = signalRServerApiRequest.negotiate(url, deviceId, "Bearer ${storageService.accessToken}")

        val lifecycleOwner = context as? LifecycleOwner
        call.enqueue(object : Callback<NegotiateApiResponse> {
            override fun onResponse(call: Call<NegotiateApiResponse>, response: Response<NegotiateApiResponse>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    if (responseData != null) {
                        initializeHubConnection(responseData.url ?: "", responseData.accessToken ?: "")
                    } else {
                        Log.d(TAG, "initializeNegotiation Status Error: ResponseData is null")
                    }
                } else {
                    val responseCode = response.code()
                    Log.d(TAG, "initializeNegotiation Status Error: $responseCode")
                    if (responseCode == 401 && lifecycleOwner != null) {
                        lifecycleOwner.lifecycleScope.launch {
                            val result = authRepository.refreshAccessToken()
                            result.onSuccess {
                                initializeNegotiation()
                            }.onFailure {
                                Log.e(TAG, "Failed to refresh token: ${it.message}")
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<NegotiateApiResponse>, t: Throwable) {
                Log.d(TAG, "initializeNegotiation Handle API request failure: ${t.message}")
            }
        })
    }

    fun addToGroup() {
        hubConnection?.let { connection ->
            Log.d(TAG, "addToGroup called")
            val deviceId = storageService.getData(Constants.DEVICE_ID) ?: ""
            val deviceName = storageService.getData(Constants.DEVICE_NAME) ?: ""

            val connectionId = connection.connectionId
            this.hubConnectionId = connectionId
            storageService.setData(Constants.CONNECTION_ID, connectionId)

            val connectionState = connection.connectionState

            if (connectionState == HubConnectionState.CONNECTED && !ObjectExtensions.isNullOrEmpty(connectionId)) {
                Log.d(TAG, "addToGroup: connectionState$connectionState")
                val url = storageService.getData(DisplayApiConfigConstants.SIGNALR_ADD_CONNECTION_URL) ?: ""
                val activationApiRequest = retrofit.create(SignalRServerApiRequest::class.java)
                val call = activationApiRequest.addToGroup(url, deviceId, deviceName, connectionId ?: "", "Bearer ${storageService.accessToken}")

                call.enqueue(object : Callback<AddToGroupApiResponse> {
                    override fun onResponse(call: Call<AddToGroupApiResponse>, response: Response<AddToGroupApiResponse>) {
                        if (response.isSuccessful) {
                            Log.d(TAG, "addToGroup: response.isSuccessful")
                        } else {
                            Log.d(TAG, "addToGroup: unsuccessful API request")
                        }
                    }

                    override fun onFailure(call: Call<AddToGroupApiResponse>, t: Throwable) {
                        Log.d(TAG, "addToGroup: Handle API request failure")
                    }
                })
            }
            Log.d(TAG, "addToGroup call completed")
        }
    }

    fun removeConnectionFromGroup() {
        val deviceId = storageService.getData(Constants.DEVICE_ID) ?: ""
        val deviceName = storageService.getData(Constants.DEVICE_NAME) ?: ""

        var connectionId = hubConnectionId
        if (ObjectExtensions.isNullOrEmpty(connectionId)) {
            connectionId = storageService.getData(Constants.CONNECTION_ID)
        }
        val url = storageService.getData(DisplayApiConfigConstants.SIGNALR_REMOVE_CONNECTION_URL) ?: ""
        val activationApiRequest = retrofit.create(SignalRServerApiRequest::class.java)
        val call = activationApiRequest.removeConnection(url, deviceId, deviceName, connectionId ?: "", "Bearer ${storageService.accessToken}")

        call.enqueue(object : Callback<RemoveConnectionApiResponse> {
            override fun onResponse(call: Call<RemoveConnectionApiResponse>, response: Response<RemoveConnectionApiResponse>) {}
            override fun onFailure(call: Call<RemoveConnectionApiResponse>, t: Throwable) {}
        })
    }

    private fun manualKeepAlive() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                hubConnection?.let { connection ->
                    val connectionState = connection.connectionState
                    if (connectionState != HubConnectionState.CONNECTED && connectionState != HubConnectionState.CONNECTING) {
                        initializeNegotiation()
                    } else {
                        if (connectionState == HubConnectionState.CONNECTED) {
                            connection.send("ManualKeepAlive")
                        }
                    }
                } ?: Log.d(TAG, "ManualKeepAlive inside else hubconnection is null")

                handler.postDelayed(this, 10000)
            }
        }, 10000)
    }

    private fun initializeHubConnection(signalrEndpoint: String, accessToken: String) {
        Log.d(TAG, "initializeHubConnection called")
        val hubConnection = HubConnectionBuilder.create(signalrEndpoint)
            .withAccessTokenProvider(Single.defer { Single.just(accessToken) })
            .withTransport(TransportEnum.WEBSOCKETS)
            .build()

        hubConnection.serverTimeout = 1000L * 60 * 10 // 10mins

        this.hubConnection = hubConnection

        this.hubConnection?.start()
            ?.doOnError { error -> Log.e(TAG, "Error starting SignalR connection: $error") }
            ?.doOnComplete {
                Log.d(TAG, "SignalR connection started")
                addToGroup()
                manualKeepAlive()
            }
            ?.subscribe()

        this.hubConnection?.on("ReceiveChangeMessage", onReceiveMessageAction, String::class.java)

        this.hubConnection?.onClosed { ex ->
            removeConnectionFromGroup()
            if (ex != null) {
                ex.printStackTrace()
                Log.d(TAG, "SignalR Closed with errors. $ex")
                if (ex.toString().contains("Connection reset")) {
                    Log.d(TAG, "Re-negotiation triggered.")
                    initializeNegotiation()
                }
            } else {
                Log.d(TAG, "SignalR connection on closed")
            }
        }
    }
}
