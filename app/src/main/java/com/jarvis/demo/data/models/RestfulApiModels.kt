package com.jarvis.demo.data.models

import com.google.gson.annotations.SerializedName

data class Device(
    val id: String,
    val name: String,
    val data: DeviceData?
)

data class DeviceData(
    val year: Int?,
    val price: Double?,
    @SerializedName("CPU model")
    val cpuModel: String?,
    @SerializedName("Hard disk size")
    val hardDiskSize: String?,
    val color: String?
)

data class CreateDeviceRequest(
    val name: String,
    val data: DeviceData?
)

data class UpdateDeviceRequest(
    val name: String?,
    val data: DeviceData?
)