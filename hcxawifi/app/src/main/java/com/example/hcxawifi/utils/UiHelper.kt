package com.example.hcxawifi.utils

import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.hcxawifi.R

object UiHelper {

    fun flashSuccessLabel(context: Context, label: TextView, message: String) {
        label.text = message
        label.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
        label.animate()
            .alpha(1f)
            .setDuration(200)
            .withEndAction {
                label.animate()
                    .alpha(0.6f)
                    .setDuration(1000)
                    .withEndAction {
                        label.setTextColor(ContextCompat.getColor(context, R.color.dark_theme_purple))
                        label.alpha = 1f
                    }
                    .start()
            }
            .start()
    }


    
    fun calculateSignalProgress(rssiLevel: Int): Int {
        return ((rssiLevel + 100) * 100 / 70).coerceIn(0, 100)
    }
}
