package com.kakao.bikeseoulfinder.ui.main

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.kakao.bikeseoulfinder.model.BikeStation

data class BikeStationItem(private val station: BikeStation) : ClusterItem {

    override fun getSnippet(): String {
        return "${station.parkingBikeTotCnt}대 남음"
    }

    override fun getTitle(): String {
        return station.stationName
    }

    override fun getPosition(): LatLng {
        return LatLng(station.stationLatitude, station.stationLongitude)
    }

    fun isFavorite(): Boolean {
        return station.isFavorite
    }

    fun getStation(): BikeStation {
        return station
    }
}