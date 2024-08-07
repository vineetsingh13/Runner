package com.example.runner.ui.fragments

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runner.OTHER.Constants.KEY_NAME
import com.example.runner.OTHER.SortType
import com.example.runner.R
import com.example.runner.adapters.RunAdapter
import com.example.runner.databinding.FragmentRunBinding
import com.example.runner.ui.viewModels.MainViewModel
import com.google.android.material.textview.MaterialTextView

import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run){

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var sharedPref: SharedPreferences


    lateinit var name: String

    private lateinit var runAdapter: RunAdapter
    private lateinit var binding: FragmentRunBinding

    /*private val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            //Manifest.permission.ACCESS_BACKGROUND_LOCATION
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
    }*/



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        Log.d("NAMES",name)
        val toolbarText = "Let's go, $name!"
        requireActivity().findViewById<MaterialTextView>(R.id.tvToolbarTitle).text = toolbarText

        when(viewModel.sortType){
            SortType.DATE -> binding.spFilter.setSelection(0)
            SortType.RUNNING_TIME -> binding.spFilter.setSelection(1)
            SortType.DISTANCE -> binding.spFilter.setSelection(2)
            SortType.AVG_SPEED -> binding.spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> binding.spFilter.setSelection(4)
        }

        binding.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                when(position){
                    0 -> viewModel.sortRuns(SortType.DATE)
                    1 -> viewModel.sortRuns(SortType.RUNNING_TIME)
                    2 -> viewModel.sortRuns(SortType.DISTANCE)
                    3 -> viewModel.sortRuns(SortType.AVG_SPEED)
                    4 -> viewModel.sortRuns(SortType.CALORIES_BURNED)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }


        }

        viewModel.runs.observe(viewLifecycleOwner, Observer {
            runAdapter.submitList(it)
        })

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        name= sharedPref.getString(KEY_NAME,"").toString()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRunBinding.inflate(layoutInflater)


        return binding.root
    }


    private fun setupRecyclerView()=binding.rvRuns.apply {
        runAdapter=RunAdapter()
        adapter=runAdapter
        layoutManager=LinearLayoutManager(requireContext())
    }



}