package com.example.panicbutton

import android.app.Application
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import kotlinx.coroutines.MainScope

class PanicApp : Application() {
    companion object {
        lateinit var instance: PanicApp
            private set
        
        val panicManager: PanicManager by lazy {
            PanicManager(MainScope(), AndroidPlatformActions(instance))
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        KmmSettings.context = this
        try {
            Firebase.initialize(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
