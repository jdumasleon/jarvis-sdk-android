package com.jarvis.demo.presentation

import android.app.Application
// import com.jarvis.presentation.ui.ActivityJarvisMode
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class JarvisDemoApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // TODO: Initialize Jarvis SDK with shake detection
        // ActivityJarvisMode.init(this) - check if needed
    }
}