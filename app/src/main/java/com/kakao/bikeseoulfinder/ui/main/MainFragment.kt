package com.kakao.bikeseoulfinder.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
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
import com.google.android.gms.maps.model.MarkerOptions
import com.kakao.bikeseoulfinder.R
import io.reactivex.processors.PublishProcessor
import pub.devrel.easypermissions.EasyPermissions


private const val REQ_GET_CURRENT_LOCATION = 1000

private const val DEFAULT_ZOOM = 15f

private const val LAT_1KM : Double = 0.008983

private const val LON_1KM : Double = 0.015060

class MainFragment : Fragment(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    private var map: GoogleMap? = null

    private var googleApiClient: GoogleApiClient? = null

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
            it.setOnCameraIdleListener({
                val northeast = it.projection.visibleRegion.latLngBounds.northeast
                val southwest = it.projection.visibleRegion.latLngBounds.southwest

                viewModel.dao?.getNearByStations(southwest.latitude, northeast.latitude, southwest.longitude, northeast.longitude)
                        ?.observe(this@MainFragment, Observer {
                            it?.forEach {
                                it@map?.addMarker(MarkerOptions().title(it.name).position(LatLng(it.latitude, it.longitude)))
                            }
                        })
            })
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
