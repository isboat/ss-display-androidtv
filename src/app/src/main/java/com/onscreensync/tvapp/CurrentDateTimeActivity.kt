package com.onscreensync.tvapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import com.onscreensync.tvapp.databinding.ActivityDateTimeBinding
import com.onscreensync.tvapp.signalR.SignalRManager
import com.onscreensync.tvapp.utils.UiHelper
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class CurrentDateTimeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDateTimeBinding
    private var dateTimeFormat: String = "EEE, d MMM yyyy, HH:mm:ss"
    private val handler = Handler(Looper.getMainLooper())
    
    @Inject
    lateinit var signalRManager: SignalRManager

    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            displayCurrentDateTime()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDateTimeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.getStringExtra("dateTimeFormat")?.let {
            if (it.isNotEmpty()) dateTimeFormat = it
        }
        
        val textColor = intent.getStringExtra("textColor")
        val textFont = intent.getStringExtra("textFont")

        textFont?.let {
            UiHelper.setTextViewFont(binding.datetimeActivityTextView, it)
        }

        textColor?.let {
            UiHelper.setTextViewColor(binding.datetimeActivityTextView, it)
        }

        handler.post(updateTimeRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimeRunnable)
    }

    override fun onBackPressed() {
        signalRManager.disconnect()

        handler.postDelayed({
            finishAffinity()
        }, 2000)
    }

    private fun displayCurrentDateTime() {
        try {
            val sdf = SimpleDateFormat(dateTimeFormat, Locale.getDefault())
            val date = sdf.format(Calendar.getInstance().time)
            binding.datetimeActivityTextView.text = Html.fromHtml(date)
        } catch (e: Exception) {
            binding.datetimeActivityTextView.text = "Invalid Format"
        }
    }
}
