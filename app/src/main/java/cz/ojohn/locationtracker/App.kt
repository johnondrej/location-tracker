package cz.ojohn.locationtracker

import android.app.Application
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import cz.ojohn.locationtracker.di.AppComponent
import cz.ojohn.locationtracker.di.DaggerAppComponent
import cz.ojohn.locationtracker.util.NotificationController
import javax.inject.Inject

/**
 * Custom application class
 */
class App : Application() {

    companion object {
        lateinit var instance: App
    }

    @Inject
    lateinit var notificationController: NotificationController

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        instance = this
        appComponent = DaggerAppComponent.builder()
                .applicationContext(applicationContext)
                .build()
        appComponent.inject(this)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        notificationController.createTrackingNotificationChannel()
    }
}
