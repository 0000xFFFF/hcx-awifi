package com.example.hcxawifi.data.repository

import android.content.Context
import android.net.Uri
import com.example.hcxawifi.data.cache.CacheManager
import com.example.hcxawifi.data.model.NetworkItem
import com.example.hcxawifi.data.model.NetworkItemCsv
import com.example.hcxawifi.utils.CsvParser
import com.example.hcxawifi.utils.FileUtils

class CsvRepository(private val context: Context) {

    private val cacheManager = CacheManager(context)

    /**
     * Load CSV from URI, cache it, and return parsed list
     */
    fun loadFromUri(uri: Uri): List<NetworkItemCsv> {
        val rawCsv = FileUtils.readTextFromUri(context, uri)
        if (rawCsv.isBlank()) return emptyList()
        cacheManager.saveRawCsv(rawCsv) // Save to cache
        return CsvParser.parseNetworkCsv(rawCsv) // Parse
    }

    /**
     * Load from cached file (if exists)
     */
    fun loadFromCache(): List<NetworkItemCsv> {
        val cachedCsv = cacheManager.loadRawCsv() ?: return emptyList()
        return CsvParser.parseNetworkCsv(cachedCsv)
    }

    fun clearCache() {
        cacheManager.clearCache()
    }

    fun hasCache(): Boolean {
        return cacheManager.cacheExists()
    }
    
    fun getCachedItemCount(): Int {
        return loadFromCache().size
    }
}