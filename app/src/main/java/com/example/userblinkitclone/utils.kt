package com.example.userblinkitclone

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.example.userblinkitclone.databinding.ProgressDialogBinding
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.DateTimeFormatter


object utils {

    lateinit var appContext: Context

    private var dialog : AlertDialog? = null

    fun showDialog(context : Context,message : String){
        val progress = ProgressDialogBinding.inflate(LayoutInflater.from(context))
        progress.tvMessage.text = message
        dialog = AlertDialog.Builder(context)
            .setView(progress.root)
            .setCancelable(false)
            .create()
        dialog!!.show()

    }

    fun hideDialog(){

        dialog?.dismiss()

    }

    fun showToast(context : Context,message : String){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }

    private var FirebaseAuthInstance : FirebaseAuth? = null
    fun getAuthInstance() : FirebaseAuth{
        if(FirebaseAuthInstance == null){
            FirebaseAuthInstance = FirebaseAuth.getInstance()
        }
        return FirebaseAuthInstance!!

    }

    fun getCurrentUserId() : String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun getRandomId() : String{
        return (1..25).map{(('A'..'Z') + ('a'..'z') + ('0'..'9')).random() }.joinToString("")
    }

    fun getCurrentDate() : String{
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return currentDate.format(formatter)
    }
}