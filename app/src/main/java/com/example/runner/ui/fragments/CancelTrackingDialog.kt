package com.example.runner.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.runner.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelTrackingDialog : DialogFragment() {

    private var yesListener:(() -> Unit)?=null

    fun setYesListener(listener: () -> Unit){
        yesListener=listener
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the run")
            .setMessage("Are you sure you want to cancel the run?")
            .setIcon(R.drawable.ic_delete_black)
            .setPositiveButton("yes") { _, _ ->
                yesListener?.let { yes->
                    yes()
                }
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }

            .create()

    }
}