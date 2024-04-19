package com.example.runner.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import com.example.runner.OTHER.Constants
import com.example.runner.OTHER.Constants.ACTION_PAUSE_SERVICE
import com.example.runner.OTHER.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runner.OTHER.Constants.ACTION_STOP_SERVICE
import com.example.runner.OTHER.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runner.OTHER.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runner.OTHER.Constants.NOTIFICATION_ID
import com.example.runner.R
import com.example.runner.ui.MainActivity
import timber.log.Timber

class TrackingService: LifecycleService() {

    var isFirstRun=true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE->{
                    if (isFirstRun){
                        startForegroundService()
                        isFirstRun=false
                    }else{
                        Timber.d("Resuming service")
                    }
                }
                ACTION_PAUSE_SERVICE->{
                    Timber.d("paused service")
                }
                ACTION_STOP_SERVICE->{
                    Timber.d("stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    private fun startForegroundService(){
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