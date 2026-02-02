package com.onscreensync.tvapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.onscreensync.tvapp.databinding.ActivityErrorBinding
import com.onscreensync.tvapp.signalR.SignalRManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ErrorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityErrorBinding

    @Inject
    lateinit var signalRManager: SignalRManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityErrorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val errorTitle = intent.getStringExtra("errorTitle")
        val errorMessage = intent.getStringExtra("errorMessage")

        binding.errorPageTitleTxt.text = errorTitle
        binding.errorPageErrorMessageTxt.setText(errorMessage)
        binding.errorPageErrorMessageTxt.isFocusable = false
    }

    override fun onBackPressed() {
        signalRManager.disconnect()

        Handler(Looper.getMainLooper()).postDelayed({
            finishAffinity()
        }, 2000)
    }
}
