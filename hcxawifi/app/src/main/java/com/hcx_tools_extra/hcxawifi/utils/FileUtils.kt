package com.hcx_tools_extra.hcxawifi.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

object FileUtils {

    fun readTextFromUri(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().use { it.readText() }
        } ?: ""
    }

    fun saveToInternalStorage(
        context: Context,
        fileName: String,
        content: String
    ) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(content.toByteArray())
        }
    }

    fun loadFromInternalStorage(
        context: Context,
        fileName: String
    ): String? {
        return try {
            context.openFileInput(fileName)
                .bufferedReader()
                .use { it.readText() }
        } catch (e: Exception) {
            null
        }
    }

    fun getFileName(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    return it.getString(index)
                }
            }
        }
        return null
    }
}