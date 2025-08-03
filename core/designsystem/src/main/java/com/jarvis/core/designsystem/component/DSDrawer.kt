package com.jarvis.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.theme.DSJarvisTheme

@Composable
fun DSDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    drawerState: DSDrawerState = rememberCardDrawerState(DSDrawerValue.Closed),
    gesturesEnabled: Boolean = true,
    drawerBackgroundColor: Color = DSJarvisTheme.colors.primary.primary100,
    drawerContentColor: Color = contentColorFor(drawerBackgroundColor),
    contentCornerSize: Dp = 0.dp,
    contentBackgroundColor: Color = DSJarvisTheme.colors.primary.primary100,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerContent = {
            Surface(
                modifier = Modifier.fillMaxHeight(),
                color = drawerBackgroundColor,
                contentColor = drawerContentColor
            ) {
                Column(content = drawerContent)
            }
        },
        modifier = modifier,
        drawerState = drawerState.materialDrawerState,
        gesturesEnabled = gesturesEnabled,
        content = content
    )
}

@Composable
fun rememberCardDrawerState(initialValue: DSDrawerValue): DSDrawerState {
    val materialDrawerValue = when (initialValue) {
        DSDrawerValue.Open -> DrawerValue.Open
        DSDrawerValue.Closed -> DrawerValue.Closed
    }
    val materialDrawerState = rememberDrawerState(materialDrawerValue)
    
    return remember {
        DSDrawerState(materialDrawerState)
    }
}

class DSDrawerState(
    internal val materialDrawerState: DrawerState
) {
    val currentValue: DSDrawerValue
        get() = when (materialDrawerState.currentValue) {
            DrawerValue.Open -> DSDrawerValue.Open
            DrawerValue.Closed -> DSDrawerValue.Closed
        }

    val isOpen: Boolean
        get() = currentValue == DSDrawerValue.Open

    val isClosed: Boolean
        get() = currentValue == DSDrawerValue.Closed

    suspend fun open() = materialDrawerState.open()
    suspend fun close() = materialDrawerState.close()
}

sealed class DSDrawerValue {
    data object Closed : DSDrawerValue()
    data object Open : DSDrawerValue()
}