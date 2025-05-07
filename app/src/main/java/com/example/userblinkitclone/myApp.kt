package com.example.userblinkitclone

import android.app.Application

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        utils.appContext = applicationContext
    }
}