package cz.ojohn.locationtracker.util

import android.location.Location
import android.os.PowerManager
import cz.ojohn.locationtracker.data.LocationEntry
import java.math.BigDecimal

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

fun Double.roundToDecimalPlaces(decimalPlaces: Int): Double {
    return BigDecimal(this).setScale(decimalPlaces, BigDecimal.ROUND_UP).toDouble()
}
