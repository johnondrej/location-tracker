package cz.ojohn.locationtracker.screen.sms

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import cz.ojohn.locationtracker.App
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.data.UserPreferences
import cz.ojohn.locationtracker.viewmodel.ViewModelFactory
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_sms.*
import kotlinx.android.synthetic.main.item_sms_command.view.*
import javax.inject.Inject

/**
 * SMS configuration screen fragment
 */
class SmsFragment : Fragment() {

    companion object {
        fun newInstance(): SmsFragment = SmsFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: SmsViewModel
    private lateinit var disposables: CompositeDisposable
    private lateinit var smsSettingsListener: CompoundButton.OnCheckedChangeListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        App.instance.appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SmsViewModel::class.java)
        disposables = CompositeDisposable()
        smsSettingsListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            viewModel.onMessageSettingsEntryChanged(buttonView.tag as String, isChecked)
        }
        return inflater.inflate(R.layout.fragment_sms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showCommandsDescription()
        initCheckboxes()
    }

    override fun onResume() {
        super.onResume()
        txtSmsPassword.text = requireContext().getString(R.string.settings_sms_password_format, viewModel.getSmsPassword())

        disposables.add(viewModel.observeExampleMessage()
                .subscribe { txtExample.text = it })
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

    private fun showCommandsDescription() {
        val ctx = requireContext()
        val commands = viewModel.getSmsCommandsList()

        layoutCommands.addView(LayoutInflater.from(ctx).inflate(R.layout.item_sms_command_divider, layoutCommands, false))
        commands.forEach {
            val layout = LayoutInflater.from(ctx).inflate(R.layout.item_sms_command, layoutCommands, false)
            layout.txtCommandName.text = viewModel.formatCommand(it)
            layout.txtCommandDescription.text = ctx.getString(it.descriptionRes)
            layoutCommands.addView(layout)
            layoutCommands.addView(LayoutInflater.from(ctx).inflate(R.layout.item_sms_command_divider, layoutCommands, false))
        }
    }

    private fun initCheckboxes() {
        val smsSettings = viewModel.getSmsSettings()
        val checkboxes = arrayOf(checkGpsCoords, checkName, checkTime, checkAccuracy,
                checkSource, checkBattery, checkWifi, checkIpAddress)

        checkGpsCoords.apply {
            tag = UserPreferences.KEY_SMS_GPS
            isChecked = smsSettings.sendGps
        }
        checkName.apply {
            tag = UserPreferences.KEY_SMS_LOC_NAME
            isChecked = smsSettings.sendLocationName
        }
        checkTime.apply {
            tag = UserPreferences.KEY_SMS_LOC_TIME
            isChecked = smsSettings.sendLocationTime
        }
        checkAccuracy.apply {
            tag = UserPreferences.KEY_SMS_LOC_ACCURACY
            isChecked = smsSettings.sendLocationAccuracy
        }
        checkSource.apply {
            tag = UserPreferences.KEY_SMS_LOC_SOURCE
            isChecked = smsSettings.sendLocationSource
        }
        checkBattery.apply {
            tag = UserPreferences.KEY_SMS_BATTERY
            isChecked = smsSettings.sendBattery
        }
        checkWifi.apply {
            tag = UserPreferences.KEY_SMS_WIFI
            isChecked = smsSettings.sendWiFi
        }
        checkIpAddress.apply {
            tag = UserPreferences.KEY_SMS_IP
            isChecked = smsSettings.sendIpAddress
        }
        checkboxes.forEach {
            it.setOnCheckedChangeListener(smsSettingsListener)
        }
    }
}
