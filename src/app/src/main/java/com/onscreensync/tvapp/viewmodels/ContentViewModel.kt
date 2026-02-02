package com.onscreensync.tvapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onscreensync.tvapp.Constants
import com.onscreensync.tvapp.apiresponses.ContentDataApiResponse
import com.onscreensync.tvapp.repository.ContentRepository
import com.onscreensync.tvapp.services.LocalStorageService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContentViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val storageService: LocalStorageService
) : ViewModel() {

    private val _contentData = MutableSharedFlow<ContentDataApiResponse>()
    val contentData = _contentData.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<Pair<String, String>>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _deviceName = MutableStateFlow("")
    val deviceName = _deviceName.asStateFlow()

    private val _navigateToActivation = MutableSharedFlow<Unit>()
    val navigateToActivation = _navigateToActivation.asSharedFlow()

    private var isActive = false

    fun start() {
        _deviceName.value = storageService.getData(Constants.DEVICE_NAME) ?: ""
        loadContentData()
    }

    fun loadContentData() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = contentRepository.getContentData()
            result.onSuccess { responseData ->
                val checksum = responseData.checksum
                val storedChecksum = storageService.getData(Constants.CHECKSUM_DATA_KEY)

                if (storedChecksum == checksum && isActive) return@onSuccess

                storageService.setData(Constants.CHECKSUM_DATA_KEY, checksum)
                isActive = true
                _contentData.emit(responseData)
            }.onFailure { exception ->
                _errorMessage.emit("Content Error" to (exception.message ?: "Unknown error"))
            }
        }
    }
}