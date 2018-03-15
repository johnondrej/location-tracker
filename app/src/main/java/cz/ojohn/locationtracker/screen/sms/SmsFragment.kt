package cz.ojohn.locationtracker.screen.sms

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.ojohn.locationtracker.R

/**
 * SMS configuration screen fragment
 */
class SmsFragment : Fragment() {

    companion object {
        fun newInstance(): SmsFragment = SmsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_sms, container, false)
    }
}
