package cz.ojohn.locationtracker.screen.find

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.ojohn.locationtracker.R

/**
 * "Find other device" screen fragments
 */
class FindDeviceFragment : Fragment() {

    companion object {
        fun newInstance(): FindDeviceFragment = FindDeviceFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_find, container, false)
    }
}
