package com.example.runner.OTHER

import android.location.Location
import com.example.runner.services.polyline
import java.util.concurrent.TimeUnit

object TrackingUtility {

    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean=false): String{
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        if(!includeMillis) {
            return "${if(hours < 10) "0" else ""}$hours:" +
                    "${if(minutes < 10) "0" else ""}$minutes:" +
                    "${if(seconds < 10) "0" else ""}$seconds"
        }
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
        milliseconds /= 10
        return "${if(hours < 10) "0" else ""}$hours:" +
                "${if(minutes < 10) "0" else ""}$minutes:" +
                "${if(seconds < 10) "0" else ""}$seconds:" +
                "${if(milliseconds < 10) "0" else ""}$milliseconds"

    }


    fun calculatePolylineLength(polyline: polyline): Float{

        var distance=0f
        for(i in 0..polyline.size-2){
            val pos1=polyline[i]
            val pos2=polyline[i+1]

            val result=FloatArray(1)
            Location.distanceBetween(
                pos1.latitude,
                pos1.longitude,
                pos2.latitude,
                pos2.longitude,
                result
            )

            distance+=result[0]
        }

        return distance
    }
}