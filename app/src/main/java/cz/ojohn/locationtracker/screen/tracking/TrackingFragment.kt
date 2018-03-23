package cz.ojohn.locationtracker.screen.tracking

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.ojohn.locationtracker.App
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.location.LocationTracker
import cz.ojohn.locationtracker.location.TrackingService
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
    }

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

    private fun onTrackingStatusChanged(status: LocationTracker.TrackingStatus) {
        trackingStatus = status
        val buttonText = when (status) {
            LocationTracker.TrackingStatus.DISABLED -> requireContext().getString(R.string.tracking_start)
            else -> requireContext().getString(R.string.tracking_stop)
        }
        btnStartTracking.text = buttonText
    }

    private fun onStartTrackingButtonSelected() {
        val serviceIntent = Intent(context, TrackingService::class.java)
        when (trackingStatus) {
            LocationTracker.TrackingStatus.DISABLED -> requireContext().startService(serviceIntent)
            else -> requireContext().stopService(serviceIntent)
        }
    }
}
