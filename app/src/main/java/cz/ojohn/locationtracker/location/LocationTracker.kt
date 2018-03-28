package cz.ojohn.locationtracker.location

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationProvider
import android.os.Bundle
import cz.ojohn.locationtracker.data.LocationEntry
import cz.ojohn.locationtracker.data.TrackingFrequency
import cz.ojohn.locationtracker.data.TrackingRadius
import cz.ojohn.locationtracker.data.UserPreferences
import cz.ojohn.locationtracker.util.locationManager
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

/**
 * Class that is responsible for performing location tracking
 */
class LocationTracker(private val appContext: Context,
                      private val userPreferences: UserPreferences,
                      private val locationController: LocationController) : LocationListener {

    private val locationSubject: PublishSubject<LocationEntry> = PublishSubject.create()
    private val statusSubject: BehaviorSubject<TrackingStatus> = BehaviorSubject.createDefault(TrackingStatus.DISABLED)

    override fun onLocationChanged(location: Location) {
        locationSubject.onNext(LocationEntry(location.latitude, location.longitude,
                location.altitude, location.accuracy, location.time))
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {
        if (provider == locationController.locationSource.androidProvider) {
            if (status == LocationProvider.AVAILABLE || status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
                if (statusSubject.value == TrackingStatus.NOT_AVAILABLE) {
                    startTrackingService()
                    statusSubject.onNext(TrackingStatus.RUNNING)
                }
            } else {
                statusSubject.onNext(TrackingStatus.NOT_AVAILABLE)
            }
        }
    }

    override fun onProviderEnabled(provider: String?) {}

    override fun onProviderDisabled(provider: String?) {
        if (provider == locationController.locationSource.androidProvider) {
            statusSubject.onNext(TrackingStatus.NOT_AVAILABLE)
        }
    }

    fun startTrackingService() {
        if (statusSubject.value != TrackingStatus.RUNNING) {
            appContext.startService(TrackingService.getIntent(appContext))
        }
    }

    fun enableTracking() {
        try {
            appContext.locationManager.requestLocationUpdates(locationController.locationSource.androidProvider,
                    0, 0f, this)
            statusSubject.onNext(TrackingStatus.RUNNING)
        } catch (ex: SecurityException) {
            throw IllegalStateException("Tried to launch enable tracking without permissions")
        }

    }

    fun disableTracking() {
        appContext.locationManager.removeUpdates(this)
        appContext.stopService(TrackingService.getIntent(appContext))
        statusSubject.onNext(TrackingStatus.DISABLED)
    }

    fun getSettings(): LocationTracker.Settings {
        return userPreferences.getTrackingSettings()
    }

    fun updateSettings(settings: Settings) {
        userPreferences.setTrackingSettings(settings)
    }

    fun observeLocationUpdates(): Observable<LocationEntry> = locationSubject

    fun observeTrackingStatus(): Observable<TrackingStatus> = statusSubject

    enum class TrackingStatus {
        RUNNING, // Location tracking is running
        DISABLED, // Location tracking is disabled
        NOT_AVAILABLE; // Location tracking should be enabled, but it is not available because of permissions or system settings
    }

    data class Settings(val frequency: TrackingFrequency,
                        val radius: TrackingRadius,
                        val phone: String,
                        val trackConstantly: Boolean,
                        val reduceFalseAlarms: Boolean,
                        val lowBatteryNotify: Boolean,
                        val lowBatteryTurnOff: Boolean,
                        val chargerNotify: Boolean)
}
