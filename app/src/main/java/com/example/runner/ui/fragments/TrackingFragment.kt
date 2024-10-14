package com.example.runner.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
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
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.runner.OTHER.Constants.ACTION_PAUSE_SERVICE
import com.example.runner.OTHER.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runner.OTHER.Constants.ACTION_STOP_SERVICE
import com.example.runner.OTHER.Constants.KEY_WEIGHT
import com.example.runner.OTHER.Constants.MAP_ZOOM
import com.example.runner.OTHER.Constants.POLYLINE_COLOR
import com.example.runner.OTHER.Constants.POLYLINE_WIDTH
import com.example.runner.OTHER.TrackingSingleton
import com.example.runner.OTHER.TrackingUtility
import com.example.runner.OTHER.polyline
import com.example.runner.R
import com.example.runner.databinding.FragmentTrackingBinding
import com.example.runner.services.TrackingService
import com.example.runner.ui.MainActivity
import com.example.runner.ui.viewModels.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.round

const val CANCEL_TRACKING_DIALOG="CANCELDIALOG"
@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking),MenuProvider {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: FragmentTrackingBinding
    private var isTracking=false
    private var currentLocationMarker: Marker? = null
    private var startLocation: LatLng? = null

    lateinit var fusedlocationprovider: FusedLocationProviderClient

    @Inject
    lateinit var sharedPref: SharedPreferences

    //private var pathPoints= mutableListOf<polyline>()

    private var map: GoogleMap? = null


    private var currentTimeMillis=0L

    private var menu: Menu?=null

    var weight=80f

    var hasNotificationPermissionGranted = false

    private var hasZoomedToUserOnce=false

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
        //isTracking = savedInstanceState?.getBoolean("isTracking") ?: false

        binding.btnToggleRun.setOnClickListener {
            //drawCompletePath()
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
            map!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(),R.raw.style_json))
            addAllPolyline()
            moveToUserBeforeStartingRun()
        }

        binding.btnFinishRun.setOnClickListener {
            TrackingSingleton.allPath.add(TrackingSingleton.pathPoints.value!!)
            zoomToSeeWholeTrack()
            endRunAndSavetoDb()
        }

        activity?.addMenuProvider(this)
        subscribeToObservers()
    }

    private fun moveCameraToUser(){
        if(TrackingSingleton.pathPoints.value!!.isNotEmpty() && TrackingSingleton.pathPoints.value!!.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    TrackingSingleton.pathPoints.value!!.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }


    //THIS FUNCTION IS FOR DRAWING THE LINE ON THE MAP
    private fun addAllPolyline(){
        if(isTracking && TrackingSingleton.pathPoints.value!!.size>=1){
            for(polyline in TrackingSingleton.pathPoints.value!!){

                val polylineOptions=PolylineOptions()
                    .color(POLYLINE_COLOR)
                    .width(POLYLINE_WIDTH)
                    .addAll(polyline)

                map?.addPolyline(polylineOptions)
            }
        }
    }



    private fun subscribeToObservers(){


        lifecycleScope.launch {
            TrackingService.isTracking.collect{ isTracking ->
                updateTracking(isTracking)
            }
        }

        TrackingSingleton.pathPoints.observe(viewLifecycleOwner, Observer {
            //pathPoints=it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingSingleton.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeMillis=it
            val formattedTime=TrackingUtility.getFormattedStopWatchTime(currentTimeMillis,true)
            binding.tvTimer.text=formattedTime

        })
    }


    private fun toggleRun(){

        if(isTracking){
            menu?.getItem(0)?.isVisible=true
            sendCommandToService(ACTION_PAUSE_SERVICE)
            TrackingSingleton.allPath.add(TrackingSingleton.pathPoints.value!!)

            Log.d("ALLPATH", TrackingSingleton.allPath.toString())
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
            //drawCompletePath()
        }else if(isTracking){
            binding.btnToggleRun.text="Stop"
            binding.btnFinishRun.visibility=View.GONE
            menu?.getItem(0)?.isVisible=true
        }
    }

    //THIS FUNCTION IS FOR DRAWING THE LINE ON MAP
    private fun addLatestPolyline(){
        if(TrackingSingleton.pathPoints.value!!.isNotEmpty() && TrackingSingleton.pathPoints.value!!.last().size>1){
            val preLastLatLng=TrackingSingleton.pathPoints.value!!.last()[TrackingSingleton.pathPoints.value!!.last().size-2]
            val lastLatLng = TrackingSingleton.pathPoints.value!!.last().last()


            if (startLocation == null) {
                startLocation = TrackingSingleton.pathPoints.value!!.last()[0]
                Log.d("start locaiton",startLocation.toString())
                //map?.addMarker(MarkerOptions().position(startLocation!!).title("Start").icon(bitmapDescriptorFromVector(requireContext(), R.drawable.flag)))
                addStartMarker(startLocation!!)
            }
            addOrUpdateMarker(lastLatLng)

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


    private fun drawCompletePath(path: MutableList<polyline>) {
        Log.d("RESUME", "RESUME CALLED")

        val polylineOptions = PolylineOptions()
            .color(POLYLINE_COLOR)
            .width(POLYLINE_WIDTH)

        for (point in path) {
            for(coordinates in point){
                polylineOptions.add(coordinates)
            }
        }

        map!!.addPolyline(polylineOptions)
    }

    override fun onStart() {
        super.onStart()
        if(TrackingSingleton.isRunOn.value==true){
            drawCompletePath(TrackingSingleton.pathPoints.value!!)
        }
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        //drawCompletePath()
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
        //outState.putBoolean("isTracking", isTracking)
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

        (activity as MainActivity).binding.cardView.visibility=View.GONE

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity).binding.cardView.visibility=View.VISIBLE
        hasZoomedToUserOnce=false
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
        TrackingSingleton.allPath.clear()
        TrackingSingleton.pathPoints.value?.clear()
        TrackingSingleton.isRunOn.postValue(false)
        TrackingSingleton.timeRunInMillis.postValue(0L)
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

        for (polylines in TrackingSingleton.allPath) {
            // Iterate through each polyline in the current polylines
            for (polyline in polylines) {
                // Iterate through each position in the current polyline
                for (pos in polyline) {
                    bounds.include(pos)
                }
            }
        }


        //val padding = (minOf(binding.mapView.width, binding.mapView.height) * 0.1f).toInt()
        val width = resources.displayMetrics.widthPixels
        val height=resources.displayMetrics.heightPixels
        val padding = (height * 0.25).toInt()

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),padding
            )
        )
    }

    private fun endRunAndSavetoDb(){

        if(startLocation!=null){
            map?.addMarker(MarkerOptions().position(startLocation!!).title("Start").icon(bitmapDescriptorFromVector(requireContext(), R.drawable.flag)))
        }

        Log.d("start location",startLocation.toString())
        map?.snapshot { bmp->
            var distanceInMeters=0

            for (polylines in TrackingSingleton.allPath) {
                // Iterate through each polyline in the current polylines
                for (polyline in polylines) {
                    // Iterate through each position in the current polyline
                    distanceInMeters+=TrackingUtility.calculatePolylineLength(polyline).toInt()
                }
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

    @SuppressLint("MissingPermission")
    private fun moveToUserBeforeStartingRun(){

        if (hasZoomedToUserOnce) return

        fusedlocationprovider = LocationServices.getFusedLocationProviderClient(requireContext())

        fusedlocationprovider.lastLocation.addOnSuccessListener { location->

            location.let {

                val latlng=LatLng(location.latitude,location.longitude)
                addOrUpdateMarker(latlng)
                map?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        latlng,
                        MAP_ZOOM
                    )
                )

                hasZoomedToUserOnce = true
            }
        }

    }

    private fun addOrUpdateMarker(position: LatLng) {
        if (currentLocationMarker == null) {
            // Add a new marker if it doesn’t exist yet
            val customIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.location)
            currentLocationMarker = map!!.addMarker(
                MarkerOptions()
                    .position(position)
                    .title("Current Location")
                    .icon(customIcon)
            )
        } else {
            // Update the marker's position
            currentLocationMarker?.position = position
        }
    }

    private fun bitmapDescriptorFromVector(context: Context, drawableId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, drawableId)
        vectorDrawable?.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val width = 110
        val height = 110
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable?.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable?.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun addStartMarker(position: LatLng) {
        val startIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.flag) // Customize as needed
        map?.addMarker(
            MarkerOptions()
                .position(position)
                .title("Start Location")
                .icon(startIcon)
        )
    }
}