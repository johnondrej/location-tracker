package cz.ojohn.locationtracker.location

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.IBinder
import android.os.PowerManager
import cz.ojohn.locationtracker.App
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.data.LocationEntry
import cz.ojohn.locationtracker.sms.SmsController
import cz.ojohn.locationtracker.util.NotificationController
import cz.ojohn.locationtracker.util.getBatteryPercentage
import cz.ojohn.locationtracker.util.powerManager
import cz.ojohn.locationtracker.util.wifiManager
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Service that tries to obtain user location
 */
class FetchLocationService : Service() {

    companion object {
        private const val TAG_WAKELOCK = "fetching_wl"
        private const val TAG_PHONE = "phone"
        private const val TIMEOUT_SECONDS: Long = 180

        fun getIntent(context: Context, phone: String): Intent {
            return Intent(context, FetchLocationService::class.java).apply {
                putExtra(TAG_PHONE, phone)
            }
        }
    }

    private var wakeLock: PowerManager.WakeLock? = null

    private lateinit var phone: String
    private lateinit var disposables: CompositeDisposable

    @Inject
    lateinit var locationTracker: LocationTracker
    @Inject
    lateinit var smsController: SmsController
    @Inject
    lateinit var notificationController: NotificationController

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        App.instance.appComponent.inject(this)
        startForeground(NotificationController.FETCHING_NOTIFICATION_ID,
                notificationController.getLocationFetchingNotification())
        disposables = CompositeDisposable()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val replyPhone = intent?.extras?.getString(TAG_PHONE)
        if (replyPhone != null) {
            phone = replyPhone
        } else {
            stopSelf()
            return START_NOT_STICKY
        }

        releaseWakeLock()
        wakeLock = applicationContext.powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG_WAKELOCK).apply {
            acquire(TIMEOUT_SECONDS * 1000)
        }

        val locationEnabled = locationTracker.enableLocationUpdates()
        if (!locationEnabled) {
            stopSelf()
            return START_NOT_STICKY
        }
        disposables.add(locationTracker.observeLocationUpdates()
                .take(1)
                .subscribe { onLocationChanged(it) })
        disposables.add(Completable.timer(TIMEOUT_SECONDS, TimeUnit.SECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { onTimeoutPassed() })
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        locationTracker.disableLocationUpdates()
        disposables.clear()
        releaseWakeLock()
    }

    private fun onLocationChanged(locationEntry: LocationEntry) {
        disposables.add(Single.fromCallable { return@fromCallable getLocationResponse(locationEntry) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { response -> onLocationResponseBuilt(response) })
    }

    private fun onTimeoutPassed() {
        val lastKnownLocation = locationTracker.getLastKnownLocation()
        if (lastKnownLocation != null) {
            onLocationChanged(lastKnownLocation)
        } else {
            smsController.sendDeviceLocation(phone, null)
            stopSelf()
        }
    }

    private fun onLocationResponseBuilt(locationResponse: LocationTracker.LocationResponse) {
        smsController.sendDeviceLocation(phone, locationResponse)
        stopSelf()
    }

    private fun getLocationResponse(locationEntry: LocationEntry): LocationTracker.LocationResponse {
        val smsSettings = smsController.smsSettings
        val locationName = if (smsSettings.sendLocationName) getLocationName(locationEntry) else null
        val batteryStatus = if (smsSettings.sendBattery) applicationContext.getBatteryPercentage() else null
        val wifiName = if (smsSettings.sendWiFi) getWifiName() else null
        val ipAddr = if (smsSettings.sendIpAddress) getIpAddress() else null

        return LocationTracker.LocationResponse(
                locationEntry,
                locationName,
                locationEntry.source,
                batteryStatus,
                wifiName,
                ipAddr
        )
    }

    private fun getLocationName(locationEntry: LocationEntry): String? {
        try {
            val geocoder = Geocoder(applicationContext, Locale.getDefault())
            val results = geocoder.getFromLocation(locationEntry.lat, locationEntry.lon, 1)
            if (results?.isNotEmpty() == true) {
                return formatAddress(results.first())
            }
        } catch (ex: IOException) {
            return null
        }
        return null
    }

    private fun formatAddress(address: Address): String? {
        val firstEntry: String? = when {
            address.maxAddressLineIndex >= 0 -> address.getAddressLine(0)
            address.thoroughfare != null -> address.thoroughfare
            else -> address.subLocality
        }
        val secondEntry: String? = when {
            address.maxAddressLineIndex >= 1 -> address.getAddressLine(1)
            address.locality != null -> address.locality
            address.subAdminArea != null -> address.subAdminArea
            address.adminArea != null -> address.adminArea
            else -> address.countryName
        }

        return when {
            firstEntry != null && secondEntry != null -> applicationContext.getString(R.string.sms_response_location_format, firstEntry, secondEntry)
            firstEntry != null && secondEntry == null -> applicationContext.getString(R.string.sms_response_location_short_format, firstEntry)
            firstEntry == null && secondEntry != null -> applicationContext.getString(R.string.sms_response_location_short_format, secondEntry)
            else -> null
        }
    }

    private fun getWifiName(): String? {
        return applicationContext.wifiManager.connectionInfo.ssid
    }

    private fun getIpAddress(): String? {
        // TODO
        return null
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }
}
