package com.example.runner.services

import android.Manifest
import android.app.Notification
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
import com.example.runner.OTHER.Constants.TIMER_UPDATER_INTERVAL
import com.example.runner.OTHER.TrackingUtility
import com.example.runner.R
import com.example.runner.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias polyline=MutableList<LatLng>
typealias polylines=MutableList<polyline>

@AndroidEntryPoint
class TrackingService: LifecycleService() {

    var isFirstRun=true

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    //this is for updating the notifications because the notifications should not be updated that much frequently
    private val timeRunSeconds = MutableLiveData<Long>()

    @Inject
    lateinit var baseNotificationBuilder:NotificationCompat.Builder

    lateinit var currentNotificationBuilder: NotificationCompat.Builder

    companion object{

        //this time is for updating on the screen
        val timeRunInMillis=MutableLiveData<Long>()
        val isTracking=MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<polylines>()
    }

    private fun postInitialValues(){
        timeRunSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
    }

    override fun onCreate() {
        super.onCreate()
        currentNotificationBuilder=baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
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
                        startTimer()
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


    var isTimerEnabled=false

    //this variable is for storing the time when user starts and pause the run
    private var lapTime=0L

    //variable for total time
    private var timeRun=0L

    private var timeStarted=0L
    private var lastSecondTimestamp=0L


    //function for starting the timer
    private fun startTimer(){

        //we shifted this from startForegroundservice because we need to add empty lines
        //when the user starts first time or starts again after stopping
        //in the startForeground service it will not stop until run is complete
        addEmptyPolyline()

        isTracking.postValue(true)
        timeStarted=System.currentTimeMillis()
        isTimerEnabled=true

        CoroutineScope(Dispatchers.Main).launch{
            while (isTracking.value!!){
                //time difference between now and timestarted
                lapTime=System.currentTimeMillis()-timeStarted

                //post the new lap time
                timeRunInMillis.postValue(timeRun+lapTime)

                //so what this does is if the lastsecondtimestamp increases by 1 sec
                //means the value of timeruninmillis will be less
                //so we update seconds time and the timestamp
                if(timeRunInMillis.value!! >= lastSecondTimestamp+1000L){
                    timeRunSeconds.postValue(timeRunSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATER_INTERVAL)
            }
            //add the last lap time to our totaltime
            timeRun += lapTime
        }
    }

    //function to pause the service when the user clicks on stop button
    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled=false
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
        startTimer()
        isTracking.postValue(true)
        val notificationManager: NotificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        notificationManager.notify(NOTIFICATION_ID,baseNotificationBuilder.build())

        timeRunSeconds.observe(this, Observer {
            val notification=currentNotificationBuilder
                .setContentText(TrackingUtility.getFormattedStopWatchTime(it*1000L))

            notificationManager.notify(NOTIFICATION_ID,notification.build())
        })
    }


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


    private fun updateNotificationTrackingState(isTracking: Boolean){

        val notificationActionText=if(isTracking) "Pause" else "Resume"

        val pendingIntent=if(isTracking){
            val pauseIntent=Intent(this,TrackingService::class.java).apply {
                action= ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this,1,pauseIntent,PendingIntent.FLAG_IMMUTABLE)
        }else{
            val resumeIntent=Intent(this, TrackingService::class.java).apply {
                action= ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this,2,resumeIntent,PendingIntent.FLAG_IMMUTABLE)
        }

        val notificationManager: NotificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible=true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        currentNotificationBuilder=baseNotificationBuilder
            .addAction(R.drawable.ic_pause_black,notificationActionText,pendingIntent)

        notificationManager.notify(NOTIFICATION_ID,currentNotificationBuilder.build())

    }
}