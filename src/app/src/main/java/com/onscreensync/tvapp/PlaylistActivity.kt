package com.onscreensync.tvapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.onscreensync.tvapp.databinding.ActivityMediaPlaylistBinding
import com.onscreensync.tvapp.datamodels.MediaAssetDataModel
import com.onscreensync.tvapp.datamodels.PlaylistItemSerializedDataModel
import com.onscreensync.tvapp.datamodels.TextADInformationAsset
import com.onscreensync.tvapp.signalR.SignalRManager
import com.onscreensync.tvapp.utils.JsonUtils
import com.onscreensync.tvapp.utils.ObjectExtensions
import com.onscreensync.tvapp.utils.UiHelper
import com.onscreensync.tvapp.viewmodels.PlaylistViewModel
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaylistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaPlaylistBinding
    private lateinit var viewModel: PlaylistViewModel

    @Inject
    lateinit var signalRManager: SignalRManager

    private var mediaController: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(PlaylistViewModel::class.java)

        setupUI()
        setupObservers()

        val itemsSerialized = ObjectExtensions.getParcelableArrayExtra(intent, "itemsSerialized", PlaylistItemSerializedDataModel::class.java)
        val durationMs = parseItemDuration(intent.getStringExtra("itemDuration"))
        
        viewModel.setPlaylist(itemsSerialized, durationMs)
    }

    private fun setupUI() {
        mediaController = MediaController(this)
        mediaController?.setAnchorView(binding.mediaPlaylistVideoAssetView)
        binding.mediaPlaylistVideoAssetView.setMediaController(mediaController)
        
        binding.mediaPlaylistVideoAssetView.setOnCompletionListener { viewModel.playNext() }
        binding.mediaPlaylistVideoAssetView.setOnPreparedListener { mp -> mp.isLooping = false }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentItem.collect { item ->
                        displayItem(item)
                    }
                }

                launch {
                    viewModel.playlistEmpty.collect { isEmpty ->
                        if (isEmpty) {
                            Toast.makeText(this@PlaylistActivity, "No item in the playlist, please add items and republish.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun displayItem(currentItem: PlaylistItemSerializedDataModel) {
        binding.mediaPlaylistActivityAdsTextview.visibility = View.INVISIBLE
        binding.mediaPlaylistImageAsset.visibility = View.INVISIBLE
        binding.mediaPlaylistVideoAssetView.visibility = View.INVISIBLE

        when (currentItem.key) {
            "AssetItemModel" -> runAssetItemModel(currentItem)
            "TextAssetItemModel" -> runTextAssetItemModel(currentItem)
            else -> {
                Toast.makeText(this, "No such Item type.", Toast.LENGTH_LONG).show()
                viewModel.playNext()
            }
        }
    }

    private fun runAssetItemModel(itemSerialized: PlaylistItemSerializedDataModel) {
        val asset = JsonUtils.fromJson(itemSerialized.value, MediaAssetDataModel::class.java) ?: return
        
        when (asset.type) {
            1 -> { // Image
                binding.mediaPlaylistImageAsset.visibility = View.VISIBLE
                Picasso.get().load(asset.assetUrl).into(binding.mediaPlaylistImageAsset)
                viewModel.startTimerForCurrentItem(true)
            }
            2 -> { // Video
                binding.mediaPlaylistVideoAssetView.visibility = View.VISIBLE
                binding.mediaPlaylistVideoAssetView.setVideoPath(asset.assetUrl)
                binding.mediaPlaylistVideoAssetView.start()
                viewModel.startTimerForCurrentItem(false) // Video completion triggers next
            }
            else -> viewModel.playNext()
        }
    }

    private fun runTextAssetItemModel(itemSerialized: PlaylistItemSerializedDataModel) {
        val textAsset = JsonUtils.fromJson(itemSerialized.value, TextADInformationAsset::class.java) ?: return
        
        binding.mediaPlaylistActivityAdsTextview.apply {
            visibility = View.VISIBLE
            text = Html.fromHtml(textAsset.description ?: "")
            UiHelper.setTextViewFont(this, textAsset.textFont)
            UiHelper.setTextViewColor(this, textAsset.textColor)
            setBackgroundColor(UiHelper.parseColor(textAsset.backgroundColor))
        }
        viewModel.startTimerForCurrentItem(true)
    }

    private fun parseItemDuration(itemDuration: String?): Int {
        if (itemDuration.isNullOrEmpty()) return 10000
        val timeParts = itemDuration.split(":")
        if (timeParts.size != 3) return 10000

        val hr = ObjectExtensions.convertToInt(timeParts[0])
        val min = ObjectExtensions.convertToInt(timeParts[1])
        val sec = ObjectExtensions.convertToInt(timeParts[2])

        return (hr * 3600 + min * 60 + sec) * 1000
    }

    override fun onBackPressed() {
        signalRManager.disconnect()
        Handler(Looper.getMainLooper()).postDelayed({
            finishAffinity()
        }, 2000)
    }
}
