package com.kakao.bikeseoulfinder.network

data class BikeStationResponse(val realtimeList: List<SimpleBikeStation>) {

    data class SimpleBikeStation(val stationName: String,
                      val stationLatitude: Double,
                      val stationLongitude: Double,
                      val parkingBikeTotCnt: Int)
}