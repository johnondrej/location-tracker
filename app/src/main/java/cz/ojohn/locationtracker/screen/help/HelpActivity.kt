package cz.ojohn.locationtracker.screen.help

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cz.ojohn.locationtracker.R

/**
 * Activity for displaying help info on devices with smaller display (like phones)
 */
class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.helpFragmentContainer, HelpFragment.newInstance())
                    .commit()
        }
    }
}
