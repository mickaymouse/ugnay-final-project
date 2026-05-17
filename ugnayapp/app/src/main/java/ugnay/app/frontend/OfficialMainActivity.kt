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

        navView.setupWithNavController(navController)
    }
}