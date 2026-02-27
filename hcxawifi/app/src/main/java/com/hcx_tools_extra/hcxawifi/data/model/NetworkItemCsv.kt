package com.hcx_tools_extra.hcxawifi.data.model

data class NetworkItemCsv(
    val bssid: String,
    val essid: String,
    val password: String
)
{
    companion object {
        const val DELIMITER = "|||"
    }
}
