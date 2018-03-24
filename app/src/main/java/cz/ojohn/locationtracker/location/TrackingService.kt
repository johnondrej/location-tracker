package cz.ojohn.locationtracker.location

import android.app.Service
import android.content.Intent
import android.os.IBinder
import cz.ojohn.locationtracker.App
import cz.ojohn.locationtracker.util.NotificationController
import javax.inject.Inject

/**
 * Android service managing location tracking
 */
class TrackingService : Service() {

    @Inject
    lateinit var locationTracker: LocationTracker
    @Inject
    lateinit var notificationController: NotificationController

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        App.instance.appComponent.inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationTracker.disableTracking()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NotificationController.TRACKING_NOTIFICATION_ID,
                notificationController.getLocationTrackingNotification())
        locationTracker.enableTracking()
        return START_STICKY
    }
}
