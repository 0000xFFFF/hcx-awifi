package com.hcx_tools_extra.hcxawifi.data.model

import android.os.Parcel
import android.os.Parcelable

data class NetworkItem(
    val bssid: String,
    val ssid: String,
    val level: Int,
    val capabilities: String,
    val frequency: Int,
    val channel: Int,
    val standard: String,
    val password: String? = null
) : Parcelable {

    // Constructor used when recreating object from Parcel
    constructor(parcel: Parcel) : this(
        bssid = parcel.readString() ?: "",
        ssid = parcel.readString() ?: "",
        level = parcel.readInt(),
        capabilities = parcel.readString() ?: "",
        frequency = parcel.readInt(),
        channel = parcel.readInt(),
        standard = parcel.readString() ?: "",
        password = parcel.readString()
    )

    // Write object data to Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(bssid)
        parcel.writeString(ssid)
        parcel.writeInt(level)
        parcel.writeString(capabilities)
        parcel.writeInt(frequency)
        parcel.writeInt(channel)
        parcel.writeString(standard)
        parcel.writeString(password)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<NetworkItem> {
        override fun createFromParcel(parcel: Parcel): NetworkItem = NetworkItem(parcel)
        override fun newArray(size: Int): Array<NetworkItem?> = arrayOfNulls(size)
    }
}