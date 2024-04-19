package com.example.runner.ui.viewModels

import androidx.lifecycle.ViewModel
import com.example.runner.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

//the job of viewModel is to collect the data from the MainRepository and provide to all the fragments and UI COMPONENTS THAT NEED IT

//here we dont need to create a provide for viewModelFactory because the mainRepository needs the runDAO
//we have already provided how to create the runDAO hence the mainRepository would be created
//so we dont need to write a provide for mainRepository
@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository
): ViewModel() {

}