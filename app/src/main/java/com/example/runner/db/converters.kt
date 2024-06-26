package com.example.runner.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class converters {

    //the below methods are for storing image in roomdb format
    //adding typeconverter makes room auto search to find the required conversion to store the complex data

    @TypeConverter
    fun toBitmap(byteArray: ByteArray):Bitmap{
        return BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
    }

    @TypeConverter
    fun fromBitmap(bmp: Bitmap): ByteArray{
        val outputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG,100,outputStream)

        return outputStream.toByteArray()
    }
}