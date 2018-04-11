package cz.ojohn.locationtracker.screen.main

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.screen.about.AboutFragment
import cz.ojohn.locationtracker.screen.find.FindDeviceFragment
import cz.ojohn.locationtracker.screen.help.HelpActivity
import cz.ojohn.locationtracker.screen.help.HelpFragment
import cz.ojohn.locationtracker.screen.sms.SmsFragment
import cz.ojohn.locationtracker.screen.tracking.TrackingFragment
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Main activity containing most of the app fragments
 */
class MainActivity : AppCompatActivity(), MainFragment.OnScreenSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, MainFragment.newInstance())
                    .commit()

            if (isInTabletMode()) {
                supportFragmentManager.beginTransaction()
                        .add(R.id.helpFragmentContainer, HelpFragment.newInstance())
                        .commit()
            }
        }

        bottomNavigation.setOnNavigationItemSelectedListener {
            onScreenSelected(it.itemId, false)
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        if (isInTabletMode()) {
            menu.removeItem(R.id.action_help)
        }
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

    override fun onScreenSelected(screenId: Int, changeSelection: Boolean) {
        val fragment = when (screenId) {
            R.id.action_screen_main -> MainFragment.newInstance()
            R.id.action_screen_tracking -> TrackingFragment.newInstance()
            R.id.action_screen_find -> FindDeviceFragment.newInstance()
            R.id.action_screen_sms -> SmsFragment.newInstance()
            else -> return
        }
        if (changeSelection) {
            bottomNavigation.selectedItemId = screenId
        }
        changeScreen(fragment)
    }

    private fun onSettingsSelected() {

    }

    private fun onHelpSelected() {
        Intent(this, HelpActivity::class.java).let {
            startActivity(it)
        }
    }

    private fun onAboutAppSelected() {
        AboutFragment.newInstance().show(supportFragmentManager, "AboutFragment")
    }

    private fun changeScreen(screenFragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, screenFragment)
                .commit()
    }

    private fun isInTabletMode(): Boolean = helpFragmentContainer != null
}
