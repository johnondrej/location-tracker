package cz.ojohn.locationtracker.screen.help

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.screen.main.MainActivity

/**
 * Activity for displaying help info on devices with smaller display (like phones)
 */
class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.helpFragmentContainer, HelpFragment.newInstance(intent.extras.getInt(MainActivity.SCREEN_KEY)))
                    .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
