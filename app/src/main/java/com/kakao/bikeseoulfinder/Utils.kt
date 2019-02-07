package com.kakao.bikeseoulfinder

import android.R
import android.app.Activity
import android.widget.SimpleAdapter
import androidx.appcompat.app.AlertDialog

class Utils {
    companion object {
        fun getFrpSeq(addr: String): Int {
            when {
                addr.contains("종로구") -> return 10
                addr.contains("중구") -> return 11
                addr.contains("마포구") -> return 12
                addr.contains("서대문구") -> return 13
                addr.contains("성동구") -> return 14
                addr.contains("광진구") -> return 15
                addr.contains("동대문구") -> return 16
                addr.contains("영등포구") -> return 17
                addr.contains("양천구") -> return 18
                addr.contains("용산구") -> return 19
                addr.contains("은평구") -> return 20
                addr.contains("강동구") -> return 21
                addr.contains("강서구") -> return 22
                addr.contains("송파구") -> return 23
                addr.contains("성북구") -> return 24
                addr.contains("중랑구") -> return 25
                addr.contains("노원구") -> return 26
                addr.contains("도봉구") -> return 27
                addr.contains("금천구") -> return 28
                addr.contains("구로구") -> return 29
                addr.contains("동작구") -> return 30
                addr.contains("관악구") -> return 31
                addr.contains("서초구") -> return 32
                addr.contains("강남구") -> return 33
                addr.contains("강북구") -> return 34
                else -> return 10
            }
        }

        fun showListDialog(activity: Activity?, title: String = "", list : List<String>, action: (String) -> Unit) {
            activity?: return

            val mapList = list.map { s -> mapOf(Pair("name", s)) }

            val adapter = SimpleAdapter(activity, mapList, R.layout.simple_list_item_1,
                    arrayOf("name"), intArrayOf(R.id.text1))

            AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setAdapter(adapter) { _, which ->
                        action.invoke(list[which])
                    }.show()
        }
    }
}