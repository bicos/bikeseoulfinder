package com.kakao.bikeseoulfinder.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.kakao.bikeseoulfinder.MainApplication
import com.kakao.bikeseoulfinder.model.BikeStation
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MainViewModel(context: Context) : AndroidViewModel(context.applicationContext as Application) {

    val dao = MainApplication.db.bikeStationDao()

    var stationList : LiveData<List<BikeStation>>? = null

    var disposable = CompositeDisposable()

    fun observeAll() : LiveData<List<BikeStation>>{
        return dao.getAll()
    }

    fun changeFavoriteStateBikeStation(bikeStation: BikeStation, isFavorite: Boolean) {
        Observable.just(bikeStation)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    bikeStation.isFavorite = isFavorite
                    dao.update(bikeStation)
                }, {

                }).let {
                    disposable.add(it)
                }
    }

    fun observeNearByStation(fromLat: Double, toLat: Double, fromLon: Double, toLon: Double) : LiveData<List<BikeStation>> {
        return dao.getNearByStations(fromLat, toLat, fromLon, toLon).apply {
            stationList = this
        }
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }
}
