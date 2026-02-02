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
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.onscreensync.tvapp.R
import com.onscreensync.tvapp.databinding.FragmentPremiumMenuBinding
import com.onscreensync.tvapp.datamodels.MenuItemDataModel
import com.onscreensync.tvapp.datamodels.MenuMetadata
import com.onscreensync.tvapp.utils.ObjectExtensions
import com.onscreensync.tvapp.utils.UiHelper
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PremiumMenuFragment : Fragment() {

    private var _binding: FragmentPremiumMenuBinding? = null
    private val binding get() = _binding!!

    private var menuMetadata: MenuMetadata? = null
    private var textFont: String? = null
    private var textColor: String? = null
    private var menuItems: Array<MenuItemDataModel>? = null

    private var setTransparentBackground: Boolean = false
    private var backgroundOpacity: String? = null

    companion object {
        private val STRIKE_THROUGH_SPAN = StrikethroughSpan()
        
        @JvmStatic
        fun newInstance() = PremiumMenuFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            textFont = bundle.getString("textFont")
            textColor = bundle.getString("textColor")
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
        _binding = FragmentPremiumMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        menuItems?.let { items ->
            if (items.isNotEmpty()) {
                createMenu()
            }
        }

        if (setTransparentBackground && !backgroundOpacity.isNullOrEmpty()) {
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
        if (!metadata.iconUrl.isNullOrEmpty()) {
            Picasso.get().load(metadata.iconUrl).into(binding.premiumMenuFragmentMenuIconImageView)
        } else {
            binding.premiumMenuFragmentMenuIconImageView.visibility = View.GONE
        }

        menuItems?.forEach { obj ->
            val tableRow = TableRow(requireContext())

            val imageView = createItemIcon(obj.iconUrl)

            val nameTextView = createTextView(obj.name).apply {
                UiHelper.setTextViewFont(this, textFont)
                UiHelper.setTextViewColor(this, textColor)
                setBackgroundResource(R.drawable.premium_border)
            }

            val priceTextView = createPriceTextView(obj.price ?: "", obj.discountPrice).apply {
                UiHelper.setTextViewFont(this, textFont)
                UiHelper.setTextViewColor(this, textColor)
            }

            val descTextView = createTextView(obj.description).apply {
                UiHelper.setTextViewFont(this, textFont)
                UiHelper.setTextViewColor(this, textColor)
                setBackgroundResource(R.drawable.premium_border)
            }

            tableRow.addView(imageView)
            tableRow.addView(nameTextView)
            tableRow.addView(priceTextView)
            tableRow.addView(descTextView)

            binding.premiumMenuFragmentTableLayout.addView(tableRow)
        }
    }

    private fun createItemIcon(iconUrl: String?): ImageView {
        return ImageView(requireContext()).apply {
            if (!iconUrl.isNullOrEmpty()) Picasso.get().load(iconUrl).into(this)
            layoutParams = TableRow.LayoutParams(150, 150).apply {
                gravity = Gravity.CENTER
            }
            setPadding(16, 16, 16, 16)
        }
    }

    private fun createPriceTextView(price: String, discountPrice: String?): TextView {
        val currency = menuMetadata?.currency ?: ""
        if (discountPrice.isNullOrEmpty()) {
            return createTextView("$currency$price")
        }

        val priceVal = if (price.isEmpty()) "0" else price
        val displayText = "$currency$priceVal $currency$discountPrice"
        return createTextView(displayText).apply {
            setText(displayText, TextView.BufferType.SPANNABLE)
            val spannable = text as Spannable
            spannable.setSpan(STRIKE_THROUGH_SPAN, 0, priceVal.length + currency.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun createTextView(text: String?): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            setPadding(16, 16, 16, 16)
            gravity = Gravity.CENTER
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}