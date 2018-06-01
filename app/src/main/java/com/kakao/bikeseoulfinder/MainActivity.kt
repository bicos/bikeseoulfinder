package com.kakao.bikeseoulfinder

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.kakao.bikeseoulfinder.network.ApiManager
import com.kakao.bikeseoulfinder.network.BikeStationResponse
import com.kakao.bikeseoulfinder.ui.main.MainFragment
import com.kakao.bikeseoulfinder.ui.main.MainViewModel
import com.kakao.bikeseoulfinder.ui.main.MainViewModelFactory
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
        }

        viewModel = ViewModelProviders.of(this, MainViewModelFactory(this))
                .get(MainViewModel::class.java)


        viewModel.dao?.getCount()?.observe(this, Observer {
            if (it == 0) {
                viewModel.loadJsonBikeListFile()
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            Log.e(MainActivity::class.java.simpleName, it.toString())
                        }, {
                            Log.e(MainActivity::class.java.simpleName, it.message, it)
                        })
            } else {
                viewModel.dao?.getAll()
                ApiManager.instance.getRealTimeBikeStations()
                        .subscribeOn(Schedulers.io())
                        .subscribe ({
                            t: BikeStationResponse? ->

                        }, {
                            Log.e(MainActivity::class.java.simpleName, it.message, it)
                        })
            }
        })
    }
}
