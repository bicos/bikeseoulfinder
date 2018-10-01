package com.kakao.bikeseoulfinder.ui.favorite

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.kakao.bikeseoulfinder.R
import kotlinx.android.synthetic.main.favorite_station_list_activity.*

class FavoriteStationListActivity : AppCompatActivity() {

    private lateinit var viewModel: FavoriteStationViewModel

    private val adapter = FavoriteListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.favorite_station_list_activity)

        list_favorite_station.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        list_favorite_station.adapter = adapter

        viewModel = FavoriteStationViewModel(this)
        viewModel.observeFavoriteStation().observe(this, Observer { bikeStationList ->
            bikeStationList?.let {
                adapter.bikeStationList.clear()
                adapter.bikeStationList.addAll(it)
                adapter.notifyDataSetChanged()
            }
        })
    }

}