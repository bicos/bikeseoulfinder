package com.kakao.bikeseoulfinder.ui.main

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import com.kakao.bikeseoulfinder.R
import com.kakao.bikeseoulfinder.ui.favorite.FavoriteStationListActivity
import kotlinx.android.synthetic.main.main_activity.*

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

        val settingArray = resources.getStringArray(R.array.setting_array)
        val adapter = ArrayAdapter<String>(this, R.layout.drawer_item, settingArray)
        left_drawer.adapter = adapter
        left_drawer.setOnItemClickListener { parent, view, position, id ->
            if (position == 0) {
                startActivity(Intent(this@MainActivity, FavoriteStationListActivity::class.java))
            }
        }

        viewModel = ViewModelProviders.of(this, MainViewModelFactory(this))
                .get(MainViewModel::class.java)
    }
}
