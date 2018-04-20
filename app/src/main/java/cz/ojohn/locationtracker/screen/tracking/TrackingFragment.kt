package cz.ojohn.locationtracker.screen.tracking

import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import cz.ojohn.locationtracker.App
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.data.TrackingFrequency
import cz.ojohn.locationtracker.data.TrackingRadius
import cz.ojohn.locationtracker.data.UserPreferences
import cz.ojohn.locationtracker.location.LocationTracker
import cz.ojohn.locationtracker.util.*
import cz.ojohn.locationtracker.view.ScrollMapFragment
import cz.ojohn.locationtracker.viewmodel.ViewModelFactory
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
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

    private var map: TrackingMap? = null
    private var mapStateObservable: Disposable? = null

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
        mapFragment.getMapAsync {
            it?.let {
                map = initMap(it)
                observeMapState()
                it.setOnMapLongClickListener { newPosition -> onTrackingPositionChange(newPosition) }
            }
        }

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
        spinnerRadius.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onTrackingRadiusChanged()
            }
        }
        btnStartTracking.setOnClickListener { onStartTrackingButtonSelected() }
        editRadius.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                onTrackingRadiusChanged()
            }
        })
        checkTrackConstantly.setOnCheckedChangeListener { _, isChecked ->
            editFrequency.isEnabled = !isChecked
            spinnerFrequency.isEnabled = !isChecked
        }

        initForm()
        if (savedInstanceState == null) {
            askForLocationPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        disposables.add(viewModel.observeTrackingStatus()
                .subscribe { onTrackingStatusChanged(it) })
        disposables.add(viewModel.observeFormState()
                .subscribe { onFormStateChanged(it) })
        disposables.add(viewModel.observeMapProperties().subscribe { onMapPropertiesChanged(it) })

        observeMapState()
        onEnableMapLocation()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onDisableMapLocation()
        unsubscribeObservables()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModel.askingForPermissions = false

        when (requestCode) {
            REQUEST_INITIAL -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
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

    private fun initMap(googleMap: GoogleMap): TrackingMap {
        val trackingSettings = viewModel.getTrackingSettings()
        val trackingPosition = LatLng(trackingSettings.latitude, trackingSettings.longitude)
        val locationMarker = googleMap.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_position))
                .anchor(0.5f, 0.5f)
                .visible(false)
                .position(LatLng(0.0, 0.0)))
        val trackingMarker = googleMap.addMarker(MarkerOptions()
                .position(trackingPosition))
        val trackingCircle = googleMap.addCircle(CircleOptions()
                .center(trackingPosition)
                .clickable(false)
                .fillColor(Color.argb(100, 251, 140, 0))
                .strokeColor(Color.argb(200, 251, 140, 0))
                .strokeWidth(4f)
                .radius(trackingSettings.radius.inMeters.toDouble()))
        return TrackingMap(googleMap, locationMarker, trackingMarker, trackingCircle)
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
                val latitude = map!!.trackingMarker.position.latitude
                val longitude = map!!.trackingMarker.position.longitude
                val frequency = if (editFrequency.text.isNotEmpty()) editFrequency.text.toString().toInt() else 0
                val radius = if (editRadius.text.isNotEmpty()) editRadius.text.toString().toInt() else 0

                viewModel.onCheckFormValues(LocationTracker.Settings(
                        latitude,
                        longitude,
                        TrackingFrequency(frequency, spinnerFrequency.selectedItem as String),
                        TrackingRadius(radius, spinnerRadius.selectedItem as String),
                        editPhone.text.toString(),
                        checkTrackConstantly.isChecked,
                        checkFalseAlarms.isChecked,
                        checkBatteryNotify.isChecked,
                        checkBatteryAutoOff.isChecked,
                        checkChargerDetect.isChecked
                ), requireContext().getBatteryPercentage(), requireContext().isBatteryChargingOrFull())
            }
            else -> viewModel.onDisableTracking()
        }
    }

    private fun onTrackingRadiusChanged() {
        val radiusInput = editRadius.text.toString()
        if (!radiusInput.isBlank()) {
            val radiusMeters = TrackingRadius(radiusInput.toInt(), spinnerRadius.selectedItem as String).inMeters
            val mapRadius = when {
                radiusMeters < UserPreferences.TRACKING_MIN_RADIUS -> UserPreferences.TRACKING_MIN_RADIUS
                radiusMeters > UserPreferences.TRACKING_MAX_RADIUS -> UserPreferences.TRACKING_MAX_RADIUS
                else -> radiusMeters
            }
            viewModel.onTrackingRadiusChange(mapRadius)
        }
    }

    private fun onTrackingPositionChange(newPosition: LatLng) {
        viewModel.onTrackingPositionChange(newPosition)
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

    private fun onMapStateChanged(mapState: TrackingViewModel.MapState) {
        val coordinates = LatLng(mapState.locationEntry.lat, mapState.locationEntry.lon)
        if (mapState is TrackingViewModel.MapState.WithoutPosition) {
            map?.googleMap?.let {
                viewModel.preserveMarkerPos = true
                map!!.locationMarker.isVisible = true
                it.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(coordinates, 14f)))
                if (mapState.adjustTracking) {
                    viewModel.onTrackingPositionChange(coordinates)
                }
            }
        }
        viewModel.onUserLocationChange(coordinates)
    }

    private fun onMapPropertiesChanged(properties: TrackingViewModel.MapProperties) {
        map?.let {
            it.locationMarker.position = properties.userLocation
            it.trackingMarker.position = properties.trackingPosition
            it.trackingCircle.center = properties.trackingPosition
            it.trackingCircle.radius = properties.trackingRadius.toDouble()
        }
    }

    private fun onEnableTracking() {
        val ctx = requireContext()
        if (ctx.isPermissionGranted(android.Manifest.permission.ACCESS_FINE_LOCATION)
                && ctx.isPermissionGranted(android.Manifest.permission.RECEIVE_SMS)
                && ctx.isPermissionGranted(android.Manifest.permission.READ_SMS)
                && ctx.isPermissionGranted(android.Manifest.permission.SEND_SMS)) {
            viewModel.onEnableTracking()
        } else {
            askForPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.RECEIVE_SMS,
                    android.Manifest.permission.READ_SMS,
                    android.Manifest.permission.SEND_SMS), REQUEST_ENABLE_TRACKING)
        }
    }

    private fun onEnableMapLocation() {
        if (requireContext().isPermissionGranted(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            viewModel.onEnableMapLocation()
        }
    }

    private fun askForLocationPermission() {
        if (!requireContext().isPermissionGranted(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            askForPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_INITIAL)
        }
    }

    private fun askForPermissions(permissions: Array<String>, requestCode: Int) {
        if (!viewModel.askingForPermissions) {
            viewModel.askingForPermissions = true
            requestPermissions(permissions, requestCode)
        }
    }

    private fun observeMapState() {
        if ((mapStateObservable == null || mapStateObservable?.isDisposed == true) && map != null) {
            mapStateObservable = viewModel.observeMapState()
                    .subscribe { onMapStateChanged(it) }
        }
    }

    private fun unsubscribeObservables() {
        mapStateObservable?.dispose()
        disposables.clear()
    }

    private class TrackingMap(val googleMap: GoogleMap,
                              val locationMarker: Marker,
                              val trackingMarker: Marker,
                              val trackingCircle: Circle)
}
