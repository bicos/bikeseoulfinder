package com.kakao.bikeseoulfinder.model

import com.google.gson.annotations.SerializedName

data class BikeStationList(@SerializedName("DATA") val desc: List<BikeStation>)