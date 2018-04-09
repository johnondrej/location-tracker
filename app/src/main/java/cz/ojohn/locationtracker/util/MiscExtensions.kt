package cz.ojohn.locationtracker.util

import android.location.Location
import android.os.PowerManager
import cz.ojohn.locationtracker.data.LocationEntry

/**
 * Mix of various extension functions
 */
fun Location.toLocationEntry(): LocationEntry {
    return LocationEntry(latitude, longitude, altitude, accuracy, time, provider)
}

fun PowerManager.WakeLock.safeRelease() {
    if (isHeld) {
        release()
    }
}
