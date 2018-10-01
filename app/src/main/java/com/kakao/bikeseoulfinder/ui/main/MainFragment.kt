package com.kakao.bikeseoulfinder.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.VisibleRegion
import com.google.android.gms.tasks.OnSuccessListener
import com.google.maps.android.MarkerManager
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.data.Renderer
import com.kakao.bikeseoulfinder.*
import com.kakao.bikeseoulfinder.AppPrefs.Companion.PREF_UPDATE_TIME
import com.kakao.bikeseoulfinder.model.BikeStation
import com.kakao.bikeseoulfinder.network.ApiManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.main_fragment.*
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*


private const val REQ_GET_CURRENT_LOCATION = 1000

private const val DEFAULT_ZOOM = 15f

private val DEFAULT_LAT_LON = LatLng(37.5640907, 126.99794029999998)

class MainFragment : Fragment(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    private lateinit var map: GoogleMap

    private var googleApiClient: GoogleApiClient? = null

    private var clusterManager: ClusterManager<BikeStationItem>? = null

    private lateinit var resultReceiver: AddressResultReceiver

    private var listener: SharedPreferences.OnSharedPreferenceChangeListener? = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == PREF_UPDATE_TIME) {
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.KOREA)
            val currentDate = sdf.format(Date(MainApplication.pref.getUpdateTime()))
            Toast.makeText(context, "데이터가 업데이트 되었습니다. $currentDate", Toast.LENGTH_SHORT).show()
            getNearByStationAndShowMarkers(map.projection.visibleRegion)
        }
    }

    val compositeDisposable : CompositeDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        context?.let {
            viewModel = ViewModelProviders.of(this, MainViewModelFactory(it)).get(MainViewModel::class.java)
        }
        resultReceiver = AddressResultReceiver(Handler())
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

    override fun onPause() {
        compositeDisposable.clear()
        super.onPause()
    }

    override fun onDestroy() {
        MainApplication.pref.getPref().unregisterOnSharedPreferenceChangeListener(listener)
        compositeDisposable.dispose()
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
        clusterManager?.renderer = BikeStationRenderer(context, map, clusterManager)
        clusterManager?.setOnClusterItemInfoWindowClickListener { bikeStationItem ->
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("geo:${bikeStationItem.position.latitude}, ${bikeStationItem.position.longitude}")
            startActivity(intent)
        }

        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener(clusterManager)
        map.setOnInfoWindowClickListener(clusterManager)

        viewModel.observeAll().observe(this, Observer {
            updateUiFromVisibleRegion()
        })

        context?.let { context ->
            if (!EasyPermissions.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                EasyPermissions.requestPermissions(this, "현재 위치를 가져오기 위해 위치 접근 권한이 필요합니다.", REQ_GET_CURRENT_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                isLocationAvailable(context, OnSuccessListener {
                    handleLocationAvailable(it.isLocationAvailable)
                })
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        context?.let {
            isLocationAvailable(it, OnSuccessListener {
                handleLocationAvailable(it.isLocationAvailable)
            })
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        setMyLocationUi(false)
    }

    private fun handleLocationAvailable(isLocationAvailable: Boolean) {
        setMyLocationUi(isLocationAvailable)
        moveCurrentLocation()
    }

    @SuppressLint("MissingPermission")
    private fun setMyLocationUi(enableMyLocation: Boolean) {
        map.isMyLocationEnabled = enableMyLocation
        map.uiSettings?.isMyLocationButtonEnabled = enableMyLocation
    }

    @SuppressLint("MissingPermission")
    private fun isLocationAvailable(context: Context, callback: OnSuccessListener<LocationAvailability>) {
        LocationServices.getFusedLocationProviderClient(context).locationAvailability.addOnSuccessListener(callback)
    }

    @SuppressLint("MissingPermission")
    private fun moveCurrentLocation() {
        context?.let { context ->
            LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), DEFAULT_ZOOM))
                } else {
                    Toast.makeText(context, "현재 위치를 가져올 수 없습니다. 기본 위치로 설정합니다.", Toast.LENGTH_SHORT).show()
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LAT_LON, DEFAULT_ZOOM))
                }
                this.getNearByStationAndShowMarkers(map.projection.visibleRegion)
            }
        }
    }

    private fun getNearByStationAndShowMarkers(region: VisibleRegion) {
        val intent = Intent(context, FetchAddressIntentService::class.java).apply {
            putExtra(Constants.RECEIVER, resultReceiver)
            putExtra(Constants.LOCATION_DATA_EXTRA, region.latLngBounds.center)
        }
        context?.startService(intent)
    }

    private fun updateUiFromVisibleRegion() {
        val northeast = map.projection.visibleRegion.latLngBounds.northeast
        val southwest = map.projection.visibleRegion.latLngBounds.southwest

        viewModel.observeNearByStation(southwest.latitude, northeast.latitude, southwest.longitude, northeast.longitude)
                .observe(this@MainFragment, Observer {
                    viewModel.stationList?.removeObservers(this)

                    it?.forEach { bikeStation ->
                        val item = BikeStationItem(bikeStation)
                        clusterManager?.removeItem(item)
                        clusterManager?.addItem(item)
                    }
                    clusterManager?.cluster()
                })
    }

    internal inner class AddressResultReceiver(handler: Handler) : ResultReceiver(handler) {

        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            if (compositeDisposable.isDisposed) return

            val addressOutput = resultData?.getString(Constants.RESULT_DATA_KEY) ?: ""
            val grpReq = Utils.getFrpSeq(addressOutput)

            val disposable = ApiManager.instance.getRealTimeBikeStations(grpReq)
                    .map { viewModel.dao.insertAll(it.realtimeList) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        // do nothing
                    }) {
                        if (BuildConfig.DEBUG) {
                            Log.i("getRealTimeBikeStations", it.message, it)
                        }

                        context?.let {
                            Toast.makeText(context, "네트워크가 원활하지 않아 그 전 위치정보를 불러옵니다.", Toast.LENGTH_SHORT).show()
                            updateUiFromVisibleRegion()
                        }
                    }

            compositeDisposable.add(disposable)
        }
    }


    class BikeStationRenderer(context: Context?, map: GoogleMap?, clusterManager: ClusterManager<BikeStationItem>?) : DefaultClusterRenderer<BikeStationItem>(context, map, clusterManager) {



    }
}
