package com.jarvis.core.designsystem.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Adb
import androidx.compose.material.icons.rounded.ArrowBack

object DSIcons {

    val arrowUp = Icons.Default.KeyboardArrowUp
    val arrowDown = Icons.Default.KeyboardArrowDown
    val dateRange = Icons.Default.DateRange
    val check = Icons.Default.Check
    val place = Icons.Default.Place
    val person = Icons.Default.Person
    val moreVert = Icons.Default.MoreVert
    val arrowForward = Icons.AutoMirrored.Filled.ArrowForward
    val search = Icons.Default.Search
    val adb = Icons.Rounded.Adb
    
    // Navigation icons
    val home = Icons.Default.Home
    val homeFilled = Icons.Default.Home
    val settings = Icons.Default.Settings
    val settingsFilled = Icons.Default.Settings
    val networkWifi = Icons.Default.Wifi
    val add = Icons.Rounded.Add
    val menu = Icons.Default.Menu
    val refresh = Icons.Default.Refresh
    val networkCheck =  Icons.Default.NetworkCheck

    object Filled {
        val home = Icons.Filled.Home
        val settings = Icons.Filled.Settings
        val networkCheck = Icons.Filled.NetworkCheck
        val moreVert = Icons.Filled.MoreVert
    }

    object Rounded {
        val add = Icons.Rounded.Add
        val check = Icons.Rounded.Check
        val close = Icons.Rounded.Close
        val search = Icons.Rounded.Search
        val settings = Icons.Rounded.Settings
        val checkCircle = Icons.Rounded.CheckCircle
        val warning = Icons.Rounded.Warning
        val arrowBack = Icons.AutoMirrored.Rounded.ArrowBack
    }

    object Outlined {
        val info = Icons.Outlined.Info
        val home = Icons.Outlined.Home
        val networkCheck = Icons.Outlined.NetworkCheck
        val settings = Icons.Outlined.Settings
        val moreVert = Icons.Outlined.MoreVert
    }
}
