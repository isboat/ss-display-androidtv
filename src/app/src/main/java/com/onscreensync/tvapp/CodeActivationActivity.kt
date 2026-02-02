package com.onscreensync.tvapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.onscreensync.tvapp.databinding.ActivityCodeActivationBinding
import com.onscreensync.tvapp.repository.DeviceRepository
import com.onscreensync.tvapp.viewmodels.CodeActivationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CodeActivationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCodeActivationBinding
    private lateinit var viewModel: CodeActivationViewModel

    @Inject
    lateinit var deviceRepository: DeviceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCodeActivationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(CodeActivationViewModel::class.java)

        setupObservers()
        viewModel.fetchDeviceCode()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.userCode.collect { code ->
                        if (code.isNotEmpty()) {
                            binding.stepTwoCode.text = code
                        }
                    }
                }

                launch {
                    viewModel.verificationUrl.collect { url ->
                        binding.stepOneUrl.text = url
                    }
                }

                launch {
                    viewModel.statusMessage.collect { message ->
                        if (message.isNotEmpty()) {
                            binding.stepTwoCode.text = message
                        }
                    }
                }

                launch {
                    viewModel.navigateToContent.collect {
                        deviceRepository.updateDeviceInfo()
                        navigateToContentActivity()
                    }
                }

                launch {
                    viewModel.error.collect { error ->
                        navigateToErrorActivity(error.first, error.second)
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        finishAffinity()
    }

    private fun navigateToContentActivity() {
        startActivity(Intent(this, ContentActivity::class.java))
        finish()
    }

    private fun navigateToErrorActivity(errorTitle: String, errorMessage: String) {
        val intent = Intent(this, ErrorActivity::class.java).apply {
            putExtra("errorTitle", errorTitle)
            putExtra("errorMessage", errorMessage)
        }
        startActivity(intent)
        finish()
    }
}
