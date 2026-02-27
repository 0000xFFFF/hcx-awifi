package com.example.hcxawifi

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        checkPermissionAndScan()

        // Button click
        scanButton.setOnClickListener { checkPermissionAndScan() }

        // Register receiver for scan results
        registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))

        listView.setOnItemClickListener { _, _, position, _ ->
            val network = listView.adapter.getItem(position) as ScanResult

            val intent = Intent(this, NetworkDetailActivity::class.java)
            intent.putExtra("SSID", network.SSID)
            intent.putExtra("BSSID", network.BSSID)
            intent.putExtra("FREQ", network.frequency)
            intent.putExtra("LEVEL", network.level)
            intent.putExtra("CAP", network.capabilities)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                intent.putExtra("STD", network.wifiStandard)
            }
            intent.putExtra("PASS", network.isPasspointNetwork)
            startActivity(intent)
        }
    }

    private var scanResultsCallback: WifiManager.ScanResultsCallback? = null
    private fun startWifiScan() {
        statusLabel.text = "Scanning..."
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Unregister previous callback if any
            scanResultsCallback?.let { wifiManager.unregisterScanResultsCallback(it) }

            val callback = object : WifiManager.ScanResultsCallback() {
                override fun onScanResultsAvailable() {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        statusLabel.text = "Permission missing"
                        return
                    }
                    updateListView(wifiManager.scanResults)
                }
            }
            scanResultsCallback = callback
            wifiManager.registerScanResultsCallback(mainExecutor, callback)
            wifiManager.startScan() // still needed to trigger a fresh scan
        } else {
            // Legacy path — BroadcastReceiver handles results
            wifiManager.startScan()
        }
    }

    // BroadcastReceiver for scan completion
    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) return // handled by callback

            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                statusLabel.text = "Permission missing"
                return
            }
            try {
                updateListView(wifiManager.scanResults)
            } catch (e: SecurityException) {
                statusLabel.text = "Permission error: ${e.message}"
            }
        }
    }

    private fun updateListView(results: List<ScanResult>) {
        val filtered = results
            .groupBy { it.BSSID }
            .mapNotNull { (_, group) -> group.maxByOrNull { it.level } }
            .sortedByDescending { it.level }

        listView.adapter = WifiAdapter(this, filtered)

        // Flash the status label green briefly
        statusLabel.text = "Updated — ${filtered.size} networks"
        statusLabel.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
        statusLabel.animate()
            .alpha(1f)
            .setDuration(200)
            .withEndAction {
                statusLabel.animate()
                    .alpha(0.6f)
                    .setDuration(1000)
                    .withEndAction {
                        statusLabel.setTextColor(ContextCompat.getColor(this, android.R.color.black))
                        statusLabel.alpha = 1f
                    }
                    .start()
            }
            .start()
    }

    // Permission launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startWifiScan() // or startWifiScanModern() if API >= 30
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
                startWifiScan() // start scanning immediately
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiScanReceiver)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            scanResultsCallback?.let { wifiManager.unregisterScanResultsCallback(it) }
        }
    }

    class WifiAdapter(
        private val context: Context,
        private val networks: List<ScanResult>
    ) : ArrayAdapter<ScanResult>(context, 0, networks) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_network, parent, false)
            val network = networks[position]

            val ssidText: TextView = view.findViewById(R.id.ssid)
            val detailsText: TextView = view.findViewById(R.id.details)
            val icon: ImageView = view.findViewById(R.id.securityIcon)

            // Safe text
            ssidText.text = if (network.SSID.toString().isNotEmpty()) network.SSID.toString() else "<Hidden SSID>"
            detailsText.text = "${network.BSSID ?: "Unknown"} | ${network.level} dBm | ${network.capabilities ?: ""}"

            // Safe icon
            try {
                icon.setImageResource(
                    if (network.capabilities.contains("WEP") || network.capabilities.contains("WPA"))
                        R.drawable.ic_lock else R.drawable.ic_unlock
                )
            } catch (e: Exception) {
                // fallback icon if drawable missing
                icon.setImageResource(android.R.drawable.ic_lock_idle_lock)
            }

            // Map RSSI (-100..0 dBm) to 0..100%
            val progress = ((network.level + 100) * 100 / 70).coerceIn(0, 100)
            val signalBar: ProgressBar = view.findViewById(R.id.signalBar)
            signalBar.progress = progress

            return view
        }
    }
}
