package cz.ojohn.locationtracker.screen.tracking

import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import cz.ojohn.locationtracker.App
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.data.TrackingFrequency
import cz.ojohn.locationtracker.data.TrackingRadius
import cz.ojohn.locationtracker.location.LocationTracker
import cz.ojohn.locationtracker.util.areAllPermissionsGranted
import cz.ojohn.locationtracker.util.isPermissionGranted
import cz.ojohn.locationtracker.util.showSnackbar
import cz.ojohn.locationtracker.view.ScrollMapFragment
import cz.ojohn.locationtracker.viewmodel.ViewModelFactory
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_tracking.*
import javax.inject.Inject

/**
 * Tracking screen fragment
 */
class TrackingFragment : Fragment() {

    companion object {
        fun newInstance(): TrackingFragment = TrackingFragment()

        const val REQUEST_INITIAL = 1
        const val REQUEST_ENABLE_TRACKING = 2
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var frequencySpinnerAdapter: ArrayAdapter<CharSequence>
    private lateinit var radiusSpinnerAdapter: ArrayAdapter<CharSequence>
    private lateinit var viewModel: TrackingViewModel
    private lateinit var disposables: CompositeDisposable
    private lateinit var trackingStatus: LocationTracker.TrackingStatus

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        App.instance.appComponent.inject(this)
        disposables = CompositeDisposable()
        trackingStatus = LocationTracker.TrackingStatus.DISABLED
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(TrackingViewModel::class.java)
        return inflater.inflate(R.layout.fragment_tracking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.fragmentMap) as ScrollMapFragment
        mapFragment.touchListener = { scrollView.requestDisallowInterceptTouchEvent(true) }
        frequencySpinnerAdapter = ArrayAdapter.createFromResource(context,
                R.array.units_time, android.R.layout.simple_spinner_item).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        radiusSpinnerAdapter = ArrayAdapter.createFromResource(context,
                R.array.units_distance, android.R.layout.simple_spinner_item).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerFrequency.adapter = frequencySpinnerAdapter
        spinnerRadius.adapter = radiusSpinnerAdapter
        btnStartTracking.setOnClickListener { onStartTrackingButtonSelected() }
        checkTrackConstantly.setOnCheckedChangeListener { _, isChecked ->
            editFrequency.isEnabled = !isChecked
            spinnerFrequency.isEnabled = !isChecked
        }

        initForm()

        if (savedInstanceState == null) {
            onEnableMapLocation()
        }
    }

    override fun onResume() {
        super.onResume()
        disposables.add(viewModel.observeTrackingStatus()
                .subscribe { onTrackingStatusChanged(it) })
        disposables.add(viewModel.observeFormState()
                .subscribe { onFormStateChanged(it) })
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModel.askingForPermissions = false

        when (requestCode) {
            REQUEST_INITIAL -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.onEnableMapLocation()
                } else {
                    showSnackbar(R.string.tracking_error_permissions_denied, Snackbar.LENGTH_LONG)
                }
            }
            REQUEST_ENABLE_TRACKING -> {
                if (grantResults.areAllPermissionsGranted()) {
                    viewModel.onEnableTracking()
                } else {
                    showSnackbar(R.string.tracking_error_permissions_denied, Snackbar.LENGTH_LONG)
                }
            }
        }
    }

    private fun initForm() {
        val trackingSettings = viewModel.getTrackingSettings()

        editFrequency.setText(trackingSettings.frequency.value.toString())
        spinnerFrequency.setSelection(frequencySpinnerAdapter.getPosition(trackingSettings.frequency.selectedUnit))
        editRadius.setText(trackingSettings.radius.value.toString())
        spinnerRadius.setSelection(radiusSpinnerAdapter.getPosition(trackingSettings.radius.selectedUnit))
        editPhone.setText(trackingSettings.phone)
        checkTrackConstantly.isChecked = trackingSettings.trackConstantly
        checkFalseAlarms.isChecked = trackingSettings.reduceFalseAlarms
        checkBatteryNotify.isChecked = trackingSettings.lowBatteryNotify
        checkBatteryAutoOff.isChecked = trackingSettings.lowBatteryTurnOff
        checkChargerDetect.isChecked = trackingSettings.chargerNotify
    }

    private fun setFormEnabled(enabled: Boolean) {
        val frequencyEnabled = enabled && !checkTrackConstantly.isChecked
        editFrequency.isEnabled = frequencyEnabled
        spinnerFrequency.isEnabled = frequencyEnabled
        editRadius.isEnabled = enabled
        spinnerRadius.isEnabled = enabled
        editPhone.isEnabled = enabled
        checkTrackConstantly.isEnabled = enabled
        checkFalseAlarms.isEnabled = enabled
        checkBatteryNotify.isEnabled = enabled
        checkBatteryAutoOff.isEnabled = enabled
        checkChargerDetect.isEnabled = enabled
    }

    private fun onTrackingStatusChanged(status: LocationTracker.TrackingStatus) {
        trackingStatus = status
        val buttonText = when (status) {
            LocationTracker.TrackingStatus.DISABLED -> requireContext().getString(R.string.tracking_start)
            else -> requireContext().getString(R.string.tracking_stop)
        }
        btnStartTracking.text = buttonText

        if (status == LocationTracker.TrackingStatus.NOT_AVAILABLE) {
            showSnackbar(R.string.tracking_error_not_available, Snackbar.LENGTH_LONG)
        }
        setFormEnabled(status == LocationTracker.TrackingStatus.DISABLED)
    }

    private fun onStartTrackingButtonSelected() {
        when (trackingStatus) {
            LocationTracker.TrackingStatus.DISABLED -> {
                val frequency = if (editFrequency.text.isNotEmpty()) editFrequency.text.toString().toInt() else 0
                val radius = if (editRadius.text.isNotEmpty()) editRadius.text.toString().toInt() else 0

                viewModel.onCheckFormValues(LocationTracker.Settings(
                        TrackingFrequency(frequency, spinnerFrequency.selectedItem as String),
                        TrackingRadius(radius, spinnerRadius.selectedItem as String),
                        editPhone.text.toString(),
                        checkTrackConstantly.isChecked,
                        checkFalseAlarms.isChecked,
                        checkBatteryNotify.isChecked,
                        checkBatteryAutoOff.isChecked,
                        checkChargerDetect.isChecked
                ))
            }
            else -> viewModel.onDisableTracking()
        }
    }

    private fun onFormStateChanged(formState: TrackingViewModel.FormState) {
        when (formState) {
            is TrackingViewModel.FormState.Valid -> onEnableTracking()
            is TrackingViewModel.FormState.Error -> {
                val message = requireContext().getString(formState.messageRes, *formState.params)
                showSnackbar(message, Snackbar.LENGTH_LONG)
            }
        }
    }

    private fun onEnableTracking() {
        val ctx = requireContext()
        if (ctx.isPermissionGranted(android.Manifest.permission.ACCESS_FINE_LOCATION)
                && ctx.isPermissionGranted(android.Manifest.permission.SEND_SMS)) {
            viewModel.onEnableTracking()
        } else {
            askForPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.SEND_SMS), REQUEST_ENABLE_TRACKING)
        }
    }

    private fun onEnableMapLocation() {
        val ctx = requireContext()
        if (ctx.isPermissionGranted(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            viewModel.onEnableMapLocation()
        } else {
            askForPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_INITIAL)
        }
    }

    private fun askForPermissions(permissions: Array<String>, requestCode: Int) {
        if (!viewModel.askingForPermissions) {
            viewModel.askingForPermissions = true
            requestPermissions(permissions, requestCode)
        }
    }
}
