package cz.ojohn.locationtracker.util

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager

/**
 * File with various extension functions for Android Context
 */
val Context.locationManager: LocationManager
    get() = getSystemService(Context.LOCATION_SERVICE) as LocationManager

val Context.notificationManager: NotificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

val Context.powerManager: PowerManager
    get() = getSystemService(Context.POWER_SERVICE) as PowerManager

val Context.wifiManager: WifiManager
    get() = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

fun Context.startForegroundServiceCompat(intent: Intent) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}

fun Context.getBatteryPercentage(): Int {
    val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val status = applicationContext.registerReceiver(null, intentFilter)

    val batteryLevel = status.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val batteryScale = status.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    return (batteryLevel / batteryScale.toFloat() * 100).toInt()
}

fun Context.isBatteryChargingOrFull(): Boolean {
    val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val status = applicationContext.registerReceiver(null, intentFilter).getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
}
