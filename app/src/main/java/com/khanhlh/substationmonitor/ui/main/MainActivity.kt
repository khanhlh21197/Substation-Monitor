package com.khanhlh.substationmonitor.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseViewModel
import com.khanhlh.substationmonitor.extensions.logD
import okhttp3.internal.and
import java.net.NetworkInterface
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var instance: MainActivity
    }

    private var currentNavController: LiveData<NavController>? = null
    private lateinit var actionBarVM: BaseViewModel<Any>

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        } // Else, need to wait for onRestoreInstanceState
        logD("MAC: ${getMacAddr()}")
        val id = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        logD("ANDROID_ID: $id")
        setUpActionBar()
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
//        onBackPressed()
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

    fun getMacAddr(): String? {
        try {
            val all: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (nif.name != "wlan0") continue
                val macBytes: ByteArray = nif.hardwareAddress ?: return ""
                val res1 = StringBuilder()
                for (b in macBytes) {
                    res1.append(Integer.toHexString(b and 0xFF) + ":")
                }
                if (res1.isNotEmpty()) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
        } catch (ex: Exception) {
        }
        return "02:00:00:00:00:00"
    }
}
