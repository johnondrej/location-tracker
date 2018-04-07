package cz.ojohn.locationtracker.screen.sms

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModel
import android.content.Context
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.data.LocationEntry
import cz.ojohn.locationtracker.location.LocationTracker
import cz.ojohn.locationtracker.sms.SmsController
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * ViewModel for SMS configuration screen
 */
@SuppressLint("StaticFieldLeak")
class SmsViewModel @Inject constructor(private val appContext: Context,
                                       private val smsController: SmsController) : ViewModel() {

    companion object {
        const val EXAMPLE_TIME = 1510002300000
        const val EXAMPLE_SOURCE = "GPS"
        const val EXAMPLE_WIFI = "TurboWiFi"
    }

    private val exampleSmsSubject: BehaviorSubject<String> = BehaviorSubject.createDefault(getExampleMessageText())

    override fun onCleared() {
        super.onCleared()
    }

    fun onMessageSettingsChanged() {
        exampleSmsSubject.onNext(getExampleMessageText())
    }

    private fun getExampleMessageText(): String {
        val locationResponse = LocationTracker.LocationResponse(
                LocationEntry(appContext.getString(R.string.sms_example_lat).toDouble(),
                        appContext.getString(R.string.sms_example_lon).toDouble(), null, null,
                        EXAMPLE_TIME),
                appContext.getString(R.string.sms_example_location_name),
                EXAMPLE_SOURCE,
                85,
                EXAMPLE_WIFI,
                appContext.getString(R.string.sms_example_ip)
        )
        return smsController.formatLocationInfoSms(locationResponse)
    }

    fun observeExampleMessage(): Observable<String> = exampleSmsSubject
}
