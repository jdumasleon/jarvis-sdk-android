package com.jarvis.demo.presentation

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.color.MaterialColors
import com.google.android.material.navigation.NavigationView
import com.jarvis.demo.R

fun MainActivity.setupUI() {
    setContentView(R.layout.activity_main_classic)
    
    val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
    (this as AppCompatActivity).setSupportActionBar(toolbar)

    val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
    val navView = findViewById<NavigationView>(R.id.nav_view)
    
    val navHostFragment = (this as FragmentActivity).supportFragmentManager
        .findFragmentById(R.id.nav_host_fragment_classic) as NavHostFragment
    val navController = navHostFragment.navController
    
    // Set up ActionBar with NavController
    val appBarConfiguration = AppBarConfiguration(
        setOf(R.id.nav_home, R.id.nav_inspector, R.id.nav_preferences),
        drawerLayout
    )
    setupActionBarWithNavController(navController, appBarConfiguration)
    navView.setupWithNavController(navController)

    // Set up drawer toggle
    val toggle = ActionBarDrawerToggle(
        this, drawerLayout, toolbar,
        R.string.navigation_drawer_open,
        R.string.navigation_drawer_close
    )
    drawerLayout.addDrawerListener(toggle)
    toggle.syncState()
    
    // Handle drawer close button in header
    val headerView = navView.getHeaderView(0)
    val closeButton = headerView.findViewById<android.widget.ImageView>(R.id.drawer_close_button)
    closeButton?.setOnClickListener {
        drawerLayout.closeDrawer(GravityCompat.START)
    }
}