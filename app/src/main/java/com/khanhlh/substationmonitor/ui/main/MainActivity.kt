package com.khanhlh.substationmonitor.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.extensions.logD
import com.khanhlh.substationmonitor.extensions.setupWithNavController

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
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
        setupBottomNavigationBar()
    }

    /**
     * Called on first creation and when restoring state.
     */
    private fun setupBottomNavigationBar() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomBar)

        val navGraphIds = listOf(
            R.navigation.bm_home,
            R.navigation.bm_search,
            R.navigation.bm_post,
            R.navigation.bm_profile
        )

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_container,
            intent = intent
        )

        bottomNavigationView.menu.findItem(R.id.bm_search).isVisible = false

        // Whenever the selected controller changes, setup the action bar.
        controller.observe(this, Observer { navController ->
            //     setupActionBarWithNavController(navController)
        })
        currentNavController = controller
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }

    override fun onBackPressed() {
        logD(supportFragmentManager.backStackEntryCount.toString())
        super.onBackPressed()
    }
}
