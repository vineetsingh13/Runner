package com.example.runner.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.runner.OTHER.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runner.R
import com.example.runner.databinding.ActivityMainBinding
import com.example.runner.db.RunDAO
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    public lateinit var binding: ActivityMainBinding

    private val PERMISSION_REQ_CODE=100

    var permissionExplainationDialogShown=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setSupportActionBar(binding.toolbar)

        navigateToTrackingFragment(intent)
        var navHostFragment= supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment

        binding.bottomNavigationView.setupWithNavController(navHostFragment.navController)
        binding.bottomNavigationView.setOnItemReselectedListener { /*no op*/ }

        navHostFragment.navController
            .addOnDestinationChangedListener{_,destination,_->
                when(destination.id){
                    R.id.settingFragment,R.id.runFragment,R.id.statisticsFragment ->
                        binding.bottomNavigationView.visibility=View.VISIBLE
                    else -> binding.bottomNavigationView.visibility=View.GONE

                }
            }
    }

    override fun onStart() {
        super.onStart()
        requestPermission()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragment(intent)
    }
    private fun navigateToTrackingFragment(intent: Intent?){
        if(intent?.action == ACTION_SHOW_TRACKING_FRAGMENT){
            val navHostFragment= supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
            navHostFragment.navController.navigate(R.id.action_global_tracking_fragment)
        }
    }


    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show()

        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQ_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQ_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show()
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) && permissionExplainationDialogShown
            ) {
                showSettingsRedirectDialog()
            } else {
                if(!permissionExplainationDialogShown){
                    showPermissionExplanationDialog()
                    permissionExplainationDialogShown=true
                }
            }
        }
    }

    private fun showPermissionExplanationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Location Permission is needed for the app.")
        builder.setCancelable(false)
        builder.setPositiveButton("OK") { dialog, which ->
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQ_CODE
            )
        }

        val alertDialog = builder.create()

        alertDialog.show()
    }

    private fun showSettingsRedirectDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.permissionExplain)
        builder.setCancelable(false)
        builder.setPositiveButton("OK") { dialog, which ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }



}


