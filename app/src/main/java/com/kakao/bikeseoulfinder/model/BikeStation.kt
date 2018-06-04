package com.kakao.bikeseoulfinder.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "BikeStations")
data class BikeStation(
        @PrimaryKey
        @SerializedName("content_id") val id: Int,
        @SerializedName("new_addr") val newAddress: String,
        @SerializedName("cradle_count") val cradleCount: Int,
        @SerializedName("longitude") val longitude: Double,
        @SerializedName("latitude") val latitude: Double,
        @SerializedName("content_nm") val name: String,
        @SerializedName("addr_gu") val addressGu: String,
        @SerializedName("parkingBikeTotCnt") var parkingBikeTotCnt: Int = 0)
