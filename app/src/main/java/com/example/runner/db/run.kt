package com.example.runner.db

import android.graphics.Bitmap
import android.health.connect.datatypes.ActiveCaloriesBurnedRecord
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_table")
data class run(
    var img:Bitmap?=null,
    var timestamp:Long=0L,
    var avgSpeed:Float=0F,
    var distanceMeters:Int=0,
    var timeInMillis:Long=0L,
    var caloriesBurned:Int=0
) {

    @PrimaryKey(autoGenerate = true)
    var id:Int?=null
}