package com.example.runner.ui.viewModels

import androidx.lifecycle.ViewModel
import com.example.runner.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val mainRepository: MainRepository
): ViewModel() {
}