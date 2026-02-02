package com.onscreensync.tvapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onscreensync.tvapp.DisplayApiConfigConstants
import com.onscreensync.tvapp.apirequests.ConfigApiRequest
import com.onscreensync.tvapp.repository.DeviceRepository
import com.onscreensync.tvapp.services.LocalStorageService
import com.onscreensync.tvapp.utils.isNullOrEmpty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val configApiRequest: ConfigApiRequest,
    private val deviceRepository: DeviceRepository,
    private val storageService: LocalStorageService
) : ViewModel() {

    private val _configLoaded = MutableStateFlow(false)
    val configLoaded = _configLoaded.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _navigateToActivation = MutableSharedFlow<Unit>()
    val navigateToActivation = _navigateToActivation.asSharedFlow()

    private val _navigateToContent = MutableSharedFlow<Unit>()
    val navigateToContent = _navigateToContent.asSharedFlow()

    fun loadApiConfig() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = configApiRequest.loadConfig().awaitResponse()
                if (response.isSuccessful) {
                    val responseData = response.body()
                    if (responseData != null) {
                        val displayApiConfig = responseData.displayApiConfig
                        if (displayApiConfig != null) {
                            storageService.setData(DisplayApiConfigConstants.BASEURL, displayApiConfig.baseEndpoint)
                            storageService.setData(DisplayApiConfigConstants.CONTENT_DATA_URL, displayApiConfig.contentDataUrl)
                            storageService.setData(DisplayApiConfigConstants.DEVICE_CODE_URL, displayApiConfig.deviceCodeUrl)
                            storageService.setData(DisplayApiConfigConstants.DEVICE_INFO_URL, displayApiConfig.deviceInfoUrl)
                            storageService.setData(DisplayApiConfigConstants.DEVICE_TOKEN_REQUEST_URL, displayApiConfig.deviceTokenRequestUrl)
                            storageService.setData(DisplayApiConfigConstants.DEVICE_REFRESH_TOKEN_REQUEST_URL, displayApiConfig.deviceRefreshTokenRequestUrl)
                            storageService.setData(DisplayApiConfigConstants.SIGNALR_ADD_CONNECTION_URL, displayApiConfig.signalrAddConnectionUrl)
                            storageService.setData(DisplayApiConfigConstants.SIGNALR_NEGOTIATION_URL, displayApiConfig.signalrNegotiationUrl)
                            storageService.setData(DisplayApiConfigConstants.SIGNALR_REMOVE_CONNECTION_URL, displayApiConfig.signalrRemoveConnectionUrl)

                            _configLoaded.value = true
                        } else {
                            _errorMessage.emit("Get Config Error, displayApiConfig is NULL")
                        }
                    } else {
                        _errorMessage.emit("Get Config Error, ConfigApiResponse is NULL")
                    }
                } else {
                    _errorMessage.emit("Get Config Error, Status Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.emit("Get Config Error, no internet connection")
            }
        }
    }

    fun startRun() {
        val accessToken = storageService.accessToken
        if (accessToken.isNullOrEmpty()) {
            viewModelScope.launch {
                _navigateToActivation.emit(Unit)
            }
        } else {
            viewModelScope.launch {
                deviceRepository.updateDeviceInfo()
                _navigateToContent.emit(Unit)
            }
        }
    }
}