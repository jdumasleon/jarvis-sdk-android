package com.jarvis.api.ui

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.jarvis.api.ui.navigation.JarvisNavigationHost
import com.jarvis.core.designsystem.theme.DSJarvisTheme

/**
 * Jarvis SDK overlay that displays as a full-screen dialog
 * This provides completely independent navigation without affecting the host app
 */
class JarvisOverlay(private val context: Context) {
    private var composeView: ComposeView? = null
    private var isShowing = false
    
    fun show() {
        if (isShowing) return
        
        val activity = context as? Activity ?: return
        
        // Create overlay dialog content
        val content = @Composable {
            JarvisOverlayContent(
                onDismiss = { dismiss() }
            )
        }
        
        // Show as a full-screen dialog
        composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(activity as androidx.lifecycle.LifecycleOwner)
            setViewTreeViewModelStoreOwner(activity as androidx.lifecycle.ViewModelStoreOwner)
            setViewTreeSavedStateRegistryOwner(activity as androidx.savedstate.SavedStateRegistryOwner)
            
            setContent {
                DSJarvisTheme {
                    Dialog(
                        onDismissRequest = { dismiss() },
                        properties = DialogProperties(
                            usePlatformDefaultWidth = false,
                            decorFitsSystemWindows = false
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.8f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                content()
                            }
                        }
                    }
                }
            }
        }
        
        isShowing = true
    }
    
    fun dismiss() {
        if (!isShowing) return
        
        composeView = null
        isShowing = false
    }
    
    fun isShowing(): Boolean = isShowing
}

@Composable
private fun JarvisOverlayContent(
    onDismiss: () -> Unit
) {
    JarvisNavigationHost(
        onDismiss = onDismiss,
        modifier = Modifier.fillMaxSize()
    )
}