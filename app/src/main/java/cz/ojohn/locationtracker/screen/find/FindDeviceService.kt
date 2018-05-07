package cz.ojohn.locationtracker.screen.find

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import cz.ojohn.locationtracker.App
import cz.ojohn.locationtracker.location.DeviceFinder
import cz.ojohn.locationtracker.sms.SmsController
import cz.ojohn.locationtracker.util.NotificationController
import cz.ojohn.locationtracker.util.powerManager
import cz.ojohn.locationtracker.util.safeRelease
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Service that gets other device location from incoming SMS
 */
class FindDeviceService : Service() {

    companion object {
        private const val TAG_WAKELOCK = "finding_wl"
        private const val TAG_PHONE = "phone"
        private const val TAG_PASSWORD = "sms_pass"
        private const val TIMEOUT_SECONDS: Long = 240

        fun getIntent(context: Context, phone: String, smsPassword: String): Intent {
            return Intent(context, FindDeviceService::class.java).apply {
                putExtra(TAG_PHONE, phone)
                putExtra(TAG_PASSWORD, smsPassword)
            }
        }
    }

    private var isFound: Boolean = false
    private var wakeLock: PowerManager.WakeLock? = null

    private lateinit var disposables: CompositeDisposable

    @Inject
    lateinit var smsController: SmsController
    @Inject
    lateinit var deviceFinder: DeviceFinder
    @Inject
    lateinit var notificationController: NotificationController

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        App.instance.appComponent.inject(this)
        startForeground(NotificationController.FINDING_NOTIFICATON_ID,
                notificationController.getDeviceFindingNotification())
        disposables = CompositeDisposable()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        wakeLock?.safeRelease()
        if (intent != null) {
            wakeLock = applicationContext.powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG_WAKELOCK).apply {
                acquire(TIMEOUT_SECONDS * 1000)
            }
            deviceFinder.onFindingStarted(intent.getStringExtra(TAG_PHONE))
            disposables.add(smsController.observeSmsActions()
                    .filter { action -> action is SmsController.SmsAction.GpsReceived }
                    .map { action -> action as SmsController.SmsAction.GpsReceived }
                    .subscribe { onGpsLocationReceived(it) })
            disposables.add(Completable.timer(TIMEOUT_SECONDS, TimeUnit.SECONDS, Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { onTimeoutPassed() })
            smsController.sendFindFromAppRequest(intent.getStringExtra(TAG_PHONE), intent.getStringExtra(TAG_PASSWORD))
            return START_REDELIVER_INTENT
        } else {
            return START_NOT_STICKY
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isFound) {
            deviceFinder.onFindingCancelled()
        }
        disposables.clear()
        wakeLock?.safeRelease()
    }

    private fun onGpsLocationReceived(gpsAction: SmsController.SmsAction.GpsReceived) {
        if (deviceFinder.targetPhone == gpsAction.phone) {
            isFound = true
            deviceFinder.onDeviceFound(gpsAction.phone, gpsAction.location)
            stopSelf()
        }
    }

    private fun onTimeoutPassed() {
        isFound = false
        stopSelf()
    }
}
