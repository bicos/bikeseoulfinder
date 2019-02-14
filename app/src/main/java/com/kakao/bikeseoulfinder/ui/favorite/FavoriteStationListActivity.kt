package com.kakao.bikeseoulfinder.ui.favorite

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kakao.bikeseoulfinder.R
import com.kakao.bikeseoulfinder.disposed
import com.kakao.bikeseoulfinder.ui.Constants.Companion.STATION_URL
import com.kakao.bikeseoulfinder.ui.main.MainActivity
import kotlinx.android.synthetic.main.favorite_station_list_activity.*

class FavoriteStationListActivity : AppCompatActivity() {

    private lateinit var viewModel: FavoriteStationViewModel

    private val adapter = FavoriteListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.favorite_station_list_activity)

        list_favorite_station.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        list_favorite_station.adapter = adapter

        adapter.itemClick.subscribe {
            val stationUrl = STATION_URL + "?lat=${it.latitude}&lng=${it.longitude}"
            val intent = Intent(this, MainActivity::class.java)
            intent.data = Uri.parse(stationUrl)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }.disposed(this)

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