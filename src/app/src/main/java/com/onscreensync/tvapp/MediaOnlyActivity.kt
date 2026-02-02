package com.onscreensync.tvapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.onscreensync.tvapp.databinding.ActivityMediaOnlyBinding
import com.onscreensync.tvapp.datamodels.MediaAssetDataModel
import com.onscreensync.tvapp.fragments.ExternalMediaFragment
import com.onscreensync.tvapp.fragments.ImageMediaFragment
import com.onscreensync.tvapp.fragments.VideoMediaFragment
import com.onscreensync.tvapp.signalR.SignalRManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MediaOnlyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaOnlyBinding

    @Inject
    lateinit var signalRManager: SignalRManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaOnlyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mediaAsset = intent.getParcelableExtra<MediaAssetDataModel>("mediaAsset")
        val externalMediaSource = intent.getStringExtra("externalMediaSource")

        if (!externalMediaSource.isNullOrEmpty()) {
            loadExternalMediaFragment(externalMediaSource)
        } else {
            mediaAsset?.let {
                when (it.type) {
                    1 -> loadImageMediaFragment(it.assetUrl)
                    2 -> loadVideoMediaFragment(it.assetUrl)
                    else -> Log.d("MediaOnlyActivity", "No such media type")
                }
            }
        }
    }

    override fun onBackPressed() {
        signalRManager.disconnect()
        Handler(Looper.getMainLooper()).postDelayed({
            finishAffinity()
        }, 2000)
    }

    private fun loadVideoMediaFragment(assetUrl: String?) {
        val bundle = Bundle().apply {
            putString("assetUrl", assetUrl)
        }
        loadFragment(VideoMediaFragment(), bundle)
    }

    private fun loadImageMediaFragment(assetUrl: String?) {
        val bundle = Bundle().apply {
            putString("assetUrl", assetUrl)
        }
        loadFragment(ImageMediaFragment(), bundle)
    }

    private fun loadExternalMediaFragment(externalMediaSource: String) {
        val bundle = Bundle().apply {
            putString("externalMediaSource", externalMediaSource)
        }
        loadFragment(ExternalMediaFragment(), bundle)
    }

    private fun loadFragment(fragment: Fragment, bundle: Bundle) {
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.media_only_framelayout, fragment)
            .addToBackStack(null)
            .commit()
    }
}
