package com.onscreensync.tvapp.utils

import android.graphics.Color
import android.widget.TextView

object UiHelper {

    @JvmStatic
    fun setTextViewFont(textView: TextView, textFont: String?) {
        if (textFont.isNullOrEmpty()) return

        val toInt = textFont.convertToInt()
        if (toInt == 0) return

        textView.textSize = toInt.toFloat()
    }

    @JvmStatic
    fun setTextViewColor(textView: TextView, textColor: String?) {
        if (textColor.isNullOrEmpty()) return
        textView.setTextColor(parseColor(textColor))
    }

    @JvmStatic
    fun parseColor(textColor: String?): Int {
        if (textColor.isNullOrEmpty()) return Color.WHITE

        return when (textColor!!.lowercase()) {
            "red" -> Color.RED
            "blue" -> Color.BLUE
            "grey", "gray" -> Color.GRAY
            "black" -> Color.BLACK
            "yellow" -> Color.YELLOW
            else -> {
                try {
                    Color.parseColor(textColor.lowercase())
                } catch (ex: Exception) {
                    Color.WHITE
                }
            }
        }
    }
}