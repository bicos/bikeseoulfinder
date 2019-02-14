package com.kakao.bikeseoulfinder.ui.main

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.android.gms.maps.model.VisibleRegion
import com.kakao.bikeseoulfinder.MainApplication
import com.kakao.bikeseoulfinder.model.BikeStation
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class MainViewModel(context: Context) : AndroidViewModel(context.applicationContext as Application) {

    val dao = MainApplication.db.bikeStationDao()

    var disposable = CompositeDisposable()

    val updateVisibleRegionEvent = PublishSubject.create<VisibleRegion>()

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
                    Log.e("MainViewModel", it.message, it)
                }).let {
                    disposable.add(it)
                }
    }

    fun observeStation(lat: Double, lon: Double): LiveData<BikeStation> {
        return dao.getStation(lat, lon)
    }

    fun observeNearByStation(fromLat: Double, toLat: Double, fromLon: Double, toLon: Double) : LiveData<List<BikeStation>> {
        return dao.getNearByStations(fromLat, toLat, fromLon, toLon)
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }

    fun updateVisibleRegion(visibleRegion: VisibleRegion) {
        updateVisibleRegionEvent.onNext(visibleRegion)
    }

    fun updateParkingBikeCountOrInsert(realtimeList: List<BikeStation>) {
        dao.insertAll(realtimeList)
        for (bikeStation in realtimeList) {
            dao.updateParkingBikeCnt(bikeStation.stationLatitude, bikeStation.stationLongitude,
                    bikeStation.parkingBikeTotCnt, bikeStation.rackTotCnt)
        }
    }
}
