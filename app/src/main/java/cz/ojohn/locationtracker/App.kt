package cz.ojohn.locationtracker

import android.app.Application
import com.google.android.gms.maps.MapView
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
        initGoogleMapAsync()
    }

    private fun createNotificationChannels() {
        notificationController.createTrackingNotificationChannel()
    }

    /**
     * Load GooglePlay services after app launch, which leads to faster loading of fragments with map
     */
    private fun initGoogleMapAsync() {
        Thread({
            try {
                val mapView = MapView(this)
                mapView.onCreate(null)
                mapView.onPause()
                mapView.onDestroy()
            } catch (ex: Exception) {
            }
        }).start()
    }
}
