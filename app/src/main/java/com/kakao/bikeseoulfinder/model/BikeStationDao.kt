package com.kakao.bikeseoulfinder.model

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query

@Dao
interface BikeStationDao {

    @Query("SELECT * FROM BikeStations")
    fun getAll(): LiveData<List<BikeStation>>

    @Insert(onConflict = REPLACE)
    fun insertAll(bikeStation: List<BikeStation>)

    @Query("SELECT count(*) FROM BikeStations")
    fun getCount(): LiveData<Int>

    @Query("SELECT * FROM BikeStations WHERE latitude >= :fromLat AND latitude <= :toLat AND longitude >= :fromLon AND longitude <= :toLon")
    fun getNearByStations(fromLat: Double, toLat: Double, fromLon: Double, toLon: Double): LiveData<List<BikeStation>>
}