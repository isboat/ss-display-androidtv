package com.onscreensync.tvapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.onscreensync.tvapp.databinding.ActivityContentnfoMessageBinding
import com.onscreensync.tvapp.signalR.SignalRManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ContentInfoMessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContentnfoMessageBinding

    @Inject
    lateinit var signalRManager: SignalRManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentnfoMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val message = intent.getStringExtra("message")
        val deviceName = intent.getStringExtra("deviceName")

        binding.activityContentInfoMessageTextview.text = message
        binding.activityContentInfoSubtextTextview.text = deviceName
    }

    override fun onBackPressed() {
        signalRManager.disconnect()

        Handler(Looper.getMainLooper()).postDelayed({
            finishAffinity()
        }, 2000)
    }
}
