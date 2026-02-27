package com.example.hcxawifi.data.cache

import android.content.Context
import com.example.hcxawifi.utils.Constants


class CacheManager(private val context: Context) {

    private val fileName = Constants.CACHE_FILE_NAME;

    fun saveRawCsv(csvText: String) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(csvText.toByteArray())
        }
    }

    fun loadRawCsv(): String? {
        return try {
            context.openFileInput(fileName)
                .bufferedReader()
                .use { it.readText() }
        } catch (e: Exception) {
            null
        }
    }

    fun clearCache() {
        context.deleteFile(fileName)
    }

    fun cacheExists(): Boolean {
        return context.fileList().contains(fileName)
    }
}