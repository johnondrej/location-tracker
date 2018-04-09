package cz.ojohn.locationtracker.location

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.PowerManager
import cz.ojohn.locationtracker.App
import cz.ojohn.locationtracker.data.LocationEntry
import cz.ojohn.locationtracker.sms.SmsController
import cz.ojohn.locationtracker.util.NotificationController
import cz.ojohn.locationtracker.util.powerManager
import cz.ojohn.locationtracker.util.safeRelease
import io.reactivex.disposables.Disposable
import javax.inject.Inject

/**
 * Android service managing location tracking
 */
class TrackingService : Service() {

    companion object {
        private const val TAG_WAKELOCK = "tracking_wl"

        fun getIntent(context: Context) = Intent(context, TrackingService::class.java)
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private var locationDisposable: Disposable? = null
    private var batteryBroadcastReceiver: BroadcastReceiver? = null
    private var chargerBroadcastReceiver: BroadcastReceiver? = null

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
        startForeground(NotificationController.TRACKING_NOTIFICATION_ID,
                notificationController.getLocationTrackingNotification())
    }

    @SuppressLint("WakelockTimeout")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        wakeLock?.safeRelease()
        wakeLock = applicationContext.powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG_WAKELOCK).apply {
            acquire()
        }

        val trackingEnabled = locationTracker.enableTracking()
        if (!trackingEnabled) {
            stopSelf()
            return START_NOT_STICKY
        }
        locationDisposable = locationTracker.observeLocationUpdates()
                .subscribe { onLocationChanged(it) }
        initBatteryReceiver()
        initChargerReceiver()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        locationTracker.disableTracking()
        locationDisposable?.dispose()
        batteryBroadcastReceiver?.let { applicationContext.unregisterReceiver(it) }
        chargerBroadcastReceiver?.let { applicationContext.unregisterReceiver(it) }
        wakeLock?.safeRelease()
    }

    private fun onLocationChanged(location: LocationEntry) {
        val trackingSettings = locationTracker.getSettings()
        if (locationTracker.distanceBetween(trackingSettings.latitude, trackingSettings.longitude,
                        location.lat, location.lon, location.accuracy) > trackingSettings.radius.inMeters) {
            smsController.sendSmsAlarm(trackingSettings.phone)
            stopSelf()
        }
    }

    private fun initBatteryReceiver() {
        val trackingSettings = locationTracker.getSettings()
        if (trackingSettings.lowBatteryNotify || trackingSettings.lowBatteryTurnOff) {
            batteryBroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    intent?.let {
                        if (it.action == Intent.ACTION_BATTERY_LOW) {
                            smsController.sendLowBatteryNotification(trackingSettings.phone, trackingSettings.lowBatteryTurnOff)
                            if (trackingSettings.lowBatteryTurnOff) {
                                this@TrackingService.stopSelf()
                            }
                        }
                    }
                }
            }
            val intentFilter = IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_LOW)
            }
            applicationContext.registerReceiver(batteryBroadcastReceiver, intentFilter)
        }
    }

    private fun initChargerReceiver() {
        val trackingSettings = locationTracker.getSettings()
        if (trackingSettings.chargerNotify) {
            chargerBroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    intent?.let {
                        val isCharging = when (it.action) {
                            Intent.ACTION_POWER_CONNECTED -> true
                            Intent.ACTION_POWER_DISCONNECTED -> false
                            else -> return
                        }
                        smsController.sendChargerNotification(trackingSettings.phone, isCharging)
                    }
                }
            }
            val intentFilter = IntentFilter().apply {
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            applicationContext.registerReceiver(chargerBroadcastReceiver, intentFilter)
        }
    }
}
