package com.example.runner.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

//abstract class to be used by roomDB for creating the database automatically
@Database(
    entities = [run::class],
    version = 1
)
@TypeConverters(converters::class)
abstract class RunningDatabase: RoomDatabase() {

    abstract fun getRunDao(): RunDAO
}