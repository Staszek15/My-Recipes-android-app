package com.staszek15.serveit

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater

fun loadingDialog(context: Context): AlertDialog? {
    // Create and show the loading dialog
    val loadingView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
    val loadingDialog = AlertDialog.Builder(context)
        .setView(loadingView)
        .setCancelable(false)
        .create()
    loadingDialog.show()
    val halfScreenHeight = Resources.getSystem().displayMetrics.heightPixels / 2
    val dialogWidth = (Resources.getSystem().displayMetrics.widthPixels * 0.85).toInt()
    loadingDialog.window?.setLayout(dialogWidth, halfScreenHeight)
    loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    return loadingDialog
}