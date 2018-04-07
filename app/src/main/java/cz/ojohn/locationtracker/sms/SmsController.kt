package cz.ojohn.locationtracker.sms

import android.content.Context
import android.location.Location
import android.telephony.SmsManager
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.data.UserPreferences
import cz.ojohn.locationtracker.location.LocationTracker
import java.text.DateFormat
import java.util.*

/**
 * Class used to handle SMS messages and alarms
 */
class SmsController(private val appContext: Context,
                    private val userPreferences: UserPreferences) {

    companion object {
        const val SMS_KEYWORD = "LT"
        const val SMS_KEYWORD_LOCATION = "LOCATION"

        const val SMS_DATA_DELIMITER = ';'

        const val FORMAT_ACCURACY = "%.1f m"
        const val FORMAT_BATTERY = "%d %"
    }

    private val smsManager: SmsManager
        get() = SmsManager.getDefault()

    val smsSettings: Settings
        get() = userPreferences.getSmsSettings()

    fun sendSmsAlarm(phone: String) {
        sendSms(phone, appContext.getString(R.string.sms_alarm))
    }

    fun sendLowBatteryNotification(phone: String, turningOff: Boolean) {
        val notificationText = when (turningOff) {
            true -> appContext.getString(R.string.sms_tracking_battery_off)
            false -> appContext.getString(R.string.sms_tracking_battery_low)
        }
        sendSms(phone, notificationText)
    }

    fun sendChargerNotification(phone: String, isConnected: Boolean) {
        val notificationText = when (isConnected) {
            true -> appContext.getString(R.string.sms_tracking_charger_connected)
            false -> appContext.getString(R.string.sms_tracking_charger_disconnected)
        }
        sendSms(phone, notificationText)
    }

    fun sendDeviceLocation(phone: String, locationResponse: LocationTracker.LocationResponse?) {
        if (locationResponse != null) {
            val formatLocationInfoSms = formatLocationInfoSms(locationResponse)
            sendSms(phone, formatLocationInfoSms)
        } else {
            sendSms(phone, appContext.getString(R.string.sms_response_error_no_location))
        }
    }

    fun processIncomingSms(sender: String, sms: String): SmsAction {
        var input = sms.trim()
        if (input.startsWith(SMS_KEYWORD, true)) {
            input = input.substring(SMS_KEYWORD.length, input.length).trim()
            if (input.contains(SMS_KEYWORD_LOCATION, true)) {
                return SmsAction.SendLocation(sender)
            }
            return SmsAction.None()
        } else {
            return SmsAction.None()
        }
    }

    fun formatLocationInfoSms(locationResponse: LocationTracker.LocationResponse): String {
        val location = locationResponse.locationEntry
        val settings = smsSettings
        val stringBuilder = StringBuilder()
        if (settings.sendGps) {
            stringBuilder.append(formatCoordinates(location.lat, location.lon))
        }
        if (settings.sendLocationName) {
            val locationName = locationResponse.locationName
                    ?: appContext.getString(R.string.sms_response_name_unknown)
            stringBuilder.append(SMS_DATA_DELIMITER).append(locationName)
        }
        if (settings.sendLocationTime) {
            val time = DateFormat.getDateTimeInstance().format(Date(location.time))
            stringBuilder.append(SMS_DATA_DELIMITER).append(time)
        }
        if (settings.sendLocationAccuracy) {
            val accuracy = if (location.accuracy != null) FORMAT_ACCURACY.format(location.accuracy)
            else appContext.getString(R.string.sms_response_accuracy_unknown)

            stringBuilder.append(SMS_DATA_DELIMITER).append(accuracy)
        }
        if (settings.sendLocationSource) {
            val source = locationResponse.locationSource
                    ?: appContext.getString(R.string.sms_response_src_unknown)
            stringBuilder.append(SMS_DATA_DELIMITER).append(source)
        }
        if (settings.sendBattery) {
            val battery = if (locationResponse.batteryStatus != null)
                FORMAT_BATTERY.format(locationResponse.batteryStatus)
            else appContext.getString(R.string.sms_response_battery_unknown)

            stringBuilder.append(SMS_DATA_DELIMITER).append(battery)
        }
        if (settings.sendWiFi) {
            val wifi = locationResponse.wifiName
                    ?: appContext.getString(R.string.sms_response_wifi_unknown)
            stringBuilder.append(SMS_DATA_DELIMITER).append(wifi)
        }
        if (settings.sendIpAddress) {
            stringBuilder.append(SMS_DATA_DELIMITER)
            val ip = locationResponse.ipAddr
                    ?: appContext.getString(R.string.sms_response_ip_unknown)
            stringBuilder.append(SMS_DATA_DELIMITER).append(ip)
        }
        return stringBuilder.toString()
    }

    private fun formatCoordinates(latitude: Double, longitude: Double): String {
        val strLatitude = Location.convert(Math.abs(latitude), Location.FORMAT_DEGREES)
        val strLongitude = Location.convert(Math.abs(longitude), Location.FORMAT_DEGREES)
        val latDescription = if (latitude >= 0) appContext.getString(R.string.sms_response_latitude_north)
        else appContext.getString(R.string.sms_response_latitude_south)
        val lonDescription = if (longitude >= 0) appContext.getString(R.string.sms_response_longitude_east)
        else appContext.getString(R.string.sms_response_longitude_west)

        return appContext.getString(R.string.sms_response_gps_format,
                strLatitude, latDescription, strLongitude, lonDescription)
    }

    private fun sendSms(phone: String, text: String) {
        val textDivided = smsManager.divideMessage(text)
        if (textDivided.size > 1) {
            smsManager.sendMultipartTextMessage(phone, null, textDivided, null, null)
        } else {
            smsManager.sendTextMessage(phone, null, text, null, null)
        }
    }

    data class Settings(val sendGps: Boolean,
                        val sendLocationName: Boolean,
                        val sendLocationTime: Boolean,
                        val sendLocationAccuracy: Boolean,
                        val sendLocationSource: Boolean,
                        val sendBattery: Boolean,
                        val sendWiFi: Boolean,
                        val sendIpAddress: Boolean)

    sealed class SmsAction {
        class None : SmsAction()
        class SendLocation(val phone: String) : SmsAction()
    }
}
