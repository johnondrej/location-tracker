package cz.ojohn.locationtracker.sms

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.telephony.SmsManager
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.data.LocationEntry
import cz.ojohn.locationtracker.data.UserPreferences
import cz.ojohn.locationtracker.location.LocationTracker
import cz.ojohn.locationtracker.util.getBatteryPercentage
import cz.ojohn.locationtracker.util.roundToDecimalPlaces
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
        const val SMS_KEYWORD_FIND = "FIND"
        const val SMS_KEYWORD_FINDAPP = "FINDAPP"
        const val SMS_KEYWORD_GPS = "GPS"
        const val SMS_KEYWORD_BATTERY = "BATTERY"
        const val SMS_KEYWORD_FINDAPP_RESPONSE = "FIND_RESPONSE"

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

    fun sendDeviceLocation(phone: String, locationResponse: LocationTracker.LocationResponse?, responseType: SmsAction.SendLocation.ResponseType) {
        if (locationResponse != null) {
            val formattedLocation = when (responseType) {
                SmsAction.SendLocation.ResponseType.FULL_RESPONSE -> formatLocationInfoSms(locationResponse)
                SmsAction.SendLocation.ResponseType.ONLY_GPS -> formatGpsLocationInfo(locationResponse)
                SmsAction.SendLocation.ResponseType.FINDING_APP -> formatLocationInfoForApp(locationResponse)
            }
            sendSms(phone, formattedLocation)
        } else {
            sendSms(phone, appContext.getString(R.string.sms_response_error_no_location))
        }
    }

    fun sendFindFromAppRequest(phone: String, smsPassword: String) {
        sendSms(phone, "$SMS_KEYWORD $smsPassword $SMS_KEYWORD_FINDAPP")
    }

    fun sendBattery(phone: String, batteryLevel: Int) {
        sendSms(phone, appContext.getString(R.string.sms_response_battery_format, batteryLevel))
    }

    fun sendPendingRequestError(phone: String) {
        sendSms(phone, appContext.getString(R.string.sms_response_pending))
    }

    fun sendAcceptedRequestInfo(phone: String) {
        sendSms(phone, appContext.getString(R.string.sms_response_start))
    }

    fun processIncomingSms(sender: String, sms: String): SmsAction {
        val smsPassword = userPreferences.getSmsPassword()
        val smsParts = sms.trim().replace("\\s+".toRegex(), " ").split(" ")
        if (smsParts.isNotEmpty() && smsParts[0].equals(SMS_KEYWORD, true)) {
            if (smsParts.size >= 3) {
                if (smsParts[1] == SMS_KEYWORD_FINDAPP_RESPONSE) {
                    return parseAppFindingResponse(sender, smsParts[2])
                } else if (smsParts[1] == smsPassword) {
                    return when (smsParts[2].toUpperCase()) {
                        SMS_KEYWORD_FIND -> SmsAction.SendLocation(sender, SmsAction.SendLocation.ResponseType.FULL_RESPONSE)
                        SMS_KEYWORD_GPS -> SmsAction.SendLocation(sender, SmsAction.SendLocation.ResponseType.ONLY_GPS)
                        SMS_KEYWORD_BATTERY -> SmsAction.SendBattery(sender, appContext.getBatteryPercentage())
                        SMS_KEYWORD_FINDAPP -> SmsAction.SendLocation(sender, SmsAction.SendLocation.ResponseType.FINDING_APP)
                        else -> SmsAction.None()
                    }
                }
            }
        }
        return SmsAction.None()
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

    private fun formatGpsLocationInfo(locationResponse: LocationTracker.LocationResponse): String {
        val location = locationResponse.locationEntry
        val time = DateFormat.getDateTimeInstance().format(Date(location.time))
        return StringBuilder().apply {
            append(formatCoordinates(location.lat, location.lon))
            append(SMS_DATA_DELIMITER).append(time)
        }.toString()
    }

    private fun formatLocationInfoForApp(locationResponse: LocationTracker.LocationResponse): String {
        return StringBuilder().apply {
            append(SMS_KEYWORD)
            append(' ')
            append(SMS_KEYWORD_FINDAPP_RESPONSE)
            append(' ')
            append(locationResponse.locationEntry.lat.roundToDecimalPlaces(5))
            append(SMS_DATA_DELIMITER)
            append(locationResponse.locationEntry.lon.roundToDecimalPlaces(5))
            append(SMS_DATA_DELIMITER)
            append(locationResponse.locationEntry.time)
        }.toString()
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

    private fun parseAppFindingResponse(phone: String, receivedInput: String): SmsAction {
        val data = receivedInput.split(';', ignoreCase = true)
        if (data.size == 3) {
            try {
                return SmsAction.GpsReceived(phone, LocationEntry(data[0].toDouble(), data[1].toDouble(),
                        null, null, data[2].toLong(), null))
            } catch (ex: Exception) {
                return SmsAction.None()
            }
        } else {
            return SmsAction.None()
        }
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
        class SendLocation(val phone: String, val responseType: ResponseType) : SmsAction() {
            enum class ResponseType {
                FULL_RESPONSE,
                ONLY_GPS,
                FINDING_APP
            }
        }

        class SendBattery(val phone: String, val batteryLevel: Int) : SmsAction()
        class GpsReceived(val phone: String, val location: LocationEntry) : SmsAction()
    }
}
