package com.example.myapplication

import com.google.gson.annotations.SerializedName

data class DataItem(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String,
    @SerializedName("location") val location: String,
    @SerializedName("serial_number") val serial_number: String
)