package com.example.runner.OTHER

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng

typealias polyline=MutableList<LatLng>
typealias polylines=MutableList<polyline>

object TrackingSingleton {


    var pathPoints = MutableLiveData<polylines>()
    var allPath = mutableListOf<polylines>()
    val timeRunInMillis= MutableLiveData<Long>()
    var timeStarted=MutableLiveData<Long>()
    val isRunOn=MutableLiveData<Boolean>()
    // Add a new point to the latest polyline
//    fun addPathPoint(point: LatLng) {
//        if (pathPoints.isEmpty()) {
//            addEmptyPolyline()
//        }
//        pathPoints.last().add(point)
//    }
//
//    // Start a new polyline
//    fun addEmptyPolyline() {
//        pathPoints.add(mutableListOf())
//    }
//
//    // Clear all path points
//    fun clearPathPoints() {
//        pathPoints.clear()
//    }
}