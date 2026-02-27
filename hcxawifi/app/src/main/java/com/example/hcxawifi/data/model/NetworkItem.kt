package com.example.hcxawifi.data.model

data class NetworkItem(
    val bssid: String,
    val ssid: String,
    val level: Int,
    val capabilities: String,
    val frequency: Float,
    val channel: Int,
    val security: String,
    val standard: String,
)
