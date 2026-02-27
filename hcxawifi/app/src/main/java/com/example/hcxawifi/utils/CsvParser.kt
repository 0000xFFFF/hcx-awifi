package com.example.hcxawifi.utils

import com.example.hcxawifi.data.model.NetworkItemCsv

object CsvParser {

    fun parseNetworkCsv(csvText: String): List<NetworkItemCsv> {

        val lines = csvText.lines().filter { it.isNotBlank() }

        if (lines.isEmpty()) return emptyList()

        return lines.mapNotNull { line ->
            val parts = line.split(NetworkItemCsv.DELIMITER)

            if (parts.size < 3) return@mapNotNull null

            try {
                NetworkItemCsv(
                    bssid = parts[0].trim(),
                    essid = parts[1].trim(),
                    password = parts[2].trim()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}