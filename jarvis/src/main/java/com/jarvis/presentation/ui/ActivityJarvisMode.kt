package com.jarvis.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.jarvis.presentation.viewmodel.ActivityJarvisModeViewModel
import javax.inject.Inject

abstract class ActivityJarvisMode : ComponentActivity() {
    @Inject
    lateinit var activityJarvisModeViewModel: ActivityJarvisModeViewModel

    @Composable
    abstract fun SetContent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SetContent()
            JarvisComponent()
        }
    }
}