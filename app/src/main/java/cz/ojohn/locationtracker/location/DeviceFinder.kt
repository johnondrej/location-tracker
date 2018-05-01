package cz.ojohn.locationtracker.location

import android.content.Context
import android.content.Intent
import cz.ojohn.locationtracker.data.LocationEntry
import cz.ojohn.locationtracker.screen.find.FindDeviceService
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

/**
 * Class that automatically sends location SMS request and process its result
 */
class DeviceFinder(private val appContext: Context) {

    private val statusSubject: BehaviorSubject<FindingStatus> = BehaviorSubject.createDefault(FindingStatus.Initial())

    private var serviceIntent: Intent? = null

    fun startFinding(phone: String, smsPassword: String) {
        serviceIntent = FindDeviceService.getIntent(appContext, phone, smsPassword)
        appContext.startService(serviceIntent)
        statusSubject.onNext(FindingStatus.Finding(phone))
    }

    fun onDeviceFound(phone: String, location: LocationEntry) {
        statusSubject.onNext(FindingStatus.Found(phone, location))
    }

    fun onFindingCancelled() {
        statusSubject.onNext(FindingStatus.Initial())
    }

    fun stopFindingService() {
        appContext.stopService(serviceIntent)
    }

    fun observeFindingStatus(): Observable<FindingStatus> = statusSubject

    sealed class FindingStatus {
        class Initial : FindingStatus()
        class Finding(val phone: String) : FindingStatus()
        class Found(val phone: String, val location: LocationEntry) : FindingStatus()
    }
}
