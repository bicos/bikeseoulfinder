package com.kakao.bikeseoulfinder.ui.favorite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.kakao.bikeseoulfinder.model.BikeStation
import io.reactivex.subjects.PublishSubject

class FavoriteListAdapter : RecyclerView.Adapter<FavoriteListAdapter.FavoriteViewHolder>() {

    val itemClick = PublishSubject.create<LatLng>()

    val bikeStationList: MutableList<BikeStation> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        return FavoriteViewHolder(LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false))
    }

    override fun getItemCount(): Int {
        return bikeStationList.size
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.setData(bikeStationList[position])
    }


    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var location : LatLng? = null

        var text1: TextView = itemView.findViewById(android.R.id.text1)
        var text2: TextView = itemView.findViewById(android.R.id.text2)

        init {
            itemView.setOnClickListener {
                location?.let {
                    itemClick.onNext(it)
                }
            }
        }

        fun setData(bikeStation: BikeStation) {
            location = LatLng(bikeStation.stationLatitude, bikeStation.stationLongitude)
            text1.text = bikeStation.stationName
            text2.text = "${bikeStation.rackTotCnt}대 남음"
        }

    }
}
