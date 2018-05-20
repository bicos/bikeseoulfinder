package com.kakao.bikeseoulfinder.ui.main

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.content.Context
import com.google.gson.Gson
import com.kakao.bikeseoulfinder.MainApplication
import com.kakao.bikeseoulfinder.R
import com.kakao.bikeseoulfinder.model.BikeStationList
import io.reactivex.Observable

class MainViewModel(context: Context) : AndroidViewModel(context.applicationContext as Application) {

    val dao = MainApplication.db?.bikeStationDao()

    fun loadJsonBikeListFile(): Observable<Unit> {
        return Observable.just(getApplication<Application>().resources.openRawResource(R.raw.bike_list))
                .map { it.bufferedReader().use { it.readText() } }
                .map { Gson().fromJson(it, BikeStationList::class.java) }
                .map { dao?.insertAll(it.desc) }
    }
}
