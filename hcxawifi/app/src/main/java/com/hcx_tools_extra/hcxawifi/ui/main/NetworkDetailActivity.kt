package com.hcx_tools_extra.hcxawifi.ui.main

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.hcx_tools_extra.hcxawifi.R
import com.hcx_tools_extra.hcxawifi.data.model.NetworkItem
import com.hcx_tools_extra.hcxawifi.utils.NetworkUtils
import com.hcx_tools_extra.hcxawifi.utils.UiHelper

class NetworkDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.network_detail_activity)

        // Get UI elements
        val ssidTextView: TextView = findViewById(R.id.ssidText)
        val bssidTextView: TextView = findViewById(R.id.bssidText)
        val frequencyTextView: TextView = findViewById(R.id.frequencyText)
        val channelTextView: TextView = findViewById(R.id.channelText)
        val capabilitiesTextView: TextView = findViewById(R.id.capabilitiesText)
        val levelTextView: TextView = findViewById(R.id.levelText)
        val signalBar: ProgressBar = findViewById(R.id.signalBarDetail)

        // Receive the Parcelable NetworkItem
        val network = intent.getParcelableExtra<NetworkItem>("network")

        if (network != null) {
            // Set values only (labels are in XML)
            ssidTextView.text = network.ssid
            bssidTextView.text = network.bssid
            frequencyTextView.text = "${network.frequency} MHz"
            channelTextView.text = "${NetworkUtils.getChannelFromFrequency(network.frequency)}"
            capabilitiesTextView.text = network.capabilities
            levelTextView.text = "${network.level} dBm"

            // Set ProgressBar value
            signalBar.progress = UiHelper.calculateSignalProgress(network.level)
        }
    }
}