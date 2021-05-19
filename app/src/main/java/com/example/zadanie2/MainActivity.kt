package com.example.zadanie2

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity: AppCompatActivity(),OnMapReadyCallback{
    private lateinit var startStopBtn: FloatingActionButton
    private lateinit var findMe: FloatingActionButton
    private var mapView: MapView? = null
    private var gmap: GoogleMap? = null



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
        mapView?.onSaveInstanceState(mapViewBundle)
    }
    private fun requestLocationPermission() {
        this.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        gmap = googleMap
        gmap?.setMinZoomPreference(12f)
        gmap?.setIndoorEnabled(true)
        val uiSettings: UiSettings? = gmap?.getUiSettings()
        uiSettings?.isIndoorLevelPickerEnabled = true
        uiSettings?.isMyLocationButtonEnabled = true
        uiSettings?.isMapToolbarEnabled = true
        uiSettings?.isCompassEnabled = true
        uiSettings?.isZoomControlsEnabled = true
        findMe = findViewById<FloatingActionButton>(R.id.findMe).apply{
            setOnClickListener {
               locationChanged(null)
            }
        }
        if(Constants.typeMap==1)
            gmap?.setMapType(GoogleMap.MAP_TYPE_NORMAL)
        else
            gmap?.setMapType(GoogleMap.MAP_TYPE_HYBRID)
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

    private fun locationChanged(l: Location?){
        if(l!=null)
        {Constants.lat = l.latitude;Constants.long = l.longitude}
        gmap?.clear()
        val t = findViewById<TextView>(R.id.textView)
        val t1 = findViewById<TextView>(R.id.textView2)
        val new = LatLng(Constants.lat, Constants.long)
        if(Constants.typeMap==1){t.setTextColor(Color.BLACK);t1.setTextColor(Color.BLACK)}
        else{t.setTextColor(Color.YELLOW);t1.setTextColor(Color.YELLOW)}
        t.text = "Ширина: " + Constants.lat.toString()
        t1.text = "Долгота: " + Constants.long.toString()
        gmap?.moveCamera(CameraUpdateFactory.newLatLng(new))
        val markerOptions = MarkerOptions()
        markerOptions.position(new)
        markerOptions.title(Constants.titleM)
        if(Constants.colorM==1)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker( BitmapDescriptorFactory.HUE_RED))
        else if(Constants.colorM==2)
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker( BitmapDescriptorFactory.HUE_GREEN))
        else if(Constants.colorM==3) markerOptions.icon(BitmapDescriptorFactory.defaultMarker( BitmapDescriptorFactory.HUE_YELLOW))
        else markerOptions.icon(BitmapDescriptorFactory.defaultMarker( BitmapDescriptorFactory.HUE_BLUE))
        gmap?.addMarker(markerOptions)
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
        mapView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (LocationService.running)
            LocationData.location.observe(this, locationObserver)
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }


    override fun onDestroy() {
        mapView?.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }


    fun goToSettingsClick(view: View) {
        var i=Bundle()
        val p = Intent(this, Settings::class.java)
        i.putString("typeMap", Constants.typeMap.toString())
        i.putString("colorM", Constants.colorM.toString())
        i.putString("titleM", Constants.titleM.toString())
        p.putExtras(i)
        startActivityForResult(p, 1)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        finishActivity(1)
        if(data!=null) {
            Constants.typeMap = data.getStringExtra("typeMap")?.toInt() ?: 1
            if(Constants.typeMap==1)
                gmap?.setMapType(GoogleMap.MAP_TYPE_NORMAL)
            else
                gmap?.setMapType(GoogleMap.MAP_TYPE_HYBRID)
            Constants.colorM = data.getStringExtra("colorM")?.toInt() ?: 1
            Constants.titleM = data.getStringExtra("titleM")?:""
            Toast.makeText(this.applicationContext, "Некоторые изменения вступят в силу после смены местоположения", Toast.LENGTH_LONG).show()
        }
    }
}



