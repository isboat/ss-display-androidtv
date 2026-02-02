package com.onscreensync.tvapp.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import androidx.fragment.app.Fragment
import com.onscreensync.tvapp.databinding.FragmentExternalMediaBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExternalMediaFragment : Fragment() {

    private var _binding: FragmentExternalMediaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExternalMediaBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            val externalMediaSource = bundle.getString("externalMediaSource")
            if (!externalMediaSource.isNullOrEmpty()) {
                binding.externalMediaWebView.apply {
                    webChromeClient = WebChromeClient()
                    settings.apply {
                        builtInZoomControls = true
                        javaScriptEnabled = true
                    }
                    loadUrl(externalMediaSource)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String?, param2: String?) = ExternalMediaFragment()
    }
}