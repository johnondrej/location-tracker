package cz.ojohn.locationtracker.data

import cz.ojohn.locationtracker.Constants

/**
 * Class representing location tracking radius settings
 */
data class TrackingRadius(val value: Int, val selectedUnit: String) {

    val inMeters: Int
        get() {
            return when (selectedUnit) {
                Constants.UNIT_METERS -> value
                Constants.UNIT_KILOMETERS -> value * 1000
                else -> throw IllegalArgumentException("Invalid tracking radius unit")
            }
        }
}
