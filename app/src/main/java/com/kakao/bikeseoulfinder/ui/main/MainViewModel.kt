package com.kakao.bikeseoulfinder.ui.main

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.content.Context
import com.kakao.bikeseoulfinder.MainApplication
import com.kakao.bikeseoulfinder.model.BikeStation

class MainViewModel(context: Context) : AndroidViewModel(context.applicationContext as Application) {

    val dao = MainApplication.db.bikeStationDao()

    var stationList : LiveData<List<BikeStation>>? = null

    fun observeAll() : LiveData<List<BikeStation>>{
        return dao.getAll()
    }

    fun observeNearByStation(fromLat: Double, toLat: Double, fromLon: Double, toLon: Double) : LiveData<List<BikeStation>> {
        return dao.getNearByStations(fromLat, toLat, fromLon, toLon).apply {
            stationList = this
        }
    }
}
