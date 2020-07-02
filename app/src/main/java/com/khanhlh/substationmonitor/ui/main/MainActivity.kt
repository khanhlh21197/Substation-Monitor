package com.khanhlh.substationmonitor.ui.main

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.extensions.logD

class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var instance: MainActivity
    }

    private var currentNavController: LiveData<NavController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        } // Else, need to wait for onRestoreInstanceState
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    /**
     * Called on first creation and when restoring state.
     */
    private fun setupBottomNavigationBar() {
        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_container) as NavHostFragment? ?: return

        // Set up Action Bar
        val controller = host.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomBar)
        bottomNavigationView.setupWithNavController(controller)

        bottomNavigationView.menu.findItem(R.id.searchFragment).isVisible = false
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }

    override fun onBackPressed() {
        logD(supportFragmentManager.backStackEntryCount.toString())
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController(R.id.nav_host_container))
                || super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        logD("Main::onDestroy")
    }

//    private fun setupBottomNavMenu(navController: NavController) {
//        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomBar)
//        bottomNav?.setupWithNavController(navController)
//    }

//    private fun setupNavigationMenu(navController: NavController) {
//        val sideNavView = findViewById<NavigationView>(R.id.nav_view)
//        sideNavView?.setupWithNavController(navController)
//    }

//    private fun setupActionBar(navController: NavController,
//                               appBarConfig : AppBarConfiguration
//    ) {
//        setupActionBarWithNavController(navController, appBarConfig)
//    }
}
