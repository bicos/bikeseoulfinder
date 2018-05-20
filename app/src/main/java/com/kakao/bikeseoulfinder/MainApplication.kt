package com.kakao.bikeseoulfinder

import android.app.Application
import android.arch.persistence.room.Room
import com.kakao.bikeseoulfinder.model.AppDatabase

class MainApplication : Application() {

    companion object {
        var db : AppDatabase? = null
    }

    override fun onCreate() {
        super.onCreate()
        db = db ?: Room.databaseBuilder(this, AppDatabase::class.java, "bike_station_list").build()
    }
}