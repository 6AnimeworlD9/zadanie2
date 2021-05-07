package com.example.zadanie2

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService


class LocationService : LifecycleService() {
    companion object{
        var running = false
    }
    private val locationChanged: (Location)->Unit = ::locationChanged
    private var locator: GpsHelper? = null


    private fun locationChanged(l: Location){
        LocationData.location.value = l
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.also {
            when (it.action) {
                Constants.START_LOCATION_SERVICE -> {
                    running = true
                    startForeground(intent)
                    locator = GpsHelper(applicationContext).apply{
                        startLocationListening(locationChanged)
                    }
                }
                Constants.STOP_LOCATION_SERVICE -> {
                    running = false
                    locator?.let {
                        it.stopLocating()
                        locator = null
                    }
                    stopService(intent)
                }
                else ->{}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun startForeground(service: Intent?) {
        //val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel()
            } else {
                ""
            }

        val notificationBuilder = NotificationCompat.Builder(this, channelId )
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.sym_def_app_icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
        startForeground(Constants.FOREGROUND_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String{
        val channelName = "Location Service"
        val chan = NotificationChannel(Constants.CHANNEL_ID,
            channelName, NotificationManager.IMPORTANCE_LOW)
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return Constants.CHANNEL_ID
    }
}