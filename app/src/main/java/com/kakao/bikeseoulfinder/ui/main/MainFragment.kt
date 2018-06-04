package com.kakao.bikeseoulfinder.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
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
import com.kakao.bikeseoulfinder.R
import kotlinx.android.synthetic.main.main_fragment.*
import pub.devrel.easypermissions.EasyPermissions


private const val REQ_GET_CURRENT_LOCATION = 1000

private const val DEFAULT_ZOOM = 15f

class MainFragment : Fragment(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    private var map: GoogleMap? = null

    private var googleApiClient: GoogleApiClient? = null

    private var clusterManager: ClusterManager<BikeStationItem>? = null

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
            map?.let {
                it.clear()
                clusterManager?.clearItems()
                getNearByStationAndShowMarkers(it.projection.visibleRegion)
            }
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(context, p0.errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onConnected(p0: Bundle?) {

    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onMapReady(map: GoogleMap?) {
        this.map = map
        this.map?.let {
            clusterManager = ClusterManager(context, it, MarkerManager(it))
            clusterManager?.setOnClusterItemInfoWindowClickListener { bikeStationItem ->
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("geo:${bikeStationItem.position.latitude}, ${bikeStationItem.position.longitude}")
                startActivity(intent)
            }

            it.setOnCameraIdleListener (clusterManager)
            it.setOnMarkerClickListener(clusterManager)
            it.setOnInfoWindowClickListener(clusterManager)

            getNearByStationAndShowMarkers(it.projection.visibleRegion)
        }

        val perm = Manifest.permission.ACCESS_FINE_LOCATION

        context?.let {
            if (EasyPermissions.hasPermissions(it, perm)) {
                setMyLocationUi(true)
                moveCurrentLocation()
            } else {
                EasyPermissions.requestPermissions(this, "현재 위치를 가져오기 위해 위치 접근 권한이 필요합니다.", REQ_GET_CURRENT_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getNearByStationAndShowMarkers(region: VisibleRegion) {
        val northeast = region.latLngBounds.northeast
        val southwest = region.latLngBounds.southwest

        viewModel.dao.getNearByStations(southwest.latitude, northeast.latitude, southwest.longitude, northeast.longitude)
                .observe(this@MainFragment, Observer {
                    it?.forEach { bikeStation ->
                        clusterManager?.addItem(BikeStationItem(bikeStation))
                    }
                    clusterManager?.cluster()
                })
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
        map?.isMyLocationEnabled = enableMyLocation
        map?.uiSettings?.isMyLocationButtonEnabled = enableMyLocation
    }

    @SuppressLint("MissingPermission")
    private fun moveCurrentLocation() {
        context?.let {
            LocationServices.getFusedLocationProviderClient(it).lastLocation.addOnSuccessListener {
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), DEFAULT_ZOOM))
            }
        }
    }

}
