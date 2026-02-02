package com.onscreensync.tvapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onscreensync.tvapp.datamodels.PlaylistItemSerializedDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor() : ViewModel() {

    private val _currentItem = MutableSharedFlow<PlaylistItemSerializedDataModel>()
    val currentItem = _currentItem.asSharedFlow()

    private val _playlistEmpty = MutableStateFlow(false)
    val playlistEmpty = _playlistEmpty.asStateFlow()

    private var items: Array<PlaylistItemSerializedDataModel> = emptyArray()
    private var currentIndex = -1
    private var itemDurationMs: Int = 10000
    private var timerJob: Job? = null

    fun setPlaylist(items: Array<PlaylistItemSerializedDataModel>?, durationMs: Int) {
        if (items.isNullOrEmpty()) {
            _playlistEmpty.value = true
            return
        }
        this.items = items
        this.itemDurationMs = durationMs
        this.currentIndex = -1
        _playlistEmpty.value = false
        playNext()
    }

    fun playNext() {
        timerJob?.cancel()
        if (items.isEmpty()) return

        currentIndex++
        if (currentIndex >= items.size) {
            currentIndex = 0
        }

        val nextItem = items[currentIndex]
        viewModelScope.launch {
            _currentItem.emit(nextItem)
        }
    }

    fun startTimerForCurrentItem(isAutoPlay: Boolean) {
        if (!isAutoPlay) return
        
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            delay(itemDurationMs.toLong())
            playNext()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}