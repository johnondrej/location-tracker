package cz.ojohn.locationtracker.screen.tracking

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.ojohn.locationtracker.R

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
}
