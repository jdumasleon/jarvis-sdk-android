package com.jarvis.presentation.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jarvis.presentation.viewmodel.ActivityShakingJarvisModeViewModel
import com.jarvis.tools.environments.EnvironmentsProvider
import com.jarvis.shakedetector.ShakeDetector
import javax.inject.Inject

abstract class ActivityShakingJarvisMode : ComponentActivity() {

    private lateinit var shakeDetector: ShakeDetector
    private lateinit var sensorManager: SensorManager

    private var accelerometer: Sensor? = null
    private var isShaking by mutableStateOf(false)

    private var environmentsProvider: EnvironmentsProvider? = null

    @Inject
    lateinit var activityShakingJarvisModeViewModel: ActivityShakingJarvisModeViewModel

    @Composable
    abstract fun SetContent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeDebugShakeDetector()
        initializeEnvironmentsList()
        setContent {
            SetContent()
            if (BuildConfig.DEBUG && isShaking) {
                GodModeComponent()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerSensorManagerListener()
    }

    override fun onPause() {
        super.onPause()
        unRegisterSensorManagerListener()
    }

    private fun initializeEnvironmentsList() {
        environmentsProvider?.let { provider ->
            val environmentsList = provider.provideEnvironmentsList()
            activityShakingJarvisModeViewModel.initializeEnvironmentsList(environmentsList)
        }
    }

    fun setEnvironmentsProvider(provider: com.jarvis.tools.environments.EnvironmentsProvider) {
        this.environmentsProvider = provider
    }

    private fun initializeDebugShakeDetector() {
        if (BuildConfig.DEBUG) {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            shakeDetector = ShakeDetector { isShaking = true }
        }
    }

    private fun registerSensorManagerListener() {
        if (BuildConfig.DEBUG) {
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun unRegisterSensorManagerListener() {
        if (BuildConfig.DEBUG) {
            sensorManager.unregisterListener(shakeDetector)
        }
    }
}