package com.kakao.bikeseoulfinder.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BikeStation::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bikeStationDao(): BikeStationDao
}