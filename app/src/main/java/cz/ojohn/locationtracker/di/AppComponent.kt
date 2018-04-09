package cz.ojohn.locationtracker.di

import android.content.Context
import cz.ojohn.locationtracker.App
import cz.ojohn.locationtracker.location.FetchLocationService
import cz.ojohn.locationtracker.location.TrackingService
import cz.ojohn.locationtracker.screen.find.FindDeviceFragment
import cz.ojohn.locationtracker.screen.find.FindDeviceService
import cz.ojohn.locationtracker.screen.sms.SmsFragment
import cz.ojohn.locationtracker.screen.tracking.TrackingFragment
import cz.ojohn.locationtracker.sms.SmsReceiver
import cz.ojohn.locationtracker.util.BootReceiver
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

/**
 * Dagger Application component
 */
@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun inject(app: App)
    fun inject(bootReceiver: BootReceiver)
    fun inject(smsReceiver: SmsReceiver)

    fun inject(trackingFragment: TrackingFragment)
    fun inject(smsFragment: SmsFragment)
    fun inject(findDeviceFragment: FindDeviceFragment)

    fun inject(trackingService: TrackingService)
    fun inject(fetchLocationService: FetchLocationService)
    fun inject(findDeviceService: FindDeviceService)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun applicationContext(context: Context): Builder

        fun build(): AppComponent
    }
}
