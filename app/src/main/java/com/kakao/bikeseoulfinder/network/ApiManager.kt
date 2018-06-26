package com.kakao.bikeseoulfinder.network

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiManager {

    companion object {

        val instance: ApiManager by lazy {
            val retrofit = Retrofit.Builder()
                    .baseUrl("https://www.bikeseoul.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                    .build()
            retrofit.create(ApiManager::class.java)
        }
    }

    @POST("app/station/getStationRealtimeStatus.do")
    @FormUrlEncoded
    fun getRealTimeBikeStations(@Field("stationGrpSeq") stationGrpSeq: Int = 10): Observable<BikeStationResponse>
}