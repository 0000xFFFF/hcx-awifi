package com.hcx_tools_extra.hcxawifi.utils

object NetworkUtils {
    
    fun getChannelFromFrequency(freq: Int): Int {
        return when (freq) {
            in 2412..2484 -> (freq - 2407) / 5
            in 5170..5835 -> (freq - 5000) / 5
            else -> -1
        }
    }
    
    fun getStandardName(standard: Int): String {
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
    
    fun isSecureNetwork(capabilities: String): Boolean {
        return capabilities.contains("WEP") || capabilities.contains("WPA")
    }

    fun bssidToLowerRemoveNonHex(bssid: String): String {
        return bssid.lowercase()
            .replace(":", "")
            .replace("-", "")
            .replace("_", "")
    }
}
