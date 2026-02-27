package com.hcx_tools_extra.hcxawifi.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import com.hcx_tools_extra.hcxawifi.data.model.NetworkItem
import com.hcx_tools_extra.hcxawifi.data.repository.NetworkItemCsvRepository
import java.util.concurrent.Executor

class WifiScanManager(
    private val context: Context,
    private val repository: NetworkItemCsvRepository,
    private val executor: Executor,
    private val onScanComplete: (List<ScanResult>) -> Unit,
    private val onError: (String) -> Unit
) {
    private val wifiManager: WifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private var isScanning = false

    // --- Legacy receiver (API < 30) ---
    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            isScanning = false
            try {
                onScanComplete(wifiManager.scanResults)
            } catch (e: SecurityException) {
                onError("Permission error: ${e.message}")
            }
        }
    }

    // --- Modern callback (API >= 30) ---
    private val scanResultsCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        object : WifiManager.ScanResultsCallback() {
            override fun onScanResultsAvailable() {
                isScanning = false
                try {
                    onScanComplete(wifiManager.scanResults)
                } catch (e: SecurityException) {
                    onError("Permission error: ${e.message}")
                }
            }
        }
    } else null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            scanResultsCallback?.let {
                wifiManager.registerScanResultsCallback(executor, it)
            }
        } else {
            context.registerReceiver(
                wifiScanReceiver,
                IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            )
        }
    }

    fun destroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            scanResultsCallback?.let { wifiManager.unregisterScanResultsCallback(it) }
        } else {
            try {
                context.unregisterReceiver(wifiScanReceiver)
            } catch (e: IllegalArgumentException) {
                // Already unregistered
            }
        }
    }

    fun startScan() {
        if (isScanning) return
        isScanning = true

        @Suppress("DEPRECATION")
        val started = wifiManager.startScan()

        if (!started) {
            // OS throttled the scan, deliver cached results instead
            isScanning = false
            try {
                onScanComplete(wifiManager.scanResults)
            } catch (e: SecurityException) {
                onError("Permission error: ${e.message}")
            }
        }
    }

    fun filterAndSortResults(results: List<ScanResult>): List<NetworkItem> {
        return results
            .groupBy { it.BSSID }
            .mapNotNull { (_, group) -> group.maxByOrNull { it.level } }
            .sortedByDescending { it.level }
            .map { scanResult ->
                NetworkItem(
                    bssid = scanResult.BSSID,
                    ssid = scanResult.SSID,
                    level = scanResult.level,
                    capabilities = scanResult.capabilities,
                    frequency = scanResult.frequency,
                    channel = NetworkUtils.getChannelFromFrequency(scanResult.frequency),
                    standard = NetworkUtils.getStandardName(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            scanResult.wifiStandard
                        } else {
                            0
                        }
                    ),
                    password = repository.getNetworkItemPassword(scanResult.BSSID, scanResult.SSID)
                )
            }
    }
}