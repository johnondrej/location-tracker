package cz.ojohn.locationtracker.screen.main

import android.arch.lifecycle.ViewModel
import cz.ojohn.locationtracker.location.DeviceFinder
import cz.ojohn.locationtracker.location.LocationTracker
import io.reactivex.Observable
import javax.inject.Inject

/**
 * ViewModel for main menu
 */
class MainViewModel @Inject constructor(private val locationTracker: LocationTracker,
                                        private val deviceFinder: DeviceFinder) : ViewModel() {

    fun observeTrackingStatus(): Observable<LocationTracker.TrackingStatus> = locationTracker.observeTrackingStatus()

    fun observeDeviceFindingStatus(): Observable<DeviceFinder.FindingStatus> = deviceFinder.observeFindingStatus()
}
