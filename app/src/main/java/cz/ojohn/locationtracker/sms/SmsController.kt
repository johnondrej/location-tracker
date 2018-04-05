package cz.ojohn.locationtracker.sms

import android.content.Context
import android.telephony.SmsManager
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.data.UserPreferences

/**
 * Class used to handle SMS messages and alarms
 */
class SmsController(private val appContext: Context,
                    private val userPreferences: UserPreferences) {

    private val smsManager: SmsManager
        get() = SmsManager.getDefault()

    fun sendSmsAlarm(phone: String) {
        smsManager.sendTextMessage(phone, null, appContext.getString(R.string.sms_alarm), null, null)
    }
}
