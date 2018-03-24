package cz.ojohn.locationtracker.screen.tracking

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.ojohn.locationtracker.App
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.location.LocationTracker
import cz.ojohn.locationtracker.location.TrackingService
import cz.ojohn.locationtracker.util.areAllPermissionsGranted
import cz.ojohn.locationtracker.util.isPermissionGranted
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

        const val REQUEST_ENABLE_TRACKING = 1
    }

    private val trackingServiceIntent: Intent
        get() = Intent(context, TrackingService::class.java)

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

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
        btnStartTracking.setOnClickListener { onStartTrackingButtonSelected() }
    }

    override fun onResume() {
        super.onResume()
        disposables.add(viewModel.observeTrackingStatus()
                .subscribe { onTrackingStatusChanged(it) })
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ENABLE_TRACKING -> {
                if (grantResults.areAllPermissionsGranted()) {
                    onTrackingEnabled()
                } else {
                    view?.let {
                        Snackbar.make(it, R.string.tracking_permissions_denied, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun onTrackingStatusChanged(status: LocationTracker.TrackingStatus) {
        trackingStatus = status
        val buttonText = when (status) {
            LocationTracker.TrackingStatus.DISABLED -> requireContext().getString(R.string.tracking_start)
            else -> requireContext().getString(R.string.tracking_stop)
        }
        btnStartTracking.text = buttonText

        if (status == LocationTracker.TrackingStatus.NOT_AVAILABLE) {
            view?.let {
                Snackbar.make(it, R.string.tracking_not_available, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun onStartTrackingButtonSelected() {
        when (trackingStatus) {
            LocationTracker.TrackingStatus.DISABLED -> onEnableTracking()
            else -> onDisableTracking()
        }
    }

    private fun onEnableTracking() {
        val ctx = requireContext()
        if (ctx.isPermissionGranted(android.Manifest.permission.ACCESS_FINE_LOCATION)
                && ctx.isPermissionGranted(android.Manifest.permission.SEND_SMS)) {
            onTrackingEnabled()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.SEND_SMS), REQUEST_ENABLE_TRACKING)
        }
    }

    private fun onDisableTracking() {
        requireContext().stopService(trackingServiceIntent)
    }

    private fun onTrackingEnabled() {
        requireContext().startService(trackingServiceIntent)
    }
}
