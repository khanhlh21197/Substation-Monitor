package com.khanhlh.substationmonitor.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseActivity
import com.khanhlh.substationmonitor.base.BaseViewModel
import com.khanhlh.substationmonitor.databinding.ActivityMainBinding
import com.khanhlh.substationmonitor.di.ViewModelFactory
import com.khanhlh.substationmonitor.extensions.logD
import com.khanhlh.substationmonitor.model.ThietBiResponse
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    lateinit var response: ThietBiResponse
    lateinit var userJson: String
    private lateinit var viewModel: MainViewModel

    private object Holder {
        val INSTANCE = MainActivity()
    }

    companion object {
        @JvmStatic
        fun getInstance(): MainActivity {
            return Holder.INSTANCE
        }
    }

    private var currentNavController: LiveData<NavController>? = null
    private lateinit var actionBarVM: BaseViewModel<Any>

    override fun initVariables() {
        baseViewModel = MainViewModel(MyApp())
        baseViewModel.attachView(this)
        baseViewModel =
            ViewModelProvider(this, ViewModelFactory(this)).get(MainViewModel::class.java)

        viewModel = baseViewModel
        bindView(R.layout.activity_main)
        binding.vm = viewModel
        getBundleData()
    }

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            response = bundle.getSerializable("thietbi") as ThietBiResponse
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
        actionbar.hide()

        app_bar.findViewById<Button>(R.id.back).setOnClickListener { onBackPressed() }
        app_bar.findViewById<TextView>(R.id.label).text = "MainActivity"

        viewModel.title.observe(this, Observer {
            app_bar.findViewById<TextView>(R.id.label).text = it
        })

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
        bundle.putSerializable("thietbi", response)
        bundle.putString("user", userJson)
        val navController = findNavController(R.id.nav_host_container)
        navController.setGraph(navController.graph, bundle)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomBar)
        bottomNavigationView.setupWithNavController(controller)

//        bottomNavigationView.menu.findItem(R.id.searchFragment).isVisible = true
//        bottomNavigationView.menu.findItem(R.id.profileFragment).isVisible = false
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return false
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
