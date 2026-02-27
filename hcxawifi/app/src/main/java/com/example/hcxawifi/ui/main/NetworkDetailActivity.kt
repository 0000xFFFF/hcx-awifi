package com.example.hcxawifi.ui.main

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.hcxawifi.R
import com.example.hcxawifi.utils.NetworkUtils
import com.example.hcxawifi.utils.UiHelper

class NetworkDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.network_detail_activity)

        val ssidText: TextView = findViewById(R.id.ssidText)
        val bssidText: TextView = findViewById(R.id.bssidText)
        val frequencyText: TextView = findViewById(R.id.frequencyText)
        val channelText: TextView = findViewById(R.id.channelText)
        val securityText: TextView = findViewById(R.id.securityText)
        val standardText: TextView = findViewById(R.id.standardText)
        val passpointText: TextView = findViewById(R.id.passpointText)
        val signalBar: ProgressBar = findViewById(R.id.signalBarDetail)

        // Receive fields individually
        val ssid = intent.getStringExtra("SSID") ?: "<Hidden SSID>"
        val bssid = intent.getStringExtra("BSSID") ?: "Unknown"
        val freq = intent.getIntExtra("FREQ", 0)
        val level = intent.getIntExtra("LEVEL", -100)
        val cap = intent.getStringExtra("CAP") ?: "Unknown"
        val std = intent.getIntExtra("STD", 0)
        val pass = intent.getBooleanExtra("PASS", false)

        // Set UI
        ssidText.text = ssid
        bssidText.text = "BSSID: $bssid"
        frequencyText.text = "Frequency: $freq MHz"
        channelText.text = "Channel: ${NetworkUtils.getChannelFromFrequency(freq)}"
        securityText.text = "Security: $cap"
        standardText.text = "Standard: ${NetworkUtils.getStandardName(std)}"
        passpointText.text = "Passpoint: $pass"

        signalBar.progress = UiHelper.calculateSignalProgress(level)
    }
}