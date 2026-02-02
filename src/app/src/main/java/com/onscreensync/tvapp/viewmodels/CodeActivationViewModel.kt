package com.onscreensync.tvapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onscreensync.tvapp.Constants
import com.onscreensync.tvapp.DisplayApiConfigConstants
import com.onscreensync.tvapp.apirequests.CodeActivationApiRequest
import com.onscreensync.tvapp.apirequests.CodeActivationRequestBody
import com.onscreensync.tvapp.apirequests.TokenApiRequest
import com.onscreensync.tvapp.apirequests.TokenApiRequestBody
import com.onscreensync.tvapp.apiresponses.CodeActivationApiResponse
import com.onscreensync.tvapp.services.LocalStorageService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import javax.inject.Inject

@HiltViewModel
class CodeActivationViewModel @Inject constructor(
    private val activationApiRequest: CodeActivationApiRequest,
    private val tokenApiRequest: TokenApiRequest,
    private val storageService: LocalStorageService
) : ViewModel() {

    private val _userCode = MutableStateFlow("")
    val userCode = _userCode.asStateFlow()

    private val _verificationUrl = MutableStateFlow("")
    val verificationUrl = _verificationUrl.asStateFlow()

    private val _deviceName = MutableStateFlow("")
    val deviceName = _deviceName.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage = _statusMessage.asStateFlow()

    private val _navigateToContent = MutableSharedFlow<Unit>()
    val navigateToContent = _navigateToContent.asSharedFlow()

    private val _error = MutableSharedFlow<Pair<String, String>>()
    val error = _error.asSharedFlow()

    private var pollingJob: Job? = null

    fun fetchDeviceCode() {
        val url = storageService.getData(DisplayApiConfigConstants.DEVICE_CODE_URL) ?: return
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = activationApiRequest.deviceCode(url, CodeActivationRequestBody("clientid", "user_code")).awaitResponse()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        storageService.setData(Constants.DEVICE_NAME, body.deviceName)
                        _deviceName.value = body.deviceName ?: ""
                        _userCode.value = body.userCode ?: ""
                        _verificationUrl.value = body.verificationUrl ?: ""
                        
                        startPollingStatus(body)
                    }
                } else {
                    _statusMessage.value = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.emit("Network Error" to (e.message ?: "Unknown error"))
            }
        }
    }

    private fun startPollingStatus(activationData: CodeActivationApiResponse) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch(Dispatchers.IO) {
            val url = storageService.getData(DisplayApiConfigConstants.DEVICE_TOKEN_REQUEST_URL) ?: return@launch
            var retryCount = 0
            val maxRetries = 100
            val interval = (activationData.interval ?: 5).toLong() * 1000

            while (retryCount < maxRetries) {
                try {
                    val response = tokenApiRequest.tokenRequest(
                        url,
                        TokenApiRequestBody(activationData.clientId, "string", activationData.deviceCode, "urn:ietf:params:oauth:grant-type:access_token")
                    ).awaitResponse()

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            storageService.accessToken = body.accessToken
                            storageService.refreshToken = body.refreshToken
                            _navigateToContent.emit(Unit)
                            return@launch
                        }
                    } else {
                        when (response.code()) {
                            428 -> { // authorization_pending
                                delay(interval)
                                retryCount++
                            }
                            else -> {
                                _error.emit("Activation Error" to "Status: ${response.code()}")
                                return@launch
                            }
                        }
                    }
                } catch (e: Exception) {
                    _error.emit("Polling Error" to (e.message ?: "Unknown error"))
                    return@launch
                }
            }
            _statusMessage.value = "Error: Maximum retry count reached"
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}