package com.kakao.bikeseoulfinder.ui.main

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.kakao.bikeseoulfinder.R
import com.kakao.bikeseoulfinder.ui.Constants.Companion.STATION_PATH
import com.kakao.bikeseoulfinder.ui.favorite.FavoriteStationListActivity
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val settingArray = resources.getStringArray(R.array.setting_array)
        left_drawer.adapter = ArrayAdapter<String>(this, R.layout.drawer_item, settingArray)
        left_drawer.setOnItemClickListener { _, _, position, _ ->
            if (position == 0) {
                startActivity(Intent(this@MainActivity, FavoriteStationListActivity::class.java))
            } else if (position == 1) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://bikeseoulfinder.firebaseapp.com/privacy_policy.html")))
            }
        }

        drawerToggle = ActionBarDrawerToggle(this, drawer_layout, R.string.drawer_open, R.string.drawer_close)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawer_layout.addDrawerListener(drawerToggle)

        viewModel = ViewModelProviders.of(this, MainViewModelFactory(this))
                .get(MainViewModel::class.java)

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val appLinkData = intent?.data

        if (appLinkData?.lastPathSegment == STATION_PATH) {
            val lat = appLinkData.getQueryParameter("lat")?.toDoubleOrNull()
            val lng = appLinkData.getQueryParameter("lng")?.toDoubleOrNull()

            if (lat == null || lng == null)
                return

            (supportFragmentManager.findFragmentById(R.id.container) as? MainFragment)
                    ?.moveCamera(lat, lng)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
