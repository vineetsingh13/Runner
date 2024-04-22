package com.example.runner.ui.viewModels

import androidx.lifecycle.ViewModel
import com.example.runner.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val mainRepository: MainRepository
): ViewModel() {

    val totalTimeRun=mainRepository.getTotalTime()
    val totalDistance=mainRepository.getTotalDistance()
    val totalCalories=mainRepository.getTotalCalories()
    val totalavgSpeed=mainRepository.getTotalAvgSpeed()

    val runSortedByDate=mainRepository.getAllRunsSortedByDate()
}