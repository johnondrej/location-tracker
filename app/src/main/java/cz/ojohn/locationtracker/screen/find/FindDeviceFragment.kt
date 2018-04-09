package cz.ojohn.locationtracker.screen.find

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import cz.ojohn.locationtracker.App
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.location.DeviceFinder
import cz.ojohn.locationtracker.util.showSnackbar
import cz.ojohn.locationtracker.viewmodel.ViewModelFactory
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_find.*
import javax.inject.Inject

/**
 * "Find other device" screen fragments
 */
class FindDeviceFragment : Fragment() {

    companion object {
        fun newInstance(): FindDeviceFragment = FindDeviceFragment()
    }

    private var map: FindingMap? = null
    private var lastStatus: DeviceFinder.FindingStatus? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: FindDeviceViewModel
    private lateinit var disposables: CompositeDisposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        App.instance.appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FindDeviceViewModel::class.java)
        disposables = CompositeDisposable()
        return inflater.inflate(R.layout.fragment_find, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync {
            it?.let {
                map = initMap(it)
            }
        }
        btnFind.setOnClickListener { onFindingButtonSelected() }
    }

    override fun onResume() {
        super.onResume()
        disposables.add(viewModel.observeFindingState()
                .subscribe { onFindingStatusChanged(it) })
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

    private fun onFindingButtonSelected() {
        val isDataCorrect = viewModel.onStartFinding(editPhone.text.toString())
        if (!isDataCorrect) {
            showSnackbar(R.string.tracking_error_phone, Snackbar.LENGTH_LONG)
        }
    }

    private fun onFindingStatusChanged(status: DeviceFinder.FindingStatus) {
        val context = requireContext()
        when (status) {
            is DeviceFinder.FindingStatus.Initial -> {
                btnFind.text = context.getText(R.string.find_btn_start)
                if (lastStatus is DeviceFinder.FindingStatus.Initial) {
                    onDeviceNotFound()
                }
            }
            is DeviceFinder.FindingStatus.Finding -> {
                btnFind.text = context.getText(R.string.find_btn_stop)
            }
            is DeviceFinder.FindingStatus.Found -> {
                btnFind.text = context.getText(R.string.find_btn_start)
                onDeviceFound(status)
            }
        }
        lastStatus = status
    }

    private fun onDeviceFound(deviceStatus: DeviceFinder.FindingStatus.Found) {
        map?.let {
            val coordinates = LatLng(deviceStatus.location.lat, deviceStatus.location.lon)
            it.deviceMarker.apply {
                isVisible = true
                position = coordinates
            }
            it.googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(coordinates, 13f)))
        }
        showSnackbar(R.string.find_device_found, Snackbar.LENGTH_LONG)
    }

    private fun onDeviceNotFound() {
        showSnackbar(R.string.find_device_not_found, Snackbar.LENGTH_LONG)
    }

    private fun initMap(googleMap: GoogleMap): FindingMap {
        val marker = googleMap.addMarker(MarkerOptions()
                .position(LatLng(0.0, 0.0))
                .visible(false))
        return FindingMap(googleMap, marker)
    }

    private class FindingMap(val googleMap: GoogleMap, val deviceMarker: Marker)
}
