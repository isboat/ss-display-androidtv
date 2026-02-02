package com.onscreensync.tvapp.fragments

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.onscreensync.tvapp.R
import com.onscreensync.tvapp.databinding.FragmentTextAdInformationBinding
import com.onscreensync.tvapp.utils.UiHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TextADInformationFragment : Fragment() {

    private var _binding: FragmentTextAdInformationBinding? = null
    private val binding get() = _binding!!

    private var backgroundColor: String? = null
    private var description: String? = null
    private var name: String? = null
    private var textColor: String? = null
    private var textFont: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            description = bundle.getString("description")
            backgroundColor = bundle.getString("backgroundColor")
            name = bundle.getString("name")
            textColor = bundle.getString("textColor")
            textFont = bundle.getString("textFont")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTextAdInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val displayDescription = if (description.isNullOrEmpty()) {
            "Error: No text found in the data, republish."
        } else {
            description!!
        }

        binding.textAdInformationTextview.apply {
            text = Html.fromHtml(displayDescription)
            UiHelper.setTextViewFont(this, textFont)
            UiHelper.setTextViewColor(this, textColor)
        }

        binding.root.setBackgroundColor(UiHelper.parseColor(backgroundColor))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String?, param2: String?) = TextADInformationFragment()
    }
}