package cz.ojohn.locationtracker.util

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cz.ojohn.locationtracker.App
import cz.ojohn.locationtracker.location.LocationTracker
import javax.inject.Inject

/**
 * BroadcastReceiver that starts needed services after system launch
 */
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var locationTracker: LocationTracker

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        App.instance.appComponent.inject(this)
        locationTracker.restartTracking()
    }
}
