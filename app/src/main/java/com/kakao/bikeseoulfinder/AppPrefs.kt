package com.kakao.bikeseoulfinder

import android.content.Context
import android.content.SharedPreferences

class AppPrefs(context: Context) {

    companion object {
        const val PREF_UPDATE_TIME = "update_time"
    }

    private val sharedPref = context.getSharedPreferences("AppPreference", Context.MODE_PRIVATE)

    fun getPref(): SharedPreferences {
        return sharedPref
    }

    fun getUpdateTime(): Long {
        return sharedPref.getLong(PREF_UPDATE_TIME, 0L)
    }

    fun setUpdateTime(updateTime: Long) {
        sharedPref.edit().putLong(PREF_UPDATE_TIME, updateTime).apply()
    }
}