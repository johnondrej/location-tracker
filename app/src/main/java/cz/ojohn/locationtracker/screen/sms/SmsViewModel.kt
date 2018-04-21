package cz.ojohn.locationtracker.screen.sms

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.location.LocationManager
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

    fun getSmsCommandsList(): Array<SmsCommand> {
        return arrayOf(SmsCommand(SmsController.SMS_KEYWORD_LOCATION, R.string.sms_description_location, true),
                SmsCommand(SmsController.SMS_KEYWORD_GPS, R.string.sms_description_gps, true))
    }

    fun formatCommand(command: SmsCommand): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(SmsController.SMS_KEYWORD)
                .append(' ')
        if (command.requiresPassword) {
            stringBuilder.append(appContext.getString(R.string.sms_description_password))
                    .append(' ')
        }
        stringBuilder.append(command.command)
        return stringBuilder.toString()
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
