package com.kakao.bikeseoulfinder.ui.main

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.kakao.bikeseoulfinder.model.BikeStation

class BikeStationItem(private val station : BikeStation) : ClusterItem {

    override fun getSnippet(): String {
        return "${station.newAddress} ${station.parkingBikeTotCnt}대 남음"
    }

    override fun getTitle(): String {
        return station.name
    }

    override fun getPosition(): LatLng {
        return LatLng(station.latitude, station.longitude)
    }
}