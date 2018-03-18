package cz.ojohn.locationtracker.screen.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.screen.find.FindDeviceFragment
import cz.ojohn.locationtracker.screen.history.LocationHistoryFragment
import cz.ojohn.locationtracker.screen.sms.SmsFragment
import cz.ojohn.locationtracker.screen.tracking.TrackingFragment
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Main activity containing most of the app fragments
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, MainFragment.newInstance())
                    .commit()
        }

        bottomNavigation.setOnNavigationItemSelectedListener {
            onScreenSelected(it.itemId)
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> onSettingsSelected()
            R.id.action_help -> onHelpSelected()
            R.id.action_about_app -> onAboutAppSelected()
            else -> return false
        }
        return true
    }

    private fun onSettingsSelected() {

    }

    private fun onHelpSelected() {

    }

    private fun onAboutAppSelected() {

    }

    private fun onScreenSelected(screenId: Int) {
        val fragment = when (screenId) {
            R.id.action_screen_main -> MainFragment.newInstance()
            R.id.action_screen_tracking -> TrackingFragment.newInstance()
            R.id.action_screen_find -> FindDeviceFragment.newInstance()
            R.id.action_screen_sms -> SmsFragment.newInstance()
            R.id.action_screen_history -> LocationHistoryFragment.newInstance()
            else -> return
        }
        changeScreen(fragment)
    }

    private fun changeScreen(screenFragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, screenFragment)
                .commit()
    }
}
