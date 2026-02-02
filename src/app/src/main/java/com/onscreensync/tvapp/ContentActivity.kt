package com.onscreensync.tvapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.onscreensync.tvapp.apiresponses.ContentDataApiResponse
import com.onscreensync.tvapp.databinding.ActivityContentBinding
import com.onscreensync.tvapp.datamodels.LayoutDataModel
import com.onscreensync.tvapp.datamodels.MenuMetadata
import com.onscreensync.tvapp.datamodels.SignalrReceivedMessage
import com.onscreensync.tvapp.repository.DeviceRepository
import com.onscreensync.tvapp.services.LocalStorageService
import com.onscreensync.tvapp.signalR.SignalRManager
import com.onscreensync.tvapp.utils.JsonUtils
import com.onscreensync.tvapp.viewmodels.ContentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ContentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContentBinding
    private lateinit var viewModel: ContentViewModel

    @Inject
    lateinit var storageService: LocalStorageService

    @Inject
    lateinit var signalRManager: SignalRManager

    @Inject
    lateinit var deviceRepository: DeviceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(ContentViewModel::class.java)

        setupObservers()
        viewModel.start()
        
        setupSignalR()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.deviceName.collect { name ->
                        binding.contentActDeviceNameTextview.text = name
                    }
                }

                launch {
                    viewModel.contentData.collect { responseData ->
                        handleContentData(responseData)
                    }
                }

                launch {
                    viewModel.errorMessage.collect { error ->
                        Log.e("ContentActivity", "${error.first}: ${error.second}")
                        displayNotFoundMessage(error.second)
                    }
                }

                launch {
                    viewModel.navigateToActivation.collect {
                        navigateToCodeActivationScreen()
                    }
                }
            }
        }
    }

    private fun handleContentData(responseData: ContentDataApiResponse) {
        val layout = responseData.layout
        if (layout != null) {
            val intent = when (layout.templateKey) {
                "MenuOverlay" -> createMenuOverlayIntent(responseData)
                "MenuOnly" -> createMenuOnlyIntent(responseData)
                "MediaOnly" -> createMediaOnlyIntent(responseData)
                "Text" -> createTextEditorIntent(responseData)
                "CurrentDateTime" -> createCurrentDateTimeIntent(responseData)
                "MediaPlaylist" -> createPlaylistIntent(responseData)
                else -> {
                    navigateToErrorActivity("No Layout Key", "Layout Key is not set, update screen and republish")
                    null
                }
            }
            intent?.let { startIntentActivity(it) }
        }
    }

    private fun setupSignalR() {
        signalRManager.init { message ->
            runOnUiThread {
                val receivedMessage = JsonUtils.fromJson(message, SignalrReceivedMessage::class.java)
                if (receivedMessage != null) {
                    when (receivedMessage.messageType) {
                        "device.info.update" -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    deviceRepository.updateDeviceInfo()
                                } catch (e: Exception) {
                                    Log.e("ContentActivity", "Error updating device info", e)
                                }
                            }
                        }
                        "content.publish" -> viewModel.loadContentData()
                        "app.restart" -> restartApp()
                        "app.terminate" -> onBackPressed()
                        "app.upgrade.info" -> showToastMessage(receivedMessage.messageData, Color.BLACK)
                        "operator.info" -> showToastMessage(receivedMessage.messageData, Color.RED)
                    }
                }
            }
        }
    }

    private fun showToastMessage(message: String?, color: Int) {
        // ... implementation (assuming custom toast layout exists)
    }

    private fun restartApp() {
        signalRManager.disconnect()
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            finish()
        }, 2000)
    }

    override fun onBackPressed() {
        signalRManager.disconnect()
        Handler(Looper.getMainLooper()).postDelayed({
            finishAffinity()
        }, 2000)
    }

    private fun createMenuOverlayIntent(responseData: ContentDataApiResponse): Intent {
        val menu = responseData.menu
        val menuMetadata = MenuMetadata(menu?.currency, menu?.description, menu?.title, menu?.iconUrl, responseData.layout?.subType)
        return Intent(this, MenuOverlayActivity::class.java).apply {
            putExtra("menuMetadata", menuMetadata)
            putExtra("menuItems", menu?.menuItems)
            putExtra("mediaAsset", responseData.mediaAsset)
            addLayoutPropertiesToIntent(responseData.layout, this)
        }
    }

    private fun createMenuOnlyIntent(responseData: ContentDataApiResponse): Intent {
        val menu = responseData.menu
        val menuMetadata = MenuMetadata(menu?.currency, menu?.description, menu?.title, menu?.iconUrl, responseData.layout?.subType)
        return Intent(this, MenuOnlyActivity::class.java).apply {
            putExtra("menuMetadata", menuMetadata)
            putExtra("menuItems", menu?.menuItems)
            addLayoutPropertiesToIntent(responseData.layout, this)
        }
    }

    private fun createMediaOnlyIntent(responseData: ContentDataApiResponse): Intent =
        Intent(this, MediaOnlyActivity::class.java).apply {
            putExtra("mediaAsset", responseData.mediaAsset)
            putExtra("externalMediaSource", responseData.externalMediaSource)
        }

    private fun createTextEditorIntent(responseData: ContentDataApiResponse): Intent =
        Intent(this, TextEditorActivity::class.java).apply {
            putExtra("textEditorData", responseData.textEditorData)
            addLayoutPropertiesToIntent(responseData.layout, this)
        }

    private fun createCurrentDateTimeIntent(responseData: ContentDataApiResponse): Intent {
        val layout = responseData.layout
        val dateTimeFormat = layout?.subType?.ifEmpty { "EEE, d MMM yyyy HH:mm:ss" } ?: "EEE, d MMM yyyy HH:mm:ss"
        return Intent(this, CurrentDateTimeActivity::class.java).apply {
            putExtra("dateTimeFormat", dateTimeFormat)
            addLayoutPropertiesToIntent(layout, this)
        }
    }

    private fun createPlaylistIntent(responseData: ContentDataApiResponse): Intent {
        val playlistData = responseData.playlistData
        return Intent(this, PlaylistActivity::class.java).apply {
            putExtra("assetItems", playlistData?.items)
            putExtra("itemsSerialized", playlistData?.itemsSerialized)
            putExtra("itemDuration", playlistData?.itemDuration)
        }
    }

    private fun addLayoutPropertiesToIntent(layout: LayoutDataModel?, intent: Intent) {
        layout?.templateProperties?.forEach { property ->
            intent.putExtra(property.key, property.value)
        }
    }

    private fun startIntentActivity(intent: Intent) {
        startActivity(intent)
        finish()
    }

    private fun navigateToErrorActivity(title: String, message: String) {
        val intent = Intent(this, ErrorActivity::class.java).apply {
            putExtra("errorTitle", title)
            putExtra("errorMessage", message)
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToCodeActivationScreen() {
        startActivity(Intent(this, CodeActivationActivity::class.java))
        finish()
    }

    private fun displayNotFoundMessage(errorCode: String) {
        val displayMsg = when (errorCode) {
            "no_such_device" -> {
                navigateToCodeActivationScreen()
                "No Such Device Found"
            }
            "no_screen_id" -> "No Screen Attached"
            "no_screen_data_found" -> "No Screen Data Found"
            else -> "Error occurred"
        }
        binding.contentMessageTxt.text = displayMsg
        val deviceName = if (errorCode == "no_such_device") "..." else storageService.getData(Constants.DEVICE_NAME)
        navigateToContentMessageInfoActivity(displayMsg, deviceName)
    }

    private fun navigateToContentMessageInfoActivity(message: String, deviceName: String?) {
        val intent = Intent(this, ContentInfoMessageActivity::class.java).apply {
            putExtra("message", message)
            putExtra("deviceName", deviceName)
        }
        startIntentActivity(intent)
    }
}
