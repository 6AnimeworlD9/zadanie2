package com.example.zadanie2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi

class GpsHelper private constructor() : LocationListener{

    private var locationManager: LocationManager? = null
    private var locationUpdater: ((Location)->Unit)? = null

    companion object{
        private var instance: GpsHelper? = null
        fun getInstance() = instance ?: GpsHelper().also { instance = it }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun startLocationListening(context: Context, locationUpdater: (Location)->Unit){
        locationManager = (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager)
            .also {   locationManager ->

                if (
                    context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                this.locationUpdater = locationUpdater
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    30F,
                    this
                )
            }
    }

    fun stopLocating(){
        locationManager?.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        locationUpdater?.invoke(location)
    }

}