package cz.ojohn.locationtracker.util

import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment

/**
 * Extension functions for Fragments
 */
fun Fragment.showSnackbar(message: String, duration: Int) {
    view?.let {
        Snackbar.make(it, message, duration).show()
    }
}

fun Fragment.showSnackbar(messageRes: Int, duration: Int) {
    view?.let {
        Snackbar.make(it, messageRes, duration).show()
    }
}
