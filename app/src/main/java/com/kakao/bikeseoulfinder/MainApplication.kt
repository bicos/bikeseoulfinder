package com.kakao.bikeseoulfinder

import android.app.Application
import android.arch.persistence.room.Room
import com.kakao.bikeseoulfinder.model.AppDatabase

class MainApplication : Application() {

    companion object {
        lateinit var db : AppDatabase
    }

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(this, AppDatabase::class.java, "bike_station_list")
                .fallbackToDestructiveMigration().build()
    }
}