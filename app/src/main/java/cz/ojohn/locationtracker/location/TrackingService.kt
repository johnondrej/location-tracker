package cz.ojohn.locationtracker.location

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import cz.ojohn.locationtracker.App
import cz.ojohn.locationtracker.util.NotificationController
import cz.ojohn.locationtracker.util.powerManager
import javax.inject.Inject

/**
 * Android service managing location tracking
 */
class TrackingService : Service() {

    companion object {
        private const val TAG_WAKELOCK = "tracking_wl"

        fun getIntent(context: Context) = Intent(context, TrackingService::class.java)
    }

    var wakeLock: PowerManager.WakeLock? = null

    @Inject
    lateinit var locationTracker: LocationTracker
    @Inject
    lateinit var notificationController: NotificationController

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        App.instance.appComponent.inject(this)
        startForeground(NotificationController.TRACKING_NOTIFICATION_ID,
                notificationController.getLocationTrackingNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        locationTracker.disableTracking()
        releaseWakeLock()
    }

    @SuppressLint("WakelockTimeout")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        releaseWakeLock()
        wakeLock = applicationContext.powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG_WAKELOCK).apply {
            acquire()
        }

        val trackingEnabled = locationTracker.enableTracking()
        if (!trackingEnabled) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }
}
