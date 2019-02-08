package com.kakao.bikeseoulfinder.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update

@Dao
interface BikeStationDao {

    @Query("SELECT * FROM BikeStations")
    fun getAll(): LiveData<List<BikeStation>>

    @Insert(onConflict = IGNORE)
    fun insertAll(bikeStation: List<BikeStation>)

    @Update(onConflict = REPLACE)
    fun updateAll(bikeStation: List<BikeStation?>)

    @Update(onConflict = REPLACE)
    fun update(bikeStation: BikeStation)

    @Query("UPDATE BikeStations SET parkingBikeTotCnt = :parkingBikeTotalCount, rackTotCnt = :rackTotCount WHERE stationLatitude = :stationLatitude AND stationLongitude = :stationLongitude")
    fun updateParkingBikeCnt(stationLatitude: Double, stationLongitude: Double, parkingBikeTotalCount: Int, rackTotCount: Int)

    @Query("SELECT count(*) FROM BikeStations")
    fun getCount(): LiveData<Int>

    @Query("SELECT * FROM BikeStations WHERE stationLatitude >= :fromLat AND stationLatitude <= :toLat AND stationLongitude >= :fromLon AND stationLongitude <= :toLon")
    fun getNearByStations(fromLat: Double, toLat: Double, fromLon: Double, toLon: Double): LiveData<List<BikeStation>>

    @Query("SELECT * FROM BikeStations WHERE isFavorite")
    fun getFavoriteStations() : LiveData<List<BikeStation>>
}