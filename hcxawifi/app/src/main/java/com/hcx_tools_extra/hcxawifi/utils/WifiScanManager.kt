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
    private val onScanComplete: (List<ScanResult>) -> Unit,
    private val onError: (String) -> Unit
) {
    private val wifiManager: WifiManager = 
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    
    private var scanResultsCallback: WifiManager.ScanResultsCallback? = null
    
    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) return
            
            try {
                val results = wifiManager.scanResults
                onScanComplete(results)
            } catch (e: SecurityException) {
                onError("Permission error: ${e.message}")
            }
        }
    }
    
    fun registerReceiver() {
        context.registerReceiver(
            wifiScanReceiver, 
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )
    }
    
    fun unregisterReceiver() {
        try {
            context.unregisterReceiver(wifiScanReceiver)
        } catch (e: IllegalArgumentException) {
            // Already unregistered
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            scanResultsCallback?.let { wifiManager.unregisterScanResultsCallback(it) }
        }
    }
    
    fun startScan(executor: Executor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            scanResultsCallback?.let { wifiManager.unregisterScanResultsCallback(it) }
            
            val callback = object : WifiManager.ScanResultsCallback() {
                override fun onScanResultsAvailable() {
                    try {
                        onScanComplete(wifiManager.scanResults)
                    } catch (e: SecurityException) {
                        onError("Permission error: ${e.message}")
                    }
                }
            }
            scanResultsCallback = callback
            wifiManager.registerScanResultsCallback(executor, callback)
        }
        wifiManager.startScan()
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
