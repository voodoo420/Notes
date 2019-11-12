package ru.geekbrains.gb_kotlin.ui.main

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class LogoutDialog : DialogFragment() {

    companion object{
        val TAG = LogoutDialog::class.java.name + "TAG"
        fun createInstance() = LogoutDialog()
    }

    interface LogoutListener{
        fun onLogout()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog = AlertDialog.Builder(context)
        .setTitle("Exit")
       // .setMessage("?")
        .setPositiveButton("Yes"){_,_ -> (activity as LogoutListener).onLogout()}
        .setNegativeButton("No"){_,_ -> dismiss()}
        .create()
}