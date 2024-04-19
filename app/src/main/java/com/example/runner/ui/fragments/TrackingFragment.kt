package com.example.runner.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.runner.OTHER.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runner.R
import com.example.runner.databinding.FragmentTrackingBinding
import com.example.runner.services.TrackingService
import com.example.runner.ui.viewModels.MainViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class TrackingFragment: Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentTrackingBinding

    private var map: GoogleMap?=null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)

        binding.btnToggleRun.setOnClickListener {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }


        binding.mapView.getMapAsync {
            map=it
        }
    }

    private fun sendCommandToService(action:String)=
        Intent(requireContext(), TrackingService::class.java).also{
            it.action=action
            requireContext().startService(it)
        }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentTrackingBinding.inflate(layoutInflater)

        return binding.root
    }
}