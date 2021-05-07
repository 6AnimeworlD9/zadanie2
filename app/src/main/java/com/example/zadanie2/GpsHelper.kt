package com.example.zadanie2
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
class GpsHelper(val context: Context) : LocationListener{
    private var locationManager: LocationManager? = null
    private var locationUpdater: ((Location)->Unit)? = null
    fun startLocationListening(locationUpdater: (Location)->Unit){
        this.locationUpdater = locationUpdater
        locationManager = (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager)
        requestUpdates()
    }
    private fun requestUpdates() {
        if (
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
            || context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1F,
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
    override fun onProviderEnabled(provider: String) {
        Toast.makeText(context, "GPS включен,сервис сможет обнаружить ваше местоположение", Toast.LENGTH_LONG).show()
    }
    override fun onProviderDisabled(provider: String) {
        Toast.makeText(context, "GPS отключен,сервис не сможет обнаружить ваше местоположение", Toast.LENGTH_LONG).show()
    }
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }
}