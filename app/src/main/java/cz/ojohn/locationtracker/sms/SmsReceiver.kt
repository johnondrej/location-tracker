package cz.ojohn.locationtracker.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import cz.ojohn.locationtracker.App
import cz.ojohn.locationtracker.location.FetchLocationService
import cz.ojohn.locationtracker.util.startForegroundServiceCompat
import javax.inject.Inject

/**
 * Broadcast receiver for listening to incoming SMS
 */
class SmsReceiver : BroadcastReceiver() {

    companion object {
        private const val KEY_PDUS = "pdus"
        private const val KEY_FORMAT = "format"
    }

    @Inject
    lateinit var appContext: Context
    @Inject
    lateinit var smsController: SmsController

    init {
        App.instance.appComponent.inject(this)
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val extras = intent?.extras
        if (extras != null) {
            var sender: String? = null
            val smsArray = extras.get(KEY_PDUS) as Array<*>
            val format = extras.get(KEY_FORMAT) as? String
            val output = StringBuilder()

            for (smsData in smsArray) {
                val sms = createFromPdu(smsData as ByteArray, format)
                output.append(sms.messageBody)
                sender = sms.originatingAddress
            }

            if (sender != null) {
                val action = smsController.processIncomingSms(sender, output.toString())
                if (action !is SmsController.SmsAction.None) {
                    onActionRequired(action)
                    abortBroadcast()
                }
            }
        }
    }

    private fun onActionRequired(action: SmsController.SmsAction) {
        when (action) {
            is SmsController.SmsAction.SendLocation -> {
                appContext.startForegroundServiceCompat(FetchLocationService.getIntent(appContext, action.phone))
            }
        }
    }

    private fun createFromPdu(smsData: ByteArray, format: String?): SmsMessage {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SmsMessage.createFromPdu(smsData, format)
        } else {
            SmsMessage.createFromPdu(smsData)
        }
    }
}
