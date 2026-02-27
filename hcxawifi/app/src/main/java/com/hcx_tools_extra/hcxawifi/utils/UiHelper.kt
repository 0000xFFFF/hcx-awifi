package com.hcx_tools_extra.hcxawifi.utils

import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.hcx_tools_extra.hcxawifi.R

object UiHelper {

    fun flashSuccessLabel(context: Context, label: TextView, message: String) {
        label.text = message
        label.setTextColor(ContextCompat.getColor(context, R.color.monokai_yellow))
        label.animate()
            .alpha(1f)
            .setDuration(200)
            .withEndAction {
                label.animate()
                    .alpha(0.6f)
                    .setDuration(1000)
                    .withEndAction {
                        label.setTextColor(ContextCompat.getColor(context, R.color.monokai_purple))
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
