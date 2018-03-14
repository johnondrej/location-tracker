package cz.ojohn.locationtracker.screen.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.ojohn.locationtracker.R

/**
 * Main screen fragment
 */
class MainFragment : Fragment() {

    companion object {
        fun newInstance(): MainFragment = MainFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_main, container, false)
    }
}
