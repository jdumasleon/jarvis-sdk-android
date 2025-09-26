package com.jarvis.demo.domain.usecase.home

import android.util.Log
import com.jarvis.api.JarvisSDK
import javax.inject.Inject

class ManageJarvisModeUseCase @Inject constructor(
    private val jarvisSDK: JarvisSDK
) {

    fun toggleJarvisMode(): Boolean {
        val newActiveState = jarvisSDK.toggle()
        Log.d("ManageJarvisModeUseCase", "Jarvis mode toggled: $newActiveState")
        Log.d("ManageJarvisModeUseCase", "SDK isActive(): ${jarvisSDK.isActive()}")
        return newActiveState
    }

    fun isJarvisActive(): Boolean {
        return jarvisSDK.isActive()
    }

    fun activateJarvis() {
        jarvisSDK.activate()
        Log.d("ManageJarvisModeUseCase", "Jarvis activated")
    }

    fun deactivateJarvis() {
        jarvisSDK.deactivate()
        Log.d("ManageJarvisModeUseCase", "Jarvis deactivated")
    }
}