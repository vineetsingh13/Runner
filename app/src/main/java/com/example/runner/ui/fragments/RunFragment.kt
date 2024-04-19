package com.example.runner.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.runner.R
import com.example.runner.databinding.FragmentRunBinding
import com.example.runner.ui.viewModels.MainViewModel

import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run) {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentRunBinding

    private val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )

    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }

    private var permissionIndex = 0
    val req = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.e("permissions", "not granted")
            Log.e("permissions", "${permissions[permissionIndex]} granted")
            permissionIndex++
            if (permissionIndex < permissions.size) {
                startLocationPermissionRequest(permissions[permissionIndex])
            }
        } else {
            Log.e("permissions", "${permissions[permissionIndex]} not granted")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startLocationPermissionRequest(permissions[permissionIndex])
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRunBinding.inflate(layoutInflater)


        return binding.root
    }

    private fun startLocationPermissionRequest(permission: String) {
        req.launch(permission)
    }

}