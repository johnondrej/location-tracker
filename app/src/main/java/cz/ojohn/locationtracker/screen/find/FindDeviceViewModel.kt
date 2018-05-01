package cz.ojohn.locationtracker.screen.find

import android.arch.lifecycle.ViewModel
import android.telephony.PhoneNumberUtils
import cz.ojohn.locationtracker.location.DeviceFinder
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * ViewModel for Find device screen
 */
class FindDeviceViewModel @Inject constructor(private val deviceFinder: DeviceFinder) : ViewModel() {

    private val disposables: CompositeDisposable = CompositeDisposable()
    private val findingStateSubject: BehaviorSubject<DeviceFinder.FindingStatus> = BehaviorSubject.create()

    init {
        disposables.add(deviceFinder.observeFindingStatus()
                .subscribe { findingStateSubject.onNext(it) })
    }

    fun onStartFinding(phone: String, smsPassword: String): Boolean {
        if (findingStateSubject.value !is DeviceFinder.FindingStatus.Finding) {
            val isPhoneValid = PhoneNumberUtils.isGlobalPhoneNumber(phone)
            if (isPhoneValid && smsPassword.isNotBlank()) {
                deviceFinder.startFinding(phone, smsPassword)
                return true
            }
            return false
        } else {
            deviceFinder.stopFindingService()
            return true
        }
    }

    fun observeFindingState(): Observable<DeviceFinder.FindingStatus> = findingStateSubject

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
