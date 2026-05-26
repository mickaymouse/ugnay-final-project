package ugnay.app.frontend.brgy_officials

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import ugnay.app.R

class OfficialMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_official_main)

        val navView =
            findViewById<BottomNavigationView>(R.id.bottom_nav_official)

        val navHostFragment =
            supportFragmentManager.findFragmentById(
                R.id.official_nav_host_fragment
            ) as NavHostFragment

        val navController = navHostFragment.navController

        // Setup navigation with bottom nav
        navView.setupWithNavController(navController)

        // Handle back press to properly manage back stack
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_official_home -> {
                    // Clear back stack and navigate to home
                    navController.popBackStack(R.id.nav_official_home, false)
                    if (navController.currentDestination?.id != R.id.nav_official_home) {
                        navController.navigate(R.id.nav_official_home)
                    }
                    true
                }
                R.id.nav_official_requests -> {
                    navController.popBackStack(R.id.nav_official_requests, false)
                    if (navController.currentDestination?.id != R.id.nav_official_requests) {
                        navController.navigate(R.id.nav_official_requests)
                    }
                    true
                }
                R.id.nav_official_news -> {
                    navController.popBackStack(R.id.nav_official_news, false)
                    if (navController.currentDestination?.id != R.id.nav_official_news) {
                        navController.navigate(R.id.nav_official_news)
                    }
                    true
                }
                R.id.nav_official_profile -> {
                    navController.popBackStack(R.id.nav_official_profile, false)
                    if (navController.currentDestination?.id != R.id.nav_official_profile) {
                        navController.navigate(R.id.nav_official_profile)
                    }
                    true
                }
                else -> false
            }
        }
    }
}