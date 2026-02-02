package com.onscreensync.tvapp.fragments

import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.onscreensync.tvapp.databinding.FragmentBasicMenuBinding
import com.onscreensync.tvapp.datamodels.MenuItemDataModel
import com.onscreensync.tvapp.datamodels.MenuMetadata
import com.onscreensync.tvapp.utils.ObjectExtensions
import com.onscreensync.tvapp.utils.UiHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BasicMenuFragment : Fragment() {

    private var _binding: FragmentBasicMenuBinding? = null
    private val binding get() = _binding!!

    private var menuMetadata: MenuMetadata? = null
    private var textFont: String? = null
    private var textColor: String? = null
    private var backgroundColor: String? = null
    private var menuItems: Array<MenuItemDataModel>? = null

    private var setTransparentBackground: Boolean = false
    private var backgroundOpacity: String? = null

    companion object {
        private val STRIKE_THROUGH_SPAN = StrikethroughSpan()
        
        @JvmStatic
        fun newInstance() = BasicMenuFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            textFont = bundle.getString("textFont")
            textColor = bundle.getString("textColor")
            backgroundColor = bundle.getString("backgroundColor")
            setTransparentBackground = bundle.getBoolean("setTransparentBackground", false)
            backgroundOpacity = bundle.getString("backgroundOpacity")

            menuMetadata = bundle.getParcelable("menuMetadata")
            menuItems = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelableArray("menuItems", MenuItemDataModel::class.java)
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelableArray("menuItems") as? Array<MenuItemDataModel>
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBasicMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        menuItems?.let { items ->
            if (items.isNotEmpty()) {
                createMenu()
            }
        }

        if (setTransparentBackground) {
            val opacityInt = ObjectExtensions.convertToInt(backgroundOpacity)
            if (opacityInt > 0) {
                binding.root.background?.alpha = opacityInt + 150
            } else if (opacityInt == 0) {
                binding.root.background?.alpha = 0
            }
        }
    }

    private fun createMenu() {
        val metadata = menuMetadata ?: return
        val displayMenuTitle = metadata.title.isNullOrEmpty().not()
        
        if (displayMenuTitle) {
            binding.basicMenuFragmentTitle.text = metadata.title
            UiHelper.setTextViewColor(binding.basicMenuFragmentTitle, textColor)
        } else {
            binding.basicMenuFragmentTitle.visibility = View.INVISIBLE
        }

        menuItems?.forEach { obj ->
            val tableRow = TableRow(requireContext()).apply {
                setPadding(100, 5, 5, 5)
            }

            val nameTextView = createTextView(obj.name).apply {
                setPadding(20, 0, 0, 0)
                UiHelper.setTextViewFont(this, textFont)
                UiHelper.setTextViewColor(this, textColor)
            }

            val priceTextView = createPriceTextView(obj.price, obj.discountPrice).apply {
                UiHelper.setTextViewFont(this, textFont)
                UiHelper.setTextViewColor(this, textColor)
            }

            val descTextView = createTextView(obj.description).apply {
                UiHelper.setTextViewFont(this, textFont)
                UiHelper.setTextViewColor(this, textColor)
            }

            tableRow.addView(nameTextView)
            tableRow.addView(priceTextView)
            tableRow.addView(descTextView)

            binding.basicMenuFragmentTableLayout.addView(tableRow)
        }
    }

    private fun createPriceTextView(price: String?, discountPrice: String?): TextView {
        val currency = menuMetadata?.currency ?: ""
        val priceText = price ?: ""
        if (discountPrice.isNullOrEmpty()) {
            return createTextView("$currency$priceText")
        }

        val displayText = "$currency$priceText $currency$discountPrice"
        return createTextView(displayText).apply {
            setText(displayText, TextView.BufferType.SPANNABLE)
            val spannable = text as Spannable
            spannable.setSpan(STRIKE_THROUGH_SPAN, 0, priceText.length + currency.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun createTextView(text: String?): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            layoutParams = TableRow.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
