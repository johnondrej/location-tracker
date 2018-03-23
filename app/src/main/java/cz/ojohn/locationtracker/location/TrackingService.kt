package cz.ojohn.locationtracker.location

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import cz.ojohn.locationtracker.App
import cz.ojohn.locationtracker.data.LocationEntry
import cz.ojohn.locationtracker.util.NotificationController
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Android service managing location tracking
 */
class TrackingService : Service() {

    @Inject
    lateinit var locationTracker: LocationTracker
    @Inject
    lateinit var notificationController: NotificationController

    private lateinit var disposables: CompositeDisposable

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        App.instance.appComponent.inject(this)
        disposables = CompositeDisposable().apply {
            add(locationTracker.observeLocationUpdates()
                    .subscribe { onLocationReceived(it) })
            add(locationTracker.observeTrackingStatus()
                    .subscribe { onTrackingStatusChanged(it) })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationTracker.disableTracking()
        disposables.clear()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NotificationController.TRACKING_NOTIFICATION_ID,
                notificationController.getLocationTrackingNotification())
        locationTracker.enableTracking()
        return START_STICKY
    }

    private fun onLocationReceived(location: LocationEntry) {
        Log.d("TrackingService", "onLocationReceived: $location")
    }

    private fun onTrackingStatusChanged(trackingStatus: LocationTracker.TrackingStatus) {
        Log.d("TrackingService", "onTrackingStatusChanged: $trackingStatus")
    }
}
