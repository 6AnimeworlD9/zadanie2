package com.example.zadanie2

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity: AppCompatActivity(),OnMapReadyCallback{
    private lateinit var startStopBtn: FloatingActionButton
    private lateinit var findMe: FloatingActionButton
    private var mapView: MapView? = null
    private var gmap: GoogleMap? = null
    private var lat=0.0
    private var long=0.0
    private var typeMap=1

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
        {lat = l.latitude
        long = l.longitude}
        gmap?.clear()
        val t = findViewById<TextView>(R.id.textView)
        val t1 = findViewById<TextView>(R.id.textView2)
        val new = LatLng(lat, long)
        t.text = "Ширина: " + lat.toString()
        t1.text = "Долгота: " + long.toString()
        gmap?.moveCamera(CameraUpdateFactory.newLatLng(new))
        val markerOptions = MarkerOptions()
        markerOptions.position(new)
        markerOptions.title("Вы здесь")
        gmap?.addMarker(markerOptions)
        notificationUpdate()
    }

    fun notificationUpdate()
    {
        val nManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(this, Constants.CHANNEL_ID )
        val notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentText("Ширина: " + lat.toString()+'\n'+"Долгота: " + long.toString())
                .setContentTitle("Ваши координаты")
                .build()
        nManager.notify(Constants.FOREGROUND_ID,notification)
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


    fun measureDistanceClick(view: View) {}
    fun goToSettingsClick(view: View) {
        var i=Bundle()
        val p = Intent(this, Settings::class.java)
        i.putString("typeMap", typeMap.toString())
        p.putExtras(i)
        startActivityForResult(p,1)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        finishActivity(1)
        if(data!=null) {
            typeMap = data.getStringExtra("typeMap")?.toInt() ?: 1
            if(typeMap==1)
                gmap?.setMapType(GoogleMap.MAP_TYPE_NORMAL)
            else
                gmap?.setMapType(GoogleMap.MAP_TYPE_HYBRID)
        }
    }
}



