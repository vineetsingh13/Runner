package com.example.runner.dependencyInjection

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.runner.OTHER.Constants
import com.example.runner.R
import com.example.runner.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    //only one instance is going to be there for the lifetime of service
    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app:Context
    )=LocationServices.getFusedLocationProviderClient(app)

    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
        @ApplicationContext app:Context
    )= PendingIntent.getActivity(
        app,
        0,
        Intent(app, MainActivity::class.java).also{
            it.action= Constants.ACTION_SHOW_TRACKING_FRAGMENT
        },
        PendingIntent.FLAG_IMMUTABLE
    )


    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext app:Context,
        pendingIntent: PendingIntent
    )= NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)
        //set on going true means we cant swipe it
        .setOngoing(true)
        .setSmallIcon(R.drawable.baseline_directions_run_24)
        .setContentTitle("Running App")
        .setContentText("00:00:00")
        .setContentIntent(pendingIntent)

}