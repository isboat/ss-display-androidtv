package com.onscreensync.tvapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.fragment.app.Fragment
import com.onscreensync.tvapp.databinding.FragmentVideoMediaBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoMediaFragment : Fragment() {

    private var _binding: FragmentVideoMediaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoMediaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        arguments?.let { bundle ->
            val assetUrl = bundle.getString("assetUrl")
            if (!assetUrl.isNullOrEmpty()) {
                setupVideoView(assetUrl)
            }
        }
    }

    private fun setupVideoView(assetUrl: String) {
        val mediaController = MediaController(requireContext())
        mediaController.setAnchorView(binding.mediaVideoAssetView)
        
        binding.mediaVideoAssetView.apply {
            setMediaController(mediaController)
            
            setOnCompletionListener { 
                start() // seamless loop
            }
            
            setOnPreparedListener { mp ->
                mp.isLooping = true
            }
            
            setVideoPath(assetUrl)
            start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String?, param2: String?) = VideoMediaFragment()
    }
}