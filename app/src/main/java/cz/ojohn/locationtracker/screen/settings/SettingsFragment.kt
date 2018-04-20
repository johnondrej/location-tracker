package cz.ojohn.locationtracker.screen.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat
import cz.ojohn.locationtracker.App
import cz.ojohn.locationtracker.Constants
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.data.UserPreferences
import cz.ojohn.locationtracker.location.LocationTracker
import javax.inject.Inject

/**
 * Fragment used for app settings
 */
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var locationTracker: LocationTracker

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        App.instance.appComponent.inject(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = Constants.SP_NAME
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            UserPreferences.KEY_SMS_PASSWORD -> onSmsPasswordChanged(sharedPreferences)
            UserPreferences.KEY_GPS_ENABLED -> onGpsStatusChanged(sharedPreferences)
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

    private fun onGpsStatusChanged(sharedPreferences: SharedPreferences) {
        val useGps = sharedPreferences.getBoolean(UserPreferences.KEY_GPS_ENABLED, true)
        locationTracker.useGpsForLocation(useGps)
    }
}
