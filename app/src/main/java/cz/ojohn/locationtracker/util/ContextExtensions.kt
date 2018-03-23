package cz.ojohn.locationtracker.util

import android.app.NotificationManager
import android.content.Context
import android.location.LocationManager

/**
 * File with various extension functions for Android Context
 */
val Context.locationManager: LocationManager
    get() = getSystemService(Context.LOCATION_SERVICE) as LocationManager

val Context.notificationManager: NotificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
