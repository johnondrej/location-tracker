package cz.ojohn.locationtracker.screen.tracking

import android.arch.lifecycle.ViewModel
import android.telephony.PhoneNumberUtils
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.data.LocationEntry
import cz.ojohn.locationtracker.data.UserPreferences.Companion.TRACKING_MAX_FREQUENCY
import cz.ojohn.locationtracker.data.UserPreferences.Companion.TRACKING_MAX_RADIUS
import cz.ojohn.locationtracker.data.UserPreferences.Companion.TRACKING_MIN_FREQUENCY
import cz.ojohn.locationtracker.data.UserPreferences.Companion.TRACKING_MIN_RADIUS
import cz.ojohn.locationtracker.location.LocationTracker
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * ViewModel for Tracking screen
 */
class TrackingViewModel @Inject constructor(private val locationTracker: LocationTracker) : ViewModel() {

    var askingForPermissions: Boolean = false
    var preserveMarkerPos: Boolean = false

    private var mapLocationEnabled: Boolean = false

    private val disposables: CompositeDisposable = CompositeDisposable()
    private val statusSubject: BehaviorSubject<LocationTracker.TrackingStatus> = BehaviorSubject.create()
    private val formStateSubject: PublishSubject<FormState> = PublishSubject.create()
    private val mapSubject: BehaviorSubject<MapState> = BehaviorSubject.create()

    init {
        disposables.add(locationTracker.observeTrackingStatus()
                .subscribe { statusSubject.onNext(it) })
        disposables.add(locationTracker.observeLocationUpdates()
                .subscribe { onLocationUpdated(it) })
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun onCheckFormValues(settings: LocationTracker.Settings) {
        val frequencyMinutes = settings.frequency.inMinutes
        val radiusMeters = settings.radius.inMeters
        if (!settings.trackConstantly && frequencyMinutes !in TRACKING_MIN_FREQUENCY..TRACKING_MAX_FREQUENCY) {
            formStateSubject.onNext(FormState.Error(R.string.tracking_error_frequency,
                    arrayOf(TRACKING_MIN_FREQUENCY, TRACKING_MAX_FREQUENCY)))
            return
        }
        if (radiusMeters !in TRACKING_MIN_RADIUS..TRACKING_MAX_RADIUS) {
            formStateSubject.onNext(FormState.Error(R.string.tracking_error_radius,
                    arrayOf(TRACKING_MIN_RADIUS, TRACKING_MAX_RADIUS)))
            return
        }
        if (!PhoneNumberUtils.isGlobalPhoneNumber(settings.phone)) {
            formStateSubject.onNext(FormState.Error(R.string.tracking_error_phone))
            return
        }

        locationTracker.updateSettings(settings)
        formStateSubject.onNext(FormState.Valid())
    }

    fun onEnableMapLocation() {
        if (!mapLocationEnabled) {
            if (!mapSubject.hasValue()) {
                locationTracker.getLastKnownLocation()?.let { onLocationUpdated(it) }
            }
            mapLocationEnabled = locationTracker.enableLocationUpdates()
        }
    }

    fun onDisableMapLocation() {
        if (mapLocationEnabled) {
            locationTracker.disableLocationUpdates()
            mapLocationEnabled = false
        }
    }

    fun onEnableTracking() {
        locationTracker.startTrackingService()
    }

    fun onDisableTracking() {
        locationTracker.disableTracking()
    }

    fun getTrackingSettings(): LocationTracker.Settings {
        return locationTracker.getSettings()
    }

    fun observeTrackingStatus(): Observable<LocationTracker.TrackingStatus> = statusSubject

    fun observeFormState(): Observable<FormState> = formStateSubject

    fun observeMapState(): Observable<MapState> = mapSubject

    private fun onLocationUpdated(locationEntry: LocationEntry) {
        val mapState = when (preserveMarkerPos) {
            true -> MapState.PositionAligned(locationEntry)
            false -> MapState.WithoutPosition(locationEntry)
        }
        mapSubject.onNext(mapState)
    }

    sealed class FormState {
        class Valid : FormState()
        class Error(val messageRes: Int, val params: Array<Int> = arrayOf()) : FormState()
    }

    sealed class MapState(val locationEntry: LocationEntry) {
        class WithoutPosition(locationEntry: LocationEntry) : MapState(locationEntry)
        class PositionAligned(locationEntry: LocationEntry) : MapState(locationEntry)
    }
}
