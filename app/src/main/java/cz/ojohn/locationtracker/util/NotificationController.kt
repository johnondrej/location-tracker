package cz.ojohn.locationtracker.util

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationCompat
import cz.ojohn.locationtracker.R

/**
 * Helper class for managing notification settings and channels
 */
class NotificationController(private val context: Context) {

    companion object {
        const val TRACKING_CHANNEL_ID = "tracking_channel"

        const val TRACKING_NOTIFICATION_ID = 1
        const val FETCHING_NOTIFICATION_ID = 2
        const val FINDING_NOTIFICATON_ID = 3
    }

    @SuppressLint("NewApi")
    fun createTrackingNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(TRACKING_CHANNEL_ID,
                    context.getString(R.string.channel_tracking_name),
                    NotificationManager.IMPORTANCE_LOW).apply {
                description = context.getString(R.string.channel_tracking_description)
            }
            context.notificationManager.createNotificationChannel(channel)
        }
    }

    fun getLocationTrackingNotification(): Notification {
        return NotificationCompat.Builder(context, TRACKING_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_tracking)
                .setContentTitle(context.getString(R.string.channel_tracking_name))
                .setContentText(context.getString(R.string.notification_tracking_content))
                .build()
    }

    fun getDeviceFindingNotification(): Notification {
        return NotificationCompat.Builder(context, TRACKING_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_tracking)
                .setContentTitle(context.getString(R.string.notification_finding))
                .setContentText(context.getString(R.string.notification_finding_content))
                .build()
    }

    fun getLocationFetchingNotification(): Notification {
        return NotificationCompat.Builder(context, TRACKING_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_location_fetching)
                .setContentTitle(context.getString(R.string.notification_location_fetching))
                .setContentText(context.getString(R.string.notification_location_fetching_content))
                .build()
    }
}
