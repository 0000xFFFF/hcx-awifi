package com.example.hcxawifi.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.example.hcxawifi.R
import com.example.hcxawifi.data.model.NetworkItem
import com.example.hcxawifi.utils.NetworkUtils
import com.example.hcxawifi.utils.UiHelper

class WifiAdapter(
    context: Context,
    private val networks: List<NetworkItem>
) : ArrayAdapter<NetworkItem>(context, 0, networks) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_network, parent, false)
        
        val network = networks[position]

        val essidText: TextView = view.findViewById(R.id.essid)
        val bssidText: TextView = view.findViewById(R.id.bssid)
        val detailsText: TextView = view.findViewById(R.id.details)
        val passwordText: TextView = view.findViewById(R.id.password)
        val levelText: TextView = view.findViewById(R.id.level)
        val icon: ImageView = view.findViewById(R.id.securityIcon)
        val signalBar: ProgressBar = view.findViewById(R.id.signalBar)

        essidText.text = network.ssid.ifEmpty { "<Hidden SSID>" }
        bssidText.text = network.bssid.uppercase()
        detailsText.text = network.capabilities
        levelText.text = "${network.level} dBm"
        passwordText.text = network.password?: ""

        try {
            val iconRes = if (NetworkUtils.isSecureNetwork(network.capabilities)) {
                R.drawable.ic_lock
            } else {
                R.drawable.ic_unlock
            }
            icon.setImageResource(iconRes)
        } catch (e: Exception) {
            icon.setImageResource(android.R.drawable.ic_lock_idle_lock)
        }

        signalBar.progress = UiHelper.calculateSignalProgress(network.level)

        return view
    }
}
