package com.kakao.bikeseoulfinder.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.VisibleRegion
import com.google.maps.android.MarkerManager
import com.google.maps.android.clustering.ClusterManager
import com.kakao.bikeseoulfinder.AppPrefs.Companion.PREF_UPDATE_TIME
import com.kakao.bikeseoulfinder.MainApplication
import com.kakao.bikeseoulfinder.R
import kotlinx.android.synthetic.main.main_fragment.*
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*


private const val REQ_GET_CURRENT_LOCATION = 1000

private const val DEFAULT_ZOOM = 15f

class MainFragment : Fragment(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    private lateinit var map: GoogleMap

    private var googleApiClient: GoogleApiClient? = null

    private var clusterManager: ClusterManager<BikeStationItem>? = null

    private var listener: SharedPreferences.OnSharedPreferenceChangeListener? = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == PREF_UPDATE_TIME) {
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.KOREA)
            val currentDate = sdf.format(Date(MainApplication.pref.getUpdateTime()))
            Toast.makeText(context, "데이터가 업데이트 되었습니다. $currentDate", Toast.LENGTH_SHORT).show()
            getNearByStationAndShowMarkers(map.projection.visibleRegion)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        context?.let {
            viewModel = ViewModelProviders.of(this, MainViewModelFactory(it)).get(MainViewModel::class.java)
        }
        (childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)?.getMapAsync(this)

        activity?.let {
            googleApiClient = GoogleApiClient.Builder(it)
                    .enableAutoManage(it /* FragmentActivity */,
                            this /* OnConnectionFailedListener */)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build().apply {
                        connect()
                    }
        }

        btn_search_bike_station.setOnClickListener {
            getNearByStationAndShowMarkers(map.projection.visibleRegion)
        }

        MainApplication.pref.getPref().registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onDestroy() {
        MainApplication.pref.getPref().unregisterOnSharedPreferenceChangeListener(listener)
        super.onDestroy()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(context, p0.errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onConnected(p0: Bundle?) {

    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onMapReady(map: GoogleMap?) {
        if (map == null) {
            Toast.makeText(context, "오류가 발생하였습니다. 잠시 후 다시 시도하여 주세요.", Toast.LENGTH_SHORT).show()
            activity?.finish()
            return
        }

        this.map = map

        clusterManager = ClusterManager(context, map, MarkerManager(map))
        clusterManager?.setOnClusterItemInfoWindowClickListener { bikeStationItem ->
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("geo:${bikeStationItem.position.latitude}, ${bikeStationItem.position.longitude}")
            startActivity(intent)
        }

        map.setOnCameraIdleListener (clusterManager)
        map.setOnMarkerClickListener(clusterManager)
        map.setOnInfoWindowClickListener(clusterManager)

        context?.let {
            if (EasyPermissions.hasPermissions(it, Manifest.permission.ACCESS_FINE_LOCATION)) {
                setMyLocationUi(true)
                moveCurrentLocation()
            } else {
                EasyPermissions.requestPermissions(this, "현재 위치를 가져오기 위해 위치 접근 권한이 필요합니다.", REQ_GET_CURRENT_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        setMyLocationUi(true)
        moveCurrentLocation()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        setMyLocationUi(false)
    }

    @SuppressLint("MissingPermission")
    private fun setMyLocationUi(enableMyLocation: Boolean) {
        map.isMyLocationEnabled = enableMyLocation
        map.uiSettings?.isMyLocationButtonEnabled = enableMyLocation
    }

    @SuppressLint("MissingPermission")
    private fun moveCurrentLocation() {
        context?.let {
            LocationServices.getFusedLocationProviderClient(it).lastLocation.addOnSuccessListener { location ->
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it@location.latitude, it@location.longitude), DEFAULT_ZOOM))
                getNearByStationAndShowMarkers(map.projection.visibleRegion)
            }
        }
    }

    private fun getNearByStationAndShowMarkers(region: VisibleRegion) {
        map.clear()
        clusterManager?.clearItems()

        val northeast = region.latLngBounds.northeast
        val southwest = region.latLngBounds.southwest

        viewModel.getNearByStations(southwest.latitude, northeast.latitude, southwest.longitude, northeast.longitude)
                .observe(this@MainFragment, Observer {
                    viewModel.stationList?.removeObservers(this)
                    it?.forEach { bikeStation ->
                        clusterManager?.addItem(BikeStationItem(bikeStation))
                    }
                    clusterManager?.cluster()
                })
    }

}
