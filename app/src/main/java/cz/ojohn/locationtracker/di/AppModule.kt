package cz.ojohn.locationtracker.di

import android.content.Context
import android.content.SharedPreferences
import cz.ojohn.locationtracker.Constants
import cz.ojohn.locationtracker.data.UserPreferences
import cz.ojohn.locationtracker.location.LocationController
import cz.ojohn.locationtracker.location.LocationTracker
import cz.ojohn.locationtracker.sms.SmsController
import cz.ojohn.locationtracker.util.NotificationController
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Dagger Application module
 */
@Module(includes = [ViewModelsModule::class])
class AppModule {

    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideUserPreferences(sharedPreferences: SharedPreferences): UserPreferences {
        return UserPreferences(sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideNotificationController(context: Context): NotificationController {
        return NotificationController(context)
    }

    @Provides
    @Singleton
    fun provideLocationController(context: Context, sharedPreferences: SharedPreferences): LocationController {
        return LocationController(context, sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideSmsController(context: Context, userPreferences: UserPreferences): SmsController {
        return SmsController(context, userPreferences)
    }

    @Provides
    @Singleton
    fun provideLocationTracker(context: Context, userPreferences: UserPreferences,
                               locationController: LocationController): LocationTracker {
        return LocationTracker(context, userPreferences, locationController)
    }
}
