package com.kakao.bikeseoulfinder.model

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = [(BikeStation::class)], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bikeStationDao(): BikeStationDao
}