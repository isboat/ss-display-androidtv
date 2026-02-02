package com.onscreensync.tvapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import com.onscreensync.tvapp.databinding.ActivityTextEditorBinding
import com.onscreensync.tvapp.signalR.SignalRManager
import com.onscreensync.tvapp.utils.UiHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TextEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTextEditorBinding

    @Inject
    lateinit var signalRManager: SignalRManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val textEditorData = intent.getStringExtra("textEditorData") ?: "Error: No text found in the data, republish."
        val textColor = intent.getStringExtra("textColor")
        val textFont = intent.getStringExtra("textFont")
        val backgroundColor = intent.getStringExtra("backgroundColor")

        binding.textEditorActivityTextView.apply {
            text = Html.fromHtml(textEditorData)
            
            textFont?.let { 
                UiHelper.setTextViewFont(this, it)
            }
            
            textColor?.let {
                UiHelper.setTextViewColor(this, it)
            }
            
            backgroundColor?.let {
                setBackgroundColor(UiHelper.parseColor(it))
            }
        }
    }

    override fun onBackPressed() {
        signalRManager.disconnect()
        Handler(Looper.getMainLooper()).postDelayed({
            finishAffinity()
        }, 2000)
    }
}