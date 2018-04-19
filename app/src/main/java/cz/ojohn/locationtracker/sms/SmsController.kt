package cz.ojohn.locationtracker.sms

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.telephony.SmsManager
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.data.LocationEntry
import cz.ojohn.locationtracker.data.UserPreferences
import cz.ojohn.locationtracker.location.LocationTracker
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
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
        const val SMS_KEYWORD_GPS = "GPS"
        const val SMS_KEYWORD_GPS_RESPONSE = "FIND_RESPONSE"

        const val SMS_DATA_DELIMITER = ';'

        const val FORMAT_ACCURACY = "%.1f m"
    }

    private val smsManager: SmsManager
        get() = SmsManager.getDefault()

    private val actionSubject: PublishSubject<SmsAction> = PublishSubject.create()

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

    fun sendDeviceLocation(phone: String, locationResponse: LocationTracker.LocationResponse?, onlyCoords: Boolean) {
        if (locationResponse != null) {
            val formatLocationInfoSms = if (!onlyCoords) formatLocationInfoSms(locationResponse)
            else formatOutgoingGps(locationResponse)
            sendSms(phone, formatLocationInfoSms)
        } else {
            sendSms(phone, appContext.getString(R.string.sms_response_error_no_location))
        }
    }

    fun sendGpsRequest(phone: String) {
        sendSms(phone, "$SMS_KEYWORD ${userPreferences.getSmsPassword()} $SMS_KEYWORD_GPS")
    }

    fun processIncomingSms(sender: String, sms: String): SmsAction {
        val smsPassword = userPreferences.getSmsPassword()
        var input = sms.trim()
        if (input.startsWith(SMS_KEYWORD, true)) {
            input = input.substring(SMS_KEYWORD.length, input.length).trim()
            if (input.contains(smsPassword)) {
                input = input.substring(smsPassword.length, input.length).trim()
                return when {
                    input.contains(SMS_KEYWORD_LOCATION, true) -> SmsAction.SendLocation(sender, false)
                    input.contains(SMS_KEYWORD_GPS, true) -> SmsAction.SendLocation(sender, true)
                    else -> SmsAction.None()
                }
            } else {
                return when {
                    input.contains(SMS_KEYWORD_GPS_RESPONSE, true) -> formatGpsResponse(sender, input)
                    else -> SmsAction.None()
                }
            }
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
            val source = appContext.getString(when (locationResponse.locationSource) {
                LocationManager.GPS_PROVIDER -> R.string.source_gps
                LocationManager.NETWORK_PROVIDER -> R.string.source_network
                LocationManager.PASSIVE_PROVIDER -> R.string.source_passive
                else -> R.string.source_unknown
            })
            stringBuilder.append(SMS_DATA_DELIMITER).append(source)
        }
        if (settings.sendBattery) {
            val battery = if (locationResponse.batteryStatus != null)
                appContext.getString(R.string.sms_response_battery_format, locationResponse.batteryStatus)
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
            stringBuilder.append(ip)
        }
        return stringBuilder.toString()
    }

    fun updateSmsSettingsEntry(key: String, isEnabled: Boolean) {
        userPreferences.edit()
                .put(key, isEnabled)
                .apply()
    }

    fun onNewSmsAction(action: SmsAction) {
        actionSubject.onNext(action)
    }

    fun observeSmsActions(): Observable<SmsAction> = actionSubject

    private fun formatGpsResponse(phone: String, receivedInput: String): SmsAction.GpsReceived {
        val data = receivedInput.split(';', ignoreCase = true)
        return SmsAction.GpsReceived(phone, LocationEntry(data[1].toDouble(), data[2].toDouble(),
                null, null, data[3].toLong(), null))
    }

    private fun formatOutgoingGps(locationResponse: LocationTracker.LocationResponse): String {
        val stringBuilder = StringBuilder().apply {
            append(SMS_KEYWORD)
            append(' ')
            append(SMS_KEYWORD_GPS_RESPONSE)
            append(SMS_DATA_DELIMITER)
            append(locationResponse.locationEntry.lat)
            append(SMS_DATA_DELIMITER)
            append(locationResponse.locationEntry.lon)
            append(SMS_DATA_DELIMITER)
            append(locationResponse.locationEntry.time)
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
        class SendLocation(val phone: String, val onlyCoords: Boolean) : SmsAction()
        class GpsReceived(val phone: String, val location: LocationEntry) : SmsAction()
    }
}
