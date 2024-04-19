package com.example.runner.repositories

import com.example.runner.db.RunDAO
import com.example.runner.db.run
import javax.inject.Inject


//in the MVVM architecture the job of MainRepository is to collect data from all sources
//APIs, roomDB, etc
class MainRepository @Inject constructor(
    val runDAO: RunDAO
) {

    suspend fun insertRun(run: run) = runDAO.insertRun(run)

    suspend fun deleteRun(run: run) = runDAO.deleteRun(run)

    fun getAllRunsSortedByDate() = runDAO.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance()=runDAO.getAllRunsSortedByDistance()

    fun getAllRunsSortedBytime()=runDAO.getAllRunsSortedByTimeMillis()

    fun getAllRunsSortedBySpeed()=runDAO.getAllRunsSortedBySpeed()

    fun getAllRunsSortedByCalories()=runDAO.getAllRunsSortedBycalories()

    fun getTotalAvgSpeed()=runDAO.getTotalAvgSpeed()

    fun getTotalCalories()=runDAO.getTotalCaloriesBurned()

    fun getTotalDistance()=runDAO.getTotalDistance()

    fun getTotalTime()=runDAO.getTotalTime()
}