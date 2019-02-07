package com.kakao.bikeseoulfinder.model

import androidx.room.Entity

@Entity(tableName = "BikeStations", primaryKeys = ["stationLatitude", "stationLongitude"])
data class BikeStation(
        val stationName: String,
        val stationLatitude: Double,
        val stationLongitude: Double,
        val rackTotCnt: Int,
        val parkingBikeTotCnt: Int,
        var isFavorite: Boolean)
