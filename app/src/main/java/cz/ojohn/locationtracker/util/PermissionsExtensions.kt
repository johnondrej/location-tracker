package cz.ojohn.locationtracker.util

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat

/**
 * Extension functions for easier permission management
 */
fun Context.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun IntArray.areAllPermissionsGranted(): Boolean {
    this.forEach {
        if (it != PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }
    return true
}
