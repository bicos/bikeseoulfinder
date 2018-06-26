package com.kakao.bikeseoulfinder.model

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update

@Dao
interface BikeStationDao {

    @Query("SELECT * FROM BikeStations")
    fun getAll(): LiveData<List<BikeStation>>

    @Insert(onConflict = REPLACE)
    fun insertAll(bikeStation: List<BikeStation>)

    @Update(onConflict = REPLACE)
    fun updateAll(bikeStation: List<BikeStation?>)

    @Query("UPDATE BikeStations SET parkingBikeTotCnt = :parkingBikeTotalCount WHERE stationLatitude = :stationLatitude AND stationLongitude = :stationLongitude")
    fun updateParkingBikeTotCnt(stationLatitude: Double, stationLongitude: Double, parkingBikeTotalCount: Int)

    @Query("SELECT count(*) FROM BikeStations")
    fun getCount(): LiveData<Int>

    @Query("SELECT * FROM BikeStations WHERE stationLatitude >= :fromLat AND stationLatitude <= :toLat AND stationLongitude >= :fromLon AND stationLongitude <= :toLon")
    fun getNearByStations(fromLat: Double, toLat: Double, fromLon: Double, toLon: Double): LiveData<List<BikeStation>>
}