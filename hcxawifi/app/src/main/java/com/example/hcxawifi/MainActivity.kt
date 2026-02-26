package com.example.hcxawifi

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var wifiManager: WifiManager
    private lateinit var scanButton: Button
    private lateinit var statusLabel: TextView
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        scanButton = findViewById(R.id.scan)
        statusLabel = findViewById(R.id.statusLabel)
        listView = findViewById(R.id.networkList)

        // Get WifiManager
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // Button click
        scanButton.setOnClickListener {
            checkPermissionAndScan()
        }

        // Register receiver for scan results
        registerReceiver(
            wifiScanReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )
    }

    // BroadcastReceiver for scan completion
    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                statusLabel.text = "Permission missing"
                return
            }

            try {
                val results = wifiManager.scanResults

                statusLabel.text = "Found ${results.size} networks"

                val networkList = results.map {
                    "${it.SSID} | ${it.BSSID} | ${it.level} dBm | ${it.capabilities}"
                }

                listView.adapter = ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_list_item_1,
                    networkList
                )

            } catch (e: SecurityException) {
                statusLabel.text = "Permission error: " + e.message
            }
        }
    }

    // Permission launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startWifiScan()
            } else {
                statusLabel.text = "Permission denied"
            }
        }

    private fun checkPermissionAndScan() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                startWifiScan()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun startWifiScan() {
        statusLabel.text = "Scanning..."
        wifiManager.startScan()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiScanReceiver)
    }
}
