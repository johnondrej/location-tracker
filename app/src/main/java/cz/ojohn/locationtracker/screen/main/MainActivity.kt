package cz.ojohn.locationtracker.screen.main

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.screen.about.AboutFragment
import cz.ojohn.locationtracker.screen.find.FindDeviceFragment
import cz.ojohn.locationtracker.screen.help.HelpActivity
import cz.ojohn.locationtracker.screen.help.HelpFragment
import cz.ojohn.locationtracker.screen.settings.SettingsActivity
import cz.ojohn.locationtracker.screen.sms.SmsFragment
import cz.ojohn.locationtracker.screen.tracking.TrackingFragment
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Main activity containing most of the app fragments
 */
class MainActivity : AppCompatActivity(), OnScreenSelectedListener {

    companion object {
        const val SCREEN_KEY = "screen_id"

        const val SCREEN_MENU = 0
        const val SCREEN_TRACKING = 1
        const val SCREEN_FIND = 2
        const val SCREEN_SMS = 3
    }

    private var currentScreenId = SCREEN_MENU

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, MainFragment.newInstance())
                    .commit()
        } else {
            currentScreenId = savedInstanceState.getInt(SCREEN_KEY, SCREEN_MENU)
        }

        if (isInTabletMode()) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.helpFragmentContainer, HelpFragment.newInstance(currentScreenId))
                    .commit()
        }

        bottomNavigation.setOnNavigationItemSelectedListener {
            onScreenSelected(it.itemId, false)
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
            R.id.action_licenses -> onLicensesSelected()
            R.id.action_about_app -> onAboutAppSelected()
            else -> return false
        }
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SCREEN_KEY, currentScreenId)
    }

    override fun onScreenSelected(screenId: Int, changeSelection: Boolean): Boolean {
        val id: Int
        val fragment: Fragment
        when (screenId) {
            R.id.action_screen_main -> {
                fragment = MainFragment.newInstance()
                id = SCREEN_MENU
            }
            R.id.action_screen_tracking -> {
                fragment = TrackingFragment.newInstance()
                id = SCREEN_TRACKING
            }
            R.id.action_screen_find -> {
                fragment = FindDeviceFragment.newInstance()
                id = SCREEN_FIND
            }
            R.id.action_screen_sms -> {
                fragment = SmsFragment.newInstance()
                id = SCREEN_SMS
            }
            else -> return false
        }

        when (id) {
            SCREEN_TRACKING, SCREEN_FIND -> {
                val apiAvailability = GoogleApiAvailability.getInstance()
                val playServicesConnection = apiAvailability.isGooglePlayServicesAvailable(this)

                if (playServicesConnection != ConnectionResult.SUCCESS) {
                    apiAvailability.getErrorDialog(this, playServicesConnection, 1).show()
                    return false
                }
            }
        }

        if (changeSelection) {
            bottomNavigation.selectedItemId = screenId
        }
        changeScreen(fragment, id)
        return true
    }

    private fun onSettingsSelected() {
        Intent(this, SettingsActivity::class.java).let {
            startActivity(it)
        }
    }

    private fun onHelpSelected() {
        Intent(this, HelpActivity::class.java).let {
            it.putExtra(SCREEN_KEY, currentScreenId)
            startActivity(it)
        }
    }

    private fun onLicensesSelected() {
        OssLicensesMenuActivity.setActivityTitle(getString(R.string.screen_licenses))
        startActivity(Intent(this, OssLicensesMenuActivity::class.java))
    }

    private fun onAboutAppSelected() {
        AboutFragment.newInstance().show(supportFragmentManager, "AboutFragment")
    }

    private fun changeScreen(screenFragment: Fragment, newScreenId: Int) {
        currentScreenId = newScreenId
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, screenFragment)
                .commit()

        if (isInTabletMode()) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.helpFragmentContainer, HelpFragment.newInstance(currentScreenId))
                    .commit()
        }
    }

    private fun isInTabletMode(): Boolean = helpFragmentContainer != null
}
