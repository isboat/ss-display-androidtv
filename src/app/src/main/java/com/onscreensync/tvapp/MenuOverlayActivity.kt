package com.onscreensync.tvapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.onscreensync.tvapp.databinding.ActivityMenuOverlayBinding
import com.onscreensync.tvapp.datamodels.MediaAssetDataModel
import com.onscreensync.tvapp.datamodels.MenuItemDataModel
import com.onscreensync.tvapp.datamodels.MenuMetadata
import com.onscreensync.tvapp.fragments.BasicMenuFragment
import com.onscreensync.tvapp.fragments.ImageMediaFragment
import com.onscreensync.tvapp.fragments.VideoMediaFragment
import com.onscreensync.tvapp.signalR.SignalRManager
import com.onscreensync.tvapp.utils.ObjectExtensions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MenuOverlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuOverlayBinding
    
    @Inject
    lateinit var signalRManager: SignalRManager

    private var menuItems: Array<MenuItemDataModel>? = null
    private var menuMetadata: MenuMetadata? = null
    private var textFont: String? = null
    private var textColor: String? = null
    private var backgroundOpacity: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuOverlayBinding.inflate(getLayoutInflater())
        setContentView(binding.root)

        // Retrieve the Intent that started this activity
        menuMetadata = intent.getParcelableExtra("menuMetadata")
        textColor = intent.getStringExtra("textColor")
        textFont = intent.getStringExtra("textFont")
        backgroundOpacity = intent.getStringExtra("backgroundOpacity")
        menuItems = ObjectExtensions.getParcelableArrayExtra(intent, "menuItems", MenuItemDataModel::class.java)
        
        loadBasicMenuFragment()

        val mediaAsset: MediaAssetDataModel? = intent.getParcelableExtra("mediaAsset")
        mediaAsset?.let { asset ->
            when (asset.type) {
                1 -> loadImageMediaFragment(asset.assetUrl)
                2 -> loadVideoMediaFragment(asset.assetUrl)
                else -> Log.d("MenuOverlayActivity", "No such media type")
            }
        }
    }

    override fun onBackPressed() {
        signalRManager.disconnect()
        Handler(Looper.getMainLooper()).postDelayed({
            finishAffinity()
        }, 2000)
    }

    private fun loadBasicMenuFragment() {
        val metadata = menuMetadata ?: return
        val items = menuItems ?: return

        val bundle = Bundle().apply {
            putParcelable("menuMetadata", metadata)
            putParcelableArray("menuItems", items)
            putString("textColor", textColor)
            putString("textFont", textFont)
            putString("backgroundOpacity", backgroundOpacity)
            putBoolean("setTransparentBackground", true)
        }

        loadFragment(BasicMenuFragment.newInstance(), bundle, R.id.menu_overlay_activity_menu_frameLayout)
    }

    private fun loadImageMediaFragment(assetUrl: String?) {
        val bundle = Bundle().apply {
            putString("assetUrl", assetUrl)
        }
        loadFragment(ImageMediaFragment(), bundle, R.id.menu_overlay_activity_media_frameLayout)
    }

    private fun loadVideoMediaFragment(assetUrl: String?) {
        val bundle = Bundle().apply {
            putString("assetUrl", assetUrl)
        }
        loadFragment(VideoMediaFragment(), bundle, R.id.menu_overlay_activity_media_frameLayout)
    }

    private fun loadFragment(fragment: Fragment, bundle: Bundle, elementId: Int) {
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(elementId, fragment)
            .addToBackStack(null)
            .commit()
    }
}