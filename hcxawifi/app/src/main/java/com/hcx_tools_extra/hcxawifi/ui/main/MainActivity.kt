package com.hcx_tools_extra.hcxawifi.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.ScanResult
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.hcx_tools_extra.hcxawifi.R
import com.hcx_tools_extra.hcxawifi.data.model.NetworkItem
import com.hcx_tools_extra.hcxawifi.data.repository.NetworkItemCsvRepository
import com.hcx_tools_extra.hcxawifi.utils.UiHelper
import com.hcx_tools_extra.hcxawifi.utils.WifiScanManager

class MainActivity : ComponentActivity() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var repository: NetworkItemCsvRepository
    private lateinit var wifiScanManager: WifiScanManager
    
    private lateinit var scanButton: Button
    private lateinit var scanStatusLabel: TextView
    private lateinit var loadCsvButton: Button
    private lateinit var loadCsvStatusLabel: TextView
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Initialize UI elements
        scanButton = findViewById(R.id.scanButton)
        scanStatusLabel = findViewById(R.id.scanStatusLabel)
        loadCsvButton = findViewById(R.id.loadCsvButton)
        loadCsvStatusLabel = findViewById(R.id.loadCsvStatusLabel)
        listView = findViewById(R.id.networkList)
        swipeRefresh = findViewById(R.id.swipeRefresh)

        // Initialize repository
        repository = NetworkItemCsvRepository(this)

        // Initialize WiFi scan manager
        wifiScanManager = WifiScanManager(
            context = this,
            repository,
            mainExecutor,
            onScanComplete = { results -> handleScanResults(results) },
            onError = { error -> scanStatusLabel.text = error }
        )

        swipeRefresh.setOnRefreshListener {
            startWifiScan()
            swipeRefresh.isRefreshing = false
        }

        // Setup click listeners
        scanButton.setOnClickListener { checkPermissionAndScan() }
        loadCsvButton.setOnClickListener { openCsvFilePicker() }
        listView.setOnItemClickListener { _, _, position, _ ->
            handleNetworkItemClick(position)
        }

        // Load cached data on startup
        repository.loadFromCache()
        if (repository.itemsCsv.isNotEmpty()) {
            updateCsvLabel(repository.itemsCsv.size)
        }

        checkPermissionAndScan()
    }

    private fun handleScanResults(results: List<ScanResult>) {
        val filtered = wifiScanManager.filterAndSortResults(results)
        listView.adapter = WifiAdapter(this, filtered)
        UiHelper.flashSuccessLabel(this, scanStatusLabel, "${filtered.size} networks")
    }

    private fun handleNetworkItemClick(position: Int) {
        // Get the NetworkItem from the adapter
        val network = listView.adapter.getItem(position) as NetworkItem

        // Pass the whole object as a Parcelable
        val intent = Intent(this, NetworkDetailActivity::class.java).apply {
            putExtra("network", network) // NetworkItem implements Parcelable
        }

        startActivity(intent)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startWifiScan()
            } else {
                scanStatusLabel.text = "Permission denied"
            }
        }

    private fun checkPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startWifiScan()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun startWifiScan() {
        scanStatusLabel.text = "Scanning..."
        wifiScanManager.startScan()
    }

    private fun openCsvFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/csv", "text/comma-separated-values"))
        }
        csvPickerLauncher.launch(intent)
    }

    private val csvPickerLauncher = 
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri -> handleCsvSelected(uri) }
            }
        }

    private fun handleCsvSelected(uri: Uri) {
        val items = repository.loadFromUri(uri)
        updateCsvLabel(items.size)
        Toast.makeText(this, "CSV loaded and cached!", Toast.LENGTH_SHORT).show()
    }

    private fun updateCsvLabel(count: Int) {
        loadCsvStatusLabel.text = "$count passwords"
    }
}
