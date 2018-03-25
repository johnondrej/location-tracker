package cz.ojohn.locationtracker.data

import cz.ojohn.locationtracker.Constants

/**
 * Class representing location tracking frequency setting
 */
data class TrackingFrequency(val value: Int, val selectedUnit: String) {

    val inMinutes: Int
        get() {
            return when (selectedUnit) {
                Constants.UNIT_MINUTES -> value
                Constants.UNIT_HOURS -> value * 60
                else -> throw IllegalArgumentException("Invalid tracking frequency unit")
            }
        }
}
