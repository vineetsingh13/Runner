package com.example.runner.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.runner.OTHER.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runner.OTHER.Constants.KEY_NAME
import com.example.runner.OTHER.Constants.KEY_WEIGHT
import com.example.runner.R
import com.example.runner.databinding.FragmentSetupBinding
import com.example.runner.ui.MainActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment: Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharedPref: SharedPreferences


    private lateinit var binding: FragmentSetupBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isFirstAppOpen = sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLE, true)
        if(!isFirstAppOpen){
            //what this does is if this not the first time the user enters the app
            //if the user presses back button on run fragment he will be taken to setup frag
            //to stop this we pop setup frag from our stack
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment,true)
                .build()

            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
        }

        binding.tvContinue.setOnClickListener {
            val success=writePersonalDataToSharedPref()

            if(success){
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            }else{
                Snackbar.make(requireView(),"Enter all details", Snackbar.LENGTH_SHORT).show()
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=FragmentSetupBinding.inflate(layoutInflater)

        (activity as MainActivity).binding.cardView.visibility=View.GONE

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity).binding.cardView.visibility=View.VISIBLE
    }

    private fun writePersonalDataToSharedPref(): Boolean {
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        if(name.isEmpty() || weight.isEmpty()) {
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()
        val toolbarText = "Let's go, $name!"
        requireActivity().findViewById<MaterialTextView>(R.id.tvToolbarTitle).text = toolbarText
        return true
    }
}