package com.example.runner.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.runner.R
import com.example.runner.databinding.ActivityMainBinding
import com.example.runner.db.RunDAO
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment

        binding.bottomNavigationView.setupWithNavController(navHostFragment.navController)

        navHostFragment.navController
            .addOnDestinationChangedListener{_,destination,_->
                when(destination.id){
                    R.id.settingFragment,R.id.runFragment,R.id.statisticsFragment ->
                        binding.bottomNavigationView.visibility=View.VISIBLE
                    else -> binding.bottomNavigationView.visibility=View.GONE

                }
            }
    }
}