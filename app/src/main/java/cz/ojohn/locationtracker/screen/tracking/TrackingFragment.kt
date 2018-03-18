package cz.ojohn.locationtracker.screen.tracking

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.view.ScrollMapFragment
import kotlinx.android.synthetic.main.fragment_tracking.*

/**
 * Tracking screen fragment
 */
class TrackingFragment : Fragment() {

    companion object {
        fun newInstance(): TrackingFragment = TrackingFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_tracking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.fragmentMap) as ScrollMapFragment
        mapFragment.touchListener = { scrollView.requestDisallowInterceptTouchEvent(true) }
    }
}
