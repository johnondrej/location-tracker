package cz.ojohn.locationtracker.screen.settings

import android.content.SharedPreferences
import android.os.Bundle
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat
import cz.ojohn.locationtracker.Constants
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.data.UserPreferences

/**
 * Fragment used for app settings
 */
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = Constants.SP_NAME
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            UserPreferences.KEY_SMS_PASSWORD -> onSmsPasswordChanged(sharedPreferences)
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        initSummaries()
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun initSummaries() {
        val sharedPreferences = preferenceScreen.sharedPreferences
        onSmsPasswordChanged(sharedPreferences)
    }

    private fun onSmsPasswordChanged(sharedPreferences: SharedPreferences) {
        val preference = findPreference(UserPreferences.KEY_SMS_PASSWORD)
        val password = sharedPreferences.getString(UserPreferences.KEY_SMS_PASSWORD, UserPreferences.SMS_DEFAULT_PASSWORD).replace(Regex("\\s+"), "")
        val summary = requireContext().getString(R.string.settings_sms_password_format, password)
        preference.summary = summary
    }
}
