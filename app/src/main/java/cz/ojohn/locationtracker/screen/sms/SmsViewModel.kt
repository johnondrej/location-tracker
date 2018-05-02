package cz.ojohn.locationtracker.screen.sms

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.location.LocationManager
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.data.LocationEntry
import cz.ojohn.locationtracker.data.UserPreferences
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
                                       private val userPreferences: UserPreferences,
                                       private val smsController: SmsController) : ViewModel() {

    companion object {
        const val EXAMPLE_TIME = 1509998700000
        const val EXAMPLE_SOURCE = LocationManager.GPS_PROVIDER
        const val EXAMPLE_WIFI = "TurboWiFi"
        const val EXAMPLE_ACCURACY = 20f
        const val EXAMPLE_BATTERY = 85
    }

    private val exampleSmsSubject: BehaviorSubject<String> = BehaviorSubject.createDefault(getExampleMessageText())

    fun onMessageSettingsEntryChanged(key: String, isEnabled: Boolean) {
        smsController.updateSmsSettingsEntry(key, isEnabled)
        exampleSmsSubject.onNext(getExampleMessageText())
    }

    fun getSmsSettings(): SmsController.Settings = smsController.smsSettings

    fun getSmsPassword(): String = userPreferences.getSmsPassword()

    fun getSmsCommandsList(): Array<SmsCommand> {
        return arrayOf(SmsCommand(SmsController.SMS_KEYWORD_FIND, R.string.sms_description_find, true),
                SmsCommand(SmsController.SMS_KEYWORD_GPS, R.string.sms_description_gps, true),
                SmsCommand(SmsController.SMS_KEYWORD_BATTERY, R.string.sms_description_battery, false))
    }

    private fun getExampleMessageText(): String {
        val locationResponse = LocationTracker.LocationResponse(
                LocationEntry(appContext.getString(R.string.sms_example_lat).toDouble(),
                        appContext.getString(R.string.sms_example_lon).toDouble(), null, EXAMPLE_ACCURACY,
                        EXAMPLE_TIME),
                appContext.getString(R.string.sms_example_location_name),
                EXAMPLE_SOURCE,
                EXAMPLE_BATTERY,
                EXAMPLE_WIFI,
                appContext.getString(R.string.sms_example_ip)
        )
        return smsController.formatLocationInfoSms(locationResponse)
    }

    fun observeExampleMessage(): Observable<String> = exampleSmsSubject
}
