package com.example.runner.ui.fragments

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.runner.OTHER.Constants.ACTION_PAUSE_SERVICE
import com.example.runner.OTHER.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runner.OTHER.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runner.OTHER.Constants.ACTION_STOP_SERVICE
import com.example.runner.OTHER.Constants.KEY_WEIGHT
import com.example.runner.OTHER.Constants.MAP_ZOOM
import com.example.runner.OTHER.Constants.POLYLINE_COLOR
import com.example.runner.OTHER.Constants.POLYLINE_WIDTH
import com.example.runner.OTHER.TrackingUtility
import com.example.runner.R
import com.example.runner.databinding.FragmentTrackingBinding
import com.example.runner.services.TrackingService
import com.example.runner.services.polyline
import com.example.runner.ui.MainActivity
import com.example.runner.ui.viewModels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.round

const val CANCEL_TRACKING_DIALOG="CANCELDIALOG"
@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking),MenuProvider {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentTrackingBinding
    private var isTracking=false

    @Inject
    lateinit var sharedPref: SharedPreferences

    private var pathPoints= mutableListOf<polyline>()

    private var map: GoogleMap? = null

    private var currentTimeMillis=0L

    private var menu: Menu?=null

    var weight=80f

    var hasNotificationPermissionGranted = false

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            hasNotificationPermissionGranted = isGranted
            if (!isGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                            showNotificationPermissionRationale()
                        } else {
                            showSettingDialog()
                        }
                    }
                }
            }
        }

    private fun showSettingDialog() {
        MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle("Notification Permission")
            .setMessage("Notification permission is required, Please allow notification permission from setting")
            .setPositiveButton("Ok") { _, _ ->
                val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:${requireContext().packageName}")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNotificationPermissionRationale() {

        MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle("Alert")
            .setMessage("Notification permission is required, to show notification")
            .setPositiveButton("Ok") { _, _ ->
                if (Build.VERSION.SDK_INT >= 33) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)

        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        if (savedInstanceState!=null){
            val cancelTrackingDialog=parentFragmentManager.findFragmentByTag(CANCEL_TRACKING_DIALOG) as CancelTrackingDialog?

            cancelTrackingDialog?.setYesListener {
                stopRun()
            }
        }
        weight=sharedPref.getFloat(KEY_WEIGHT,80f)
        Log.d("WEIGHT", weight.toString())

        binding.mapView.getMapAsync {
            map = it
            addAllPolyline()
        }

        binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSavetoDb()
        }

        activity?.addMenuProvider(this)
        subscribeToObservers()
    }

    private fun moveCameraToUser(){
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }


    //THIS FUNCTION IS FOR DRAWING THE LINE ON THE MAP
    private fun addAllPolyline(){
        for(polyline in pathPoints){

            val polylineOptions=PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)

            map?.addPolyline(polylineOptions)
        }
    }



    private fun subscribeToObservers(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints=it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeMillis=it
            val formattedTime=TrackingUtility.getFormattedStopWatchTime(currentTimeMillis,true)
            binding.tvTimer.text=formattedTime
        })
    }


    private fun toggleRun(){

        if(isTracking){
            menu?.getItem(0)?.isVisible=true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }


    //function for updating the ui
    private fun updateTracking(isTracking:Boolean){
        this.isTracking=isTracking

        if(!isTracking && currentTimeMillis>0L){
            binding.btnToggleRun.text="Start"
            binding.btnFinishRun.visibility=View.VISIBLE
        }else if(isTracking){
            binding.btnToggleRun.text="Stop"
            binding.btnFinishRun.visibility=View.GONE
            menu?.getItem(0)?.isVisible=true
        }
    }

    //THIS FUNCTION IS FOR DRAWING THE LINE ON MAP
    private fun addLatestPolyline(){
        if(pathPoints.isNotEmpty() && pathPoints.last().size>1){
            val preLastLatLng=pathPoints.last()[pathPoints.last().size-2]
            val lastLatLng = pathPoints.last().last()

            val polylineOptions=PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)

            map?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
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
        binding = FragmentTrackingBinding.inflate(layoutInflater)

        if (!NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }

        }else{
            hasNotificationPermissionGranted = true
        }
        return binding.root
    }


    private fun showCancelTrackingDialog(){

        CancelTrackingDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG)
    }

    private fun stopRun(){
        binding.tvTimer.text="00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        menu?.clear()
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.toolbar_tracking_menu,menu)
        this.menu=menu
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.cancelTracking->{
                showCancelTrackingDialog()
                true
            }
            else -> {
                false
            }
        }
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        if (currentTimeMillis>0L){
            menu.getItem(0)?.isVisible=true
        }
    }


    //we want the track in the center of the image zoomed
    private fun zoomToSeeWholeTrack(){
        val bounds=LatLngBounds.builder()

        for(polyline in pathPoints){

            for(pos in polyline){
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height*0.05f).toInt()
            )
        )
    }

    private fun endRunAndSavetoDb(){
        map?.snapshot { bmp->

            var distanceInMeters=0
            for(polyline in pathPoints){
                distanceInMeters+=TrackingUtility.calculatePolylineLength(polyline).toInt()
            }

            val avgSpeed = round((distanceInMeters/1000f) / (currentTimeMillis/1000f/60/60) * 10)/10f
            val dateTimeStamp=Calendar.getInstance().timeInMillis

            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()

            val run= com.example.runner.db.run(
                bmp,
                dateTimeStamp,
                avgSpeed,
                distanceInMeters,
                currentTimeMillis,
                caloriesBurned
            )

            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()

            stopRun()
        }
    }
}