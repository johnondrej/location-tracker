package cz.ojohn.locationtracker.screen.about

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import cz.ojohn.locationtracker.R
import kotlinx.android.synthetic.main.fragment_about_app.*

/**
 * FragmentDialog with info about this application
 */
class AboutFragment : DialogFragment() {

    companion object {
        fun newInstance(): AboutFragment {
            return AboutFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about_app, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window.requestFeature(Window.FEATURE_NO_TITLE)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let {
            txtVersion.text = it.getString(R.string.about_app_version,
                    it.packageManager.getPackageInfo(it.packageName, 0).versionName)
            txtIcon.movementMethod = LinkMovementMethod.getInstance()
            txtDescription.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}
