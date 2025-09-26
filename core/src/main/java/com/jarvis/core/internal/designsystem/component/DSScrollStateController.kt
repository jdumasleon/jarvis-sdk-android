@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.designsystem.component

import androidx.annotation.RestrictTo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

/**
 * A utility class to manage scroll state for TopAppBar and NavigationBar color changes
 */
data class ScrollState(
    val isScrolledUp: Boolean = false,
    val isScrolledDown: Boolean = false
)

/**
 * Creates a scroll state controller that detects scroll direction and manages
 * background color changes for TopAppBar (scroll up) and NavigationBar (scroll down)
 */
@Composable
fun rememberScrollStateController(
    scrollUpThreshold: Float = 50f,
    scrollDownThreshold: Float = 50f
): Pair<ScrollState, NestedScrollConnection> {
    var scrollState by remember { mutableStateOf(ScrollState()) }
    
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                
                when {
                    // Scrolling up - show TopAppBar background change
                    delta < -scrollUpThreshold && !scrollState.isScrolledUp -> {
                        scrollState = scrollState.copy(isScrolledUp = true)
                    }
                    // Scrolling down - show NavigationBar background change  
                    delta > scrollDownThreshold && !scrollState.isScrolledDown -> {
                        scrollState = scrollState.copy(isScrolledDown = true)
                    }
                    // Reset when scrolling back
                    delta > scrollUpThreshold && scrollState.isScrolledUp -> {
                        scrollState = scrollState.copy(isScrolledUp = false)
                    }
                    delta < -scrollDownThreshold && scrollState.isScrolledDown -> {
                        scrollState = scrollState.copy(isScrolledDown = false)
                    }
                }
                
                return Offset.Zero
            }
        }
    }
    
    return scrollState to nestedScrollConnection
}

/**
 * A simpler version that only tracks one scroll direction at a time
 */
@Composable
fun rememberSimpleScrollStateController(
    threshold: Float = 50f
): Pair<ScrollState, NestedScrollConnection> {
    var scrollState by remember { mutableStateOf(ScrollState()) }
    
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                
                when {
                    // Scrolling up
                    delta < -threshold -> {
                        scrollState = ScrollState(isScrolledUp = true, isScrolledDown = false)
                    }
                    // Scrolling down
                    delta > threshold -> {
                        scrollState = ScrollState(isScrolledUp = false, isScrolledDown = true)
                    }
                }
                
                return Offset.Zero
            }
        }
    }
    
    return scrollState to nestedScrollConnection
}