package com.hcx_tools_extra.hcxawifi.data.repository

import android.content.Context
import android.net.Uri
import com.hcx_tools_extra.hcxawifi.data.model.NetworkItemCsv
import com.hcx_tools_extra.hcxawifi.utils.Constants
import com.hcx_tools_extra.hcxawifi.utils.CsvParser
import com.hcx_tools_extra.hcxawifi.utils.FileUtils
import com.hcx_tools_extra.hcxawifi.utils.NetworkUtils.bssidToLowerRemoveNonHex

class NetworkItemCsvRepository(private val context: Context) {

    private val fileName = Constants.CACHE_FILE_NAME
    var itemsCsv: List<NetworkItemCsv> = emptyList()

    /**
     * Load CSV from URI, cache it, and return parsed list
     */
    fun loadFromUri(uri: Uri): List<NetworkItemCsv> {
        val rawCsv = FileUtils.readTextFromUri(context, uri)
        if (rawCsv.isBlank()) return emptyList()
        saveRawCsv(rawCsv)
        itemsCsv = CsvParser.parseNetworkCsv(rawCsv)
        return itemsCsv
    }

    /**
     * Load from cached file (if exists)
     */
    fun loadFromCache(): List<NetworkItemCsv> {
        val rawCsv = loadRawCsv() ?: return emptyList()
        itemsCsv = CsvParser.parseNetworkCsv(rawCsv)
        return itemsCsv
    }

    fun clearCache() {
        itemsCsv = emptyList()
        context.deleteFile(fileName)
    }

    fun hasCache(): Boolean {
        return itemsCsv.isNotEmpty() || context.fileList().contains(fileName)
    }

    fun getNetworkItemPassword(bssid: String, essid: String): String? {
        return itemsCsv.find { item ->
            bssidToLowerRemoveNonHex(item.bssid) == bssidToLowerRemoveNonHex(bssid) && item.essid == essid
        }?.password
    }

    // Private helper methods
    private fun saveRawCsv(csvText: String) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(csvText.toByteArray())
        }
    }

    private fun loadRawCsv(): String? {
        return try {
            context.openFileInput(fileName)
                .bufferedReader()
                .use { it.readText() }
        } catch (e: Exception) {
            null
        }
    }
}
