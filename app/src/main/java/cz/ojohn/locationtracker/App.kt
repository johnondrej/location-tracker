package cz.ojohn.locationtracker

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import cz.ojohn.locationtracker.di.AppComponent
import cz.ojohn.locationtracker.di.DaggerAppComponent
import cz.ojohn.locationtracker.util.NotificationController
import io.fabric.sdk.android.Fabric
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
        initCrashReporting()
    }

    private fun createNotificationChannels() {
        notificationController.createTrackingNotificationChannel()
    }

    private fun initCrashReporting() {
        val crashlyticsCore = CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build()
        Fabric.with(this, Crashlytics.Builder().core(crashlyticsCore).build())
    }
}
