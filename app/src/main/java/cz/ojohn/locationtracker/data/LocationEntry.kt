package cz.ojohn.locationtracker.data

/**
 * Model class for info about observed location
 */
data class LocationEntry(val lat: Double,
                         val lon: Double,
                         val alt: Double?,
                         val accuracy: Float?,
                         val time: Long,
                         val source: String? = null)
