package cz.ojohn.locationtracker.screen.help

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.screen.main.MainActivity.Companion.SCREEN_FIND
import cz.ojohn.locationtracker.screen.main.MainActivity.Companion.SCREEN_MENU
import cz.ojohn.locationtracker.screen.main.MainActivity.Companion.SCREEN_SMS
import cz.ojohn.locationtracker.screen.main.MainActivity.Companion.SCREEN_TRACKING
import kotlinx.android.synthetic.main.fragment_help.*

/**
 * Fragment for displaying help info related to currently selected screen
 */
class HelpFragment : Fragment() {

    companion object {
        private const val KEY_SCREEN = "screen_id"

        fun newInstance(screenId: Int): HelpFragment {
            return HelpFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_SCREEN, screenId)
                }
            }
        }
    }

    private val screenId: Int?
        get() = arguments?.getInt(KEY_SCREEN)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_help, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listHelp.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = HelpAdapter(getHelpItems())
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    private fun getHelpItems(): Array<HelpItem> {
        return when (screenId) {
            SCREEN_MENU -> getMenuHelpItems()
            SCREEN_TRACKING -> getTrackingHelpItems()
            SCREEN_FIND -> getFindHelpItems()
            SCREEN_SMS -> getSmsHelpItems()
            else -> getUnknownItems()
        }
    }

    private fun getMenuHelpItems(): Array<HelpItem> {
        return arrayOf(HelpItem(null, R.string.help_menu_intro),
                HelpItem(R.string.help_menu_tracking_caption, R.string.help_menu_tracking),
                HelpItem(R.string.help_menu_find_caption, R.string.help_menu_find),
                HelpItem(R.string.help_menu_sms_caption, R.string.help_menu_sms))
    }

    private fun getTrackingHelpItems(): Array<HelpItem> {
        return arrayOf(HelpItem(null, R.string.help_tracking_intro),
                HelpItem(R.string.help_tracking_map_caption, R.string.help_tracking_map),
                HelpItem(R.string.help_tracking_radius_caption, R.string.help_tracking_radius),
                HelpItem(R.string.help_tracking_phone_caption, R.string.help_tracking_phone),
                HelpItem(R.string.help_tracking_false_alarms_caption, R.string.help_tracking_false_alarms),
                HelpItem(R.string.help_tracking_notify_battery_caption, R.string.help_tracking_notify_battery),
                HelpItem(R.string.help_tracking_battery_auto_off_caption, R.string.help_tracking_battery_auto_off),
                HelpItem(R.string.help_tracking_charger_caption, R.string.help_tracking_charger)
        )
    }

    private fun getFindHelpItems(): Array<HelpItem> {
        return arrayOf(HelpItem(null, R.string.help_find_intro))
    }

    private fun getSmsHelpItems(): Array<HelpItem> {
        return arrayOf(HelpItem(R.string.help_sms_list_caption, R.string.help_sms_list),
                HelpItem(R.string.help_sms_example_caption, R.string.help_sms_example))
    }

    private fun getUnknownItems(): Array<HelpItem> {
        return arrayOf(HelpItem(null, R.string.help_unknown))
    }

}
