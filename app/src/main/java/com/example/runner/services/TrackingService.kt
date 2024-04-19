package com.example.runner.services

import android.content.Intent
import androidx.lifecycle.LifecycleService
import com.example.runner.OTHER.Constants.ACTION_PAUSE_SERVICE
import com.example.runner.OTHER.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runner.OTHER.Constants.ACTION_STOP_SERVICE
import timber.log.Timber

class TrackingService: LifecycleService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE->{
                    Timber.d("started or resumed service")
                }
                ACTION_PAUSE_SERVICE->{
                    Timber.d("paused service")
                }
                ACTION_STOP_SERVICE->{
                    Timber.d("stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
}