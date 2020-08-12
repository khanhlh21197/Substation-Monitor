package com.khanhlh.substationmonitor.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseViewModel
import com.khanhlh.substationmonitor.extensions.logD
import com.khanhlh.substationmonitor.model.NhaResponse

class MainActivity : AppCompatActivity() {
    lateinit var response: NhaResponse
    lateinit var userJson: String

    companion object {
        lateinit var instance: MainActivity
    }

    private var currentNavController: LiveData<NavController>? = null
    private lateinit var actionBarVM: BaseViewModel<Any>

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getBundleData()
        instance = this
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        } // Else, need to wait for onRestoreInstanceState
        val id = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        logD("ANDROID_ID: $id")
        setUpActionBar()
    }

    private fun getBundleData() {
        val bundle = intent.extras
        if (bundle != null) {
            response = bundle.getSerializable("nha") as NhaResponse
            userJson = bundle.getString("user") as String
        }
    }

    private fun setUpActionBar() {
        //actionbar
        val actionbar = supportActionBar
        //set actionbar title
//        actionbar!!.title = "New Activity"
        //set back button
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)

//        actionBarVM = BaseViewModel()
//        actionBarVM.title.observe(
//            this,
//            androidx.lifecycle.Observer { actionbar.title = it })
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    /**
     * Called on first creation and when restoring state.
     */
    @SuppressLint("ResourceType")
    private fun setupBottomNavigationBar() {
        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_container) as NavHostFragment? ?: return

        // Set up Action Bar
        val controller = host.navController

        val bundle = Bundle()
        bundle.putSerializable("nha", response)
        bundle.putString("user", userJson)
        val navController = findNavController(R.id.nav_host_container)
        navController.setGraph(navController.graph, bundle)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomBar)
        bottomNavigationView.setupWithNavController(controller)

        bottomNavigationView.menu.findItem(R.id.searchFragment).isVisible = true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
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
