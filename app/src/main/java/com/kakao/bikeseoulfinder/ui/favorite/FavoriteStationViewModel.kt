package com.kakao.bikeseoulfinder.ui.favorite

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import android.content.Context
import com.kakao.bikeseoulfinder.MainApplication
import com.kakao.bikeseoulfinder.model.BikeStation

class FavoriteStationViewModel(context: Context) : AndroidViewModel(context.applicationContext as Application) {

    val dao = MainApplication.db.bikeStationDao()

    var stationList: LiveData<List<BikeStation>>? = null

    fun observeFavoriteStation(): LiveData<List<BikeStation>> {
        return dao.getFavoriteStations().apply {
            stationList = this
        }
    }
}