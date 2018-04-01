package cz.ojohn.locationtracker.data

import android.content.SharedPreferences
import cz.ojohn.locationtracker.Constants
import cz.ojohn.locationtracker.location.LocationTracker

/**
 * Class for controlling user preferences and providing access to SharedPreferences
 */
class UserPreferences(private val sharedPreferences: SharedPreferences) {

    companion object {
        const val TRACKING_MIN_FREQUENCY = 5
        const val TRACKING_MAX_FREQUENCY = 300
        const val TRACKING_MIN_RADIUS = 100
        const val TRACKING_MAX_RADIUS = 10000
        const val TRACKING_DEFAULT_RADIUS = 300

        const val KEY_TRACKING_LAST_STATUS = "tracking_last_status"
        const val KEY_TRACKING_LATITUDE = "tracking_latitude"
        const val KEY_TRACKING_LONGITUDE = "tracking_longitude"
        const val KEY_TRACKING_FREQUENCY = "tracking_frequency"
        const val KEY_TRACKING_FREQUENCY_UNIT = "tracking_frequency_unit"
        const val KEY_TRACKING_RADIUS = "tracking_radius"
        const val KEY_TRACKING_RADIUS_UNIT = "tracking_radius_unit"
        const val KEY_TRACKING_PHONE = "tracking_phone"
        const val KEY_TRACKING_CONSTANT = "tracking_constantly"
        const val KEY_TRACKING_FALSE_ALARMS = "tracking_false_alarms"
        const val KEY_TRACKING_BATTERY_NOTIFY = "tracking_low_battery_notify"
        const val KEY_TRACKING_BATTERY_OFF = "tracking_low_battery_off"
        const val KEY_TRACKING_CHARGER = "tracking_charger_detect"
    }

    fun getTrackingFrequency(): TrackingFrequency {
        return TrackingFrequency(sharedPreferences.getInt(KEY_TRACKING_FREQUENCY, TRACKING_MIN_FREQUENCY),
                sharedPreferences.getString(KEY_TRACKING_FREQUENCY_UNIT, Constants.UNIT_MINUTES))
    }

    fun getTrackingRadius(): TrackingRadius {
        return TrackingRadius(sharedPreferences.getInt(KEY_TRACKING_RADIUS, TRACKING_DEFAULT_RADIUS),
                sharedPreferences.getString(KEY_TRACKING_RADIUS_UNIT, Constants.UNIT_METERS))
    }

    fun getTrackingSettings(): LocationTracker.Settings {
        return LocationTracker.Settings(getFloat(KEY_TRACKING_LATITUDE, 0f).toDouble(),
                getFloat(KEY_TRACKING_LONGITUDE, 0f).toDouble(),
                getTrackingFrequency(),
                getTrackingRadius(),
                getString(KEY_TRACKING_PHONE, ""),
                getBoolean(KEY_TRACKING_CONSTANT, true),
                getBoolean(KEY_TRACKING_FALSE_ALARMS, true),
                getBoolean(KEY_TRACKING_BATTERY_NOTIFY, true),
                getBoolean(KEY_TRACKING_BATTERY_OFF, false),
                getBoolean(KEY_TRACKING_CHARGER, false))
    }

    fun setTrackingSettings(trackingSettings: LocationTracker.Settings) {
        sharedPreferences.edit()
                .putInt(KEY_TRACKING_FREQUENCY, trackingSettings.frequency.value)
                .putString(KEY_TRACKING_FREQUENCY_UNIT, trackingSettings.frequency.selectedUnit)
                .putInt(KEY_TRACKING_RADIUS, trackingSettings.radius.value)
                .putString(KEY_TRACKING_RADIUS_UNIT, trackingSettings.radius.selectedUnit)
                .putString(KEY_TRACKING_PHONE, trackingSettings.phone)
                .putBoolean(KEY_TRACKING_CONSTANT, trackingSettings.trackConstantly)
                .putBoolean(KEY_TRACKING_FALSE_ALARMS, trackingSettings.reduceFalseAlarms)
                .putBoolean(KEY_TRACKING_BATTERY_NOTIFY, trackingSettings.lowBatteryNotify)
                .putBoolean(KEY_TRACKING_BATTERY_OFF, trackingSettings.lowBatteryTurnOff)
                .putBoolean(KEY_TRACKING_CHARGER, trackingSettings.chargerNotify)
                .apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue)
    }

    fun getStringSet(key: String, defaultValue: Set<String>): MutableSet<String> {
        return sharedPreferences.getStringSet(key, defaultValue)
    }

    fun put(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun put(key: String, value: Float) {
        sharedPreferences.edit().putFloat(key, value).apply()
    }

    fun put(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    fun put(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }

    fun put(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun put(key: String, value: Set<String>) {
        sharedPreferences.edit().putStringSet(key, value).apply()
    }

    fun edit(): Editor {
        return PreferenceEditor(sharedPreferences.edit())
    }

    interface Editor {
        fun put(key: String, value: Boolean): Editor
        fun put(key: String, value: Float): Editor
        fun put(key: String, value: Int): Editor
        fun put(key: String, value: Long): Editor
        fun put(key: String, value: String): Editor
        fun put(key: String, value: Set<String>): Editor
        fun remove(key: String): Editor
        fun apply()
        fun commit()
        fun clear()
    }

    private class PreferenceEditor(private val editor: SharedPreferences.Editor) : Editor {

        override fun put(key: String, value: Boolean): Editor {
            editor.putBoolean(key, value)
            return this
        }

        override fun put(key: String, value: Float): Editor {
            editor.putFloat(key, value)
            return this
        }

        override fun put(key: String, value: Int): Editor {
            editor.putInt(key, value)
            return this
        }

        override fun put(key: String, value: Long): Editor {
            editor.putLong(key, value)
            return this
        }

        override fun put(key: String, value: String): Editor {
            editor.putString(key, value)
            return this
        }

        override fun put(key: String, value: Set<String>): Editor {
            editor.putStringSet(key, value)
            return this
        }

        override fun remove(key: String): Editor {
            editor.remove(key)
            return this
        }

        override fun apply() {
            editor.apply()
        }

        override fun commit() {
            editor.commit()
        }

        override fun clear() {
            editor.clear()
        }
    }
}
