package cz.ojohn.locationtracker.screen.help

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.ojohn.locationtracker.R

/**
 * Fragment for displaying help info related to currently selected screen
 */
class HelpFragment : Fragment() {

    companion object {
        fun newInstance(): HelpFragment = HelpFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_help, container, false)
    }
}
