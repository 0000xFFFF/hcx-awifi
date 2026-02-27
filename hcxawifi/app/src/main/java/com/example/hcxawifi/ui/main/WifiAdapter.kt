package com.example.hcxawifi.ui.main

import android.content.Context
import android.net.wifi.ScanResult
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

        val ssidText: TextView = view.findViewById(R.id.ssid)
        val detailsText: TextView = view.findViewById(R.id.details)
        val icon: ImageView = view.findViewById(R.id.securityIcon)
        val signalBar: ProgressBar = view.findViewById(R.id.signalBar)

        ssidText.text = if (network.ssid.isNotEmpty()) { network.ssid  } else { "<Hidden SSID>" }
        
        detailsText.text = "${network.bssid ?: "Unknown"} | ${network.level} dBm | ${network.capabilities ?: ""}"

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
