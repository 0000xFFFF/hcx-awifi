package com.example.hcxawifi

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import android.net.wifi.ScanResult

class NetworkDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.details_network)

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
        channelText.text = "Channel: ${getChannel(freq)}"
        securityText.text = "Security: $cap"
        standardText.text = "Standard: ${getStandardName(std)}"
        passpointText.text = "Passpoint: $pass"

        val progress = ((level + 100) * 100 / 70).coerceIn(0, 100)
        signalBar.progress = progress
    }

    private fun getChannel(freq: Int): Int {
        return when (freq) {
            in 2412..2484 -> (freq - 2407) / 5
            in 5170..5835 -> (freq - 5000) / 5
            else -> -1
        }
    }

    private fun getStandardName(standard: Int): String {
        return when (standard) {
            0 -> "Unknown"
            1 -> "802.11a"
            2 -> "802.11b"
            3 -> "802.11g"
            4 -> "802.11n"
            5 -> "802.11ac"
            6 -> "802.11ax"
            else -> "Other"
        }
    }
}