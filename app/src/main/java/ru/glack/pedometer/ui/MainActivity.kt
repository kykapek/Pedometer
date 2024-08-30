package ru.glack.pedometer.ui

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACTIVITY_RECOGNITION
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.navigation.NavigationView
import ru.glack.pedometer.R
import ru.glack.pedometer.service.LocationStepService
import android.Manifest.permission.*
import com.google.firebase.Firebase
import com.google.firebase.database.database

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupNavigation()
        checkPermissions()
    }

    private fun checkPermissions() {
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startLocationStepService()
            } else {
                this.finish()
            }
        }
        when {
            ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED -> {
                startLocationStepService()
            }
            else -> {
                requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                requestPermissionLauncher.launch(ACTIVITY_RECOGNITION)
            }
        }
    }

    private fun setupNavigation() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drawerLayout = findViewById(R.id.drawerLayout)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout,
            R.string.m_nav_open,
            R.string.m_nav_close
        )
        val navigationView: NavigationView = findViewById(R.id.navView)
        navController = (supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment).navController
        setupActionBarWithNavController(this, navController, drawerLayout)
        setupWithNavController(navigationView, navController)
        navigationView.setNavigationItemSelectedListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("nav_drawer", item.title.toString())
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        item.isChecked = true
        drawerLayout.closeDrawers()
        when (item.itemId) {
            R.id.mainFragment -> navController.navigate(R.id.mainFragment)
            R.id.databaseFragment -> navController.navigate(R.id.databaseFragment)
            R.id.serviceFragment -> navController.navigate(R.id.serviceFragment)
        }
        return true
    }

    private fun startLocationStepService() {
        val serviceIntent = Intent(this, LocationStepService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}