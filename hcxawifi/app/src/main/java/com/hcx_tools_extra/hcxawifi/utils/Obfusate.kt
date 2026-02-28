package com.hcx_tools_extra.hcxawifi.utils

import com.hcx_tools_extra.hcxawifi.data.model.NetworkItem
import kotlin.random.Random

object Obfusate {
    fun randomizeNetworks(networks: List<NetworkItem>): List<NetworkItem> {
        return networks.map { network ->
            network.copy(
                ssid = "wifi-${randomString(6)}",
                bssid = randomBssid(),
                password = if (Random.nextBoolean()) "..." else "pass-${randomString(4)}"
            )
        }
    }

    private fun randomString(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    private fun randomBssid(): String {
        return (1..6)
            .map {
                Random.nextInt(0, 256)
                    .toString(16)
                    .padStart(2, '0')
                    .uppercase()
            }
            .joinToString(":")
    }
}

