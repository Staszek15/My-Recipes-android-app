package com.staszek15.myrecipes

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LoadingDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val progressBar = ProgressBar(requireContext()).apply {
            isIndeterminate = true
        }

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_loading, null)

        return MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setCancelable(false)
            .create()

    }
}
