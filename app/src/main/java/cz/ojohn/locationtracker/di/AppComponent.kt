package cz.ojohn.locationtracker.di

import android.content.Context
import cz.ojohn.locationtracker.App
import cz.ojohn.locationtracker.location.TrackingService
import cz.ojohn.locationtracker.screen.tracking.TrackingFragment
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
    fun inject(trackingFragment: TrackingFragment)
    fun inject(trackingService: TrackingService)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun applicationContext(context: Context): Builder

        fun build(): AppComponent
    }
}
