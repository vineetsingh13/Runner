package com.example.runner.ui.fragments

import android.content.SharedPreferences
import android.icu.text.TimeZoneNames.NameType
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.runner.OTHER.Constants.KEY_NAME
import com.example.runner.OTHER.Constants.KEY_WEIGHT
import com.example.runner.R
import com.example.runner.databinding.FragmentSettingsBinding
import com.example.runner.databinding.FragmentSetupBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment: Fragment(R.layout.fragment_settings) {

    private lateinit var binding: FragmentSettingsBinding

    @Inject
    lateinit var sharefPref: SharedPreferences

    private fun applyChangesToSharedPref(): Boolean{
        val nameText=binding.etName.text.toString()
        val weight=binding.etWeight.text.toString()

        if(nameText.isEmpty() || weight.isEmpty()){
            return false
        }
        sharefPref.edit()
            .putString(KEY_NAME,nameText)
            .putFloat(KEY_WEIGHT,weight.toFloat())
            .apply()

        return true
    }

    private fun loadFieldFromSharedPref(){
        val name=sharefPref.getString(KEY_NAME,"")
        val weight=sharefPref.getFloat(KEY_WEIGHT,80f)
        binding.etName.setText(name)
        binding.etWeight.setText(weight.toString())

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadFieldFromSharedPref()
        binding.btnApplyChanges.setOnClickListener {
            val success=applyChangesToSharedPref()

            if(success){
                Snackbar.make(requireView(),"Changes Saved", Snackbar.LENGTH_SHORT).show()
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
        binding= FragmentSettingsBinding.inflate(layoutInflater)


        return binding.root
    }
}