package cz.ojohn.locationtracker.screen.tracking

import android.arch.lifecycle.ViewModel
import cz.ojohn.locationtracker.location.LocationTracker
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * ViewModel for Tracking screen
 */
class TrackingViewModel @Inject constructor(private val locationTracker: LocationTracker) : ViewModel() {

    private val disposables: CompositeDisposable = CompositeDisposable()
    private val statusSubject: BehaviorSubject<LocationTracker.TrackingStatus> = BehaviorSubject.create()

    init {
        disposables.add(locationTracker.observeTrackingStatus()
                .subscribe { statusSubject.onNext(it) })
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun observeTrackingStatus(): Observable<LocationTracker.TrackingStatus> = statusSubject
}
