package com.example.runner.services

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class DefaultLocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient
):LocationClient {

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<Location> {


        return callbackFlow {
            val request=LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,2000)
                .setMinUpdateIntervalMillis(2000)


            val locationCallback=object: LocationCallback(){

                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    p0.locations.lastOrNull()?.let { location->
                        launch { send(location) }
                    }

                }
            }

            client.requestLocationUpdates(
                request.build(),
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

}