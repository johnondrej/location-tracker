package cz.ojohn.locationtracker.screen.settings

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cz.ojohn.locationtracker.R

/**
 * Activity containing fragment with settings
 */
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, SettingsFragment())
                    .commit()
        }
    }
}
