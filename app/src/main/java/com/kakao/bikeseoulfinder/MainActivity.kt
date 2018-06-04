package com.kakao.bikeseoulfinder

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.kakao.bikeseoulfinder.network.ApiManager
import com.kakao.bikeseoulfinder.ui.main.MainFragment
import com.kakao.bikeseoulfinder.ui.main.MainViewModel
import com.kakao.bikeseoulfinder.ui.main.MainViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

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

        viewModel.count.observe(this, Observer {
            viewModel.count.removeObservers(this)
            if (it == 0) {
                viewModel.loadJsonBikeListFile()
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            Log.e(MainActivity::class.java.simpleName, it.toString())
                        }, {
                            Log.e(MainActivity::class.java.simpleName, it.message, it)
                        })
            } else {
                ApiManager.instance.getRealTimeBikeStations()
                        .flatMapIterable { t -> t.realtimeList }
                        .map {
                            val split = it.stationName.split(".")
                            if (split.size == 2)  {
                                val stationId = split[0]
                                if (!stationId.isBlank() && TextUtils.isDigitsOnly(stationId)) {
                                    viewModel.dao.updateParkingBikeTotCnt(stationId.toInt(), it.parkingBikeTotCnt)
                                }
                            }
                        }
                        .toList()
                        .subscribe { _: MutableList<Unit>? ->
                            val date = Date()
                            MainApplication.pref.setUpdateTime(date.time)
                        }
            }
        })
    }
}
