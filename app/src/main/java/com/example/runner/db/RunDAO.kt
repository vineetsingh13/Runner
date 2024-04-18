package com.example.runner.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RunDAO {

    //on conflict replace means the old values will be replaced if similar comes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: run)

    @Delete
    suspend fun deleteRun(run: run)

    @Query("SELECT * FROM running_table ORDER BY timestamp DESC")
    fun getAllRunsSortedByDate(): LiveData<List<run>>

    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC")
    fun getAllRunsSortedByTimeMillis(): LiveData<List<run>>

    @Query("SELECT * FROM running_table ORDER BY caloriesBurned DESC")
    fun getAllRunsSortedBycalories(): LiveData<List<run>>

    @Query("SELECT * FROM running_table ORDER BY avgSpeed DESC")
    fun getAllRunsSortedBySpeed(): LiveData<List<run>>

    @Query("SELECT * FROM running_table ORDER BY distanceMeters DESC")
    fun getAllRunsSortedByDistance(): LiveData<List<run>>

    @Query("SELECT SUM(timeInMillis) FROM running_table")
    fun getTotalTime():LiveData<Long>

    @Query("SELECT SUM(caloriesBurned) FROM running_table")
    fun getTotalCaloriesBurned():LiveData<Int>

    @Query("SELECT SUM(distanceMeters) FROM running_table")
    fun getTotalDistance():LiveData<Int>

    @Query("SELECT AVG(avgSpeed) FROM running_table")
    fun getTotalAvgSpeed():LiveData<Float>
}