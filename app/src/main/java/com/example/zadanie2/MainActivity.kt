package com.example.zadanie2

import android.Manifest
import android.R.id
import android.widget.Toolbar
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity: AppCompatActivity(),OnMapReadyCallback{
    private lateinit var startStopBtn: FloatingActionButton
    private lateinit var output: LinearLayout
    private var mapView: MapView? = null
    private var gmap: GoogleMap? = null
    private var lat = 40.7143528
    private var long= -74.0059731

    companion object {
        private const val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startStopBtn = findViewById<FloatingActionButton>(R.id.startStopBtn).apply{
            setOnClickListener {
                if (
                        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                ) {
                    requestLocationPermission()
                } else {
                    changeServiceState()
                }
            }
        }
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }

        mapView = findViewById(R.id.mapView1)
        mapView?.onCreate(mapViewBundle)
        mapView?.getMapAsync(this)
    }
    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }
        mapView!!.onSaveInstanceState(mapViewBundle)
    }
    private fun requestLocationPermission() {
        this.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        gmap = googleMap
        gmap?.setMinZoomPreference(12f)
        val ny = LatLng(lat, long)
        gmap?.moveCamera(CameraUpdateFactory.newLatLng(ny))
    }
    fun onMapReady1(googleMap: GoogleMap?) {
        gmap = googleMap
        gmap?.setMinZoomPreference(12f)
        val ny = LatLng(lat, long)
        gmap?.moveCamera(CameraUpdateFactory.newLatLng(ny))
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0
                && permissions.first() == Manifest.permission.ACCESS_FINE_LOCATION
                && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
            changeServiceState(true)
        }
    }

    private fun sendCommand(command: String){
        val intent = Intent(this, LocationService::class.java).apply {
            this.action = command
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private val locationObserver: (Location) -> Unit = ::locationChanged

    private fun locationChanged(l: Location){
        /*val tv = TextView(this@MainActivity)
        tv.textSize = 18F
        tv.text = getString(R.string.position, l.latitude, l.longitude)
        output.addView(tv)*/
        lat=l.latitude
        long=l.longitude
        onMapReady1(null)
    }

    private fun changeServiceState(forceStart: Boolean = false) {
        if (!LocationService.running || forceStart) {
            sendCommand(Constants.START_LOCATION_SERVICE)
            LocationData.location.observe(this, locationObserver)
        } else {
            sendCommand(Constants.STOP_LOCATION_SERVICE)
            LocationData.location.removeObservers(this)
        }
    }

    override fun onPause() {
        super.onPause()
        LocationData.location.removeObserver(locationObserver)
        mapView!!.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (LocationService.running)
            LocationData.location.observe(this, locationObserver)
        mapView!!.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
    }


    override fun onDestroy() {
        mapView!!.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }
}



