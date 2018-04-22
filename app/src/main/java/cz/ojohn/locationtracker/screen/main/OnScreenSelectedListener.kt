package cz.ojohn.locationtracker.screen.main

/**
 * Interface for screen selection callback
 */
interface OnScreenSelectedListener {
    fun onScreenSelected(screenId: Int, changeSelection: Boolean): Boolean
}
