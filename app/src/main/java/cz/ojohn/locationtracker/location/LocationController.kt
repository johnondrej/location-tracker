package cz.ojohn.locationtracker.location

import android.content.Context
import android.content.SharedPreferences
import android.location.LocationManager
import cz.ojohn.locationtracker.util.locationManager
import java.security.InvalidParameterException

/**
 * Class used to control location settings
 */
class LocationController(private val appContext: Context,
                         private val sharedPreferences: SharedPreferences) {

    companion object {
        const val KEY_LOCATION_PROVIDER = "location_provider"
    }

    var locationSource: LocationSource
        get() {
            val androidProvider = sharedPreferences.getString(KEY_LOCATION_PROVIDER, LocationManager.GPS_PROVIDER)
            return getLocationSourceFromString(androidProvider)
        }
        set(value) = sharedPreferences.edit().putString(KEY_LOCATION_PROVIDER, value.androidProvider).apply()

    fun isLocationAllowed(): Boolean {
        return isLocationSourceAllowed(locationSource)
    }

    fun isLocationSourceAllowed(locationSource: LocationSource): Boolean {
        return try {
            appContext.locationManager.isProviderEnabled(locationSource.androidProvider)
        } catch (ex: SecurityException) {
            false
        }
    }

    private fun getLocationSourceFromString(androidProvider: String): LocationSource {
        return when (androidProvider) {
            LocationManager.GPS_PROVIDER -> LocationSource.GPS
            LocationManager.NETWORK_PROVIDER -> LocationSource.NETWORK
            else -> throw InvalidParameterException("Invalid location provider")
        }
    }

    enum class LocationSource(val androidProvider: String) {
        GPS(LocationManager.GPS_PROVIDER),
        NETWORK(LocationManager.NETWORK_PROVIDER)
    }
}
