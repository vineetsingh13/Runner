package com.example.runner.ui.viewModels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runner.OTHER.SortType
import com.example.runner.db.run
import com.example.runner.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.launch
import javax.inject.Inject

//the job of viewModel is to collect the data from the MainRepository and provide to all the fragments and UI COMPONENTS THAT NEED IT

//here we dont need to create a provide for viewModelFactory because the mainRepository needs the runDAO
//we have already provided how to create the runDAO hence the mainRepository would be created
//so we dont need to write a provide for mainRepository
@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository
): ViewModel() {

    fun insertRun(run: run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }

    private val runSortedByDate=mainRepository.getAllRunsSortedByDate()

    private val runSortedByDistance=mainRepository.getAllRunsSortedByDistance()

    private val runSortedByCalories=mainRepository.getAllRunsSortedByCalories()

    private val runSortedByTime=mainRepository.getAllRunsSortedBytime()

    private val runSortedBySpeed=mainRepository.getAllRunsSortedBySpeed()

    val runs=MediatorLiveData<List<run>>()

    var sortType=SortType.DATE

    init {
        //so for runs livedata we add a source runsortedbydate
        //now the lambda function is called every time there is a change in runsortedbydate data
        //and if our condition is true we set value emit by our lambda block
        runs.addSource(runSortedByDate){result->
            if(sortType==SortType.DATE){
                result?.let {
                    runs.value=it
                }
            }
        }

        runs.addSource(runSortedByCalories){result->
            if(sortType==SortType.CALORIES_BURNED){
                result?.let {
                    runs.value=it
                }
            }
        }

        runs.addSource(runSortedBySpeed){result->
            if(sortType==SortType.AVG_SPEED){
                result?.let {
                    runs.value=it
                }
            }
        }

        runs.addSource(runSortedByTime){result->
            if(sortType==SortType.RUNNING_TIME){
                result?.let {
                    runs.value=it
                }
            }
        }

        runs.addSource(runSortedByDistance){result->
            if(sortType==SortType.DISTANCE){
                result?.let {
                    runs.value=it
                }
            }
        }
    }

    fun sortRuns(sortType: SortType)=when(sortType){
        SortType.DATE -> runSortedByDate.value?.let { runs.value=it }
        SortType.CALORIES_BURNED -> runSortedByCalories.value?.let { runs.value=it }
        SortType.AVG_SPEED -> runSortedBySpeed.value?.let { runs.value=it }
        SortType.RUNNING_TIME -> runSortedByTime.value?.let { runs.value=it }
        SortType.DISTANCE -> runSortedByDistance.value?.let { runs.value=it }
    }.also {
        this.sortType=sortType
    }
}