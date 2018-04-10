package cz.ojohn.locationtracker.screen.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.util.isPermissionGranted
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * Main screen fragment
 */
class MainFragment : Fragment() {

    companion object {
        private const val REQUEST_LOCATION = 1
        private const val REQUEST_SMS = 2

        fun newInstance(): MainFragment = MainFragment()
    }

    private lateinit var menuAdapter: MenuAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        menuAdapter = MenuAdapter(requireContext().applicationContext, getPermissionsNotices(), getControls())
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listMenu.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = menuAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        menuAdapter.updateMissingPermissions(getPermissionsNotices())
    }

    private fun getPermissionsNotices(): List<MenuAdapter.PermissionNotice> {
        val context = requireContext()
        val locationNotice = if (context.isPermissionGranted(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            null
        } else {
            MenuAdapter.PermissionNotice(R.drawable.ic_status_warning, R.string.menu_notice_permission_location,
                    R.string.menu_notice_permission_grant, { requestLocationPermission() })
        }
        val smsNotice = if (context.isPermissionGranted(android.Manifest.permission.RECEIVE_SMS)
                && context.isPermissionGranted(android.Manifest.permission.READ_SMS)
                && context.isPermissionGranted(android.Manifest.permission.SEND_SMS)) {
            null
        } else {
            MenuAdapter.PermissionNotice(R.drawable.ic_status_warning, R.string.menu_notice_permission_sms,
                    R.string.menu_notice_permission_grant, { requestSmsPermission() })
        }

        return listOfNotNull(locationNotice, smsNotice)
    }

    private fun getControls(): List<MenuAdapter.ControlItem> {
        val trackingControl = MenuAdapter.ControlItem(R.drawable.ic_control_tracking, R.string.screen_tracking,
                R.string.menu_status_available, R.color.colorStatusOk, R.drawable.ic_status_ok,
                { onTrackingScreenSelected() })
        val findDeviceControl = MenuAdapter.ControlItem(R.drawable.ic_control_find, R.string.screen_find,
                R.string.menu_status_available, R.color.colorStatusOk, R.drawable.ic_status_ok,
                { onFindDeviceScreenSelected() })
        val smsControl = MenuAdapter.ControlItem(R.drawable.ic_control_sms, R.string.screen_sms,
                R.string.menu_sms_description, R.color.colorPrimaryDark, R.drawable.ic_settings,
                { onSmsScreenSelected() })

        return listOf(trackingControl, findDeviceControl, smsControl)
    }

    private fun requestLocationPermission() {
        requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
    }

    private fun requestSmsPermission() {
        requestPermissions(arrayOf(android.Manifest.permission.RECEIVE_SMS,
                android.Manifest.permission.READ_SMS,
                android.Manifest.permission.SEND_SMS), REQUEST_SMS)
    }

    private fun onTrackingScreenSelected() {
        val activity = requireActivity() as OnScreenSelectedListener
        activity.onScreenSelected(R.id.action_screen_tracking, true)
    }

    private fun onFindDeviceScreenSelected() {
        val activity = requireActivity() as OnScreenSelectedListener
        activity.onScreenSelected(R.id.action_screen_find, true)
    }

    private fun onSmsScreenSelected() {
        val activity = requireActivity() as OnScreenSelectedListener
        activity.onScreenSelected(R.id.action_screen_sms, true)
    }

    interface OnScreenSelectedListener {
        fun onScreenSelected(screenId: Int, changeSelection: Boolean)
    }
}
