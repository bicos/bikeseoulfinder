package com.kakao.bikeseoulfinder.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
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
        left_drawer.adapter = ArrayAdapter<String>(this, R.layout.drawer_item, settingArray)
        left_drawer.setOnItemClickListener { _, _, position, _ ->
            if (position == 0) {
                startActivity(Intent(this@MainActivity, FavoriteStationListActivity::class.java))
            }
        }

        viewModel = ViewModelProviders.of(this, MainViewModelFactory(this))
                .get(MainViewModel::class.java)
    }
}
