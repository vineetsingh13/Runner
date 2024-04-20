package com.example.runner.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationRequest
import android.os.Build
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.runner.OTHER.Constants
import com.example.runner.OTHER.Constants.ACTION_PAUSE_SERVICE
import com.example.runner.OTHER.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runner.OTHER.Constants.ACTION_STOP_SERVICE
import com.example.runner.OTHER.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runner.OTHER.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runner.OTHER.Constants.NOTIFICATION_ID
import com.example.runner.R
import com.example.runner.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber

typealias polyline=MutableList<LatLng>
typealias polylines=MutableList<polyline>
class TrackingService: LifecycleService() {

    var isFirstRun=true

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object{
        val isTracking=MutableLiveData<Boolean>()

        val pathPoints = MutableLiveData<polylines>()

    }

    private fun postInitialValues(){

        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE->{
                    if (isFirstRun){
                        startForegroundService()
                        isFirstRun=false
                    }else{
                        Timber.d("Resuming service")
                        startForegroundService()
                    }
                }
                ACTION_PAUSE_SERVICE->{
                    Timber.d("paused service")
                    pauseService()
                }
                ACTION_STOP_SERVICE->{
                    Timber.d("stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    private fun pauseService(){
        isTracking.postValue(false)
    }

    //what this functions does is if the tracking is true
    //we want to receive location updates and if its not then we dont
    private fun updateLocationTracking(isTracking: Boolean){
        if(isTracking){
            val request=com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,2000)
                .setIntervalMillis(2000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(2000)
                .build()

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this,"Enable Location Permissions", Toast.LENGTH_SHORT).show()
                return
            }
            fusedLocationProviderClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        }else{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    //used for getting continuous location updates or changes
    //so basic structure with locationCallBack we get the new or updated location
    //using addPathPoint we add the new location at the back of our list
    //then we add the list to our list
    val locationCallback=object: LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)

            if(isTracking.value!!){
                result.locations.let {
                    locations -> for (location in locations){
                        addPathPoint(location)
                        Timber.d("new location: ${location.latitude},${location.longitude}")
                    }
                }
            }
        }
    }


    //in this function what we are doing is
    //the user walks so a location comes so we add the location's lat and long to our list
    //then we add that list to pathPoints list
    private fun addPathPoint(location: Location?){
        location?.let {
            val pos=LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    //what this function does is
    //when the user stops the tracking and walks certain mile then that distance would not be considered
    //so instead of adding coordinated we add empty list when the user stops a run
    //also if the initial value is null we add the mutable list of list of coordiantes
    private fun addEmptyPolyline()= pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)

    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))



    private fun startForegroundService(){

        addEmptyPolyline()
        isTracking.postValue(true)
        val notificationManager: NotificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        //set on going true means we cant swipe it
        val notificationBuilder=NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.baseline_directions_run_24)
            .setContentTitle("Running App")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())
            .build()


        notificationManager.notify(NOTIFICATION_ID,notificationBuilder)
    }

    //flag update current means it will update the current intent instead of creating a new one
    private fun getMainActivityPendingIntent()= PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also{
            it.action= Constants.ACTION_SHOW_TRACKING_FRAGMENT
        },
        PendingIntent.FLAG_IMMUTABLE
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        //WE SET OUT IMPORTANCELOW BECAUSE IF WE SET IT HIGH EVERY TIME AN UPDATE COMES IT WILL RING
        //BUT WE DONT WANT THAT BECAUSE WE ARE CONTINUOUSLY SENDING UPDATES TO OUR NOTIFICATION
        val channel=NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }
}