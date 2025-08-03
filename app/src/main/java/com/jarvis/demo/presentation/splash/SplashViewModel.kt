package com.jarvis.demo.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.core.common.di.CoroutineDispatcherModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    @CoroutineDispatcherModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val splashShowFlow = MutableStateFlow(true)
    val isSplashShow = splashShowFlow.asStateFlow()

    init {
        splashScreen()
    }

    private fun splashScreen() {
        viewModelScope.launch(ioDispatcher) {
            delay(3000)
            splashShowFlow.value = false
        }
    }
}