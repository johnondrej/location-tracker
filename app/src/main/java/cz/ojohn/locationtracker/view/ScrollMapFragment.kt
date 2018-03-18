package cz.ojohn.locationtracker.view

import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.gms.maps.SupportMapFragment

/**
 * Google SupportMapFragment for use in ScrollView
 */
class ScrollMapFragment : SupportMapFragment() {

    var touchListener: (() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = super.onCreateView(inflater, container, savedInstanceState) as? ViewGroup
        activity?.let {
            val touchInterceptor = TouchInterceptor(it).apply {
                setBackgroundColor(ContextCompat.getColor(it, android.R.color.transparent))
            }
            layout?.addView(touchInterceptor,
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
        return layout
    }

    inner class TouchInterceptor(context: Context) : FrameLayout(context) {

        override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
            when (ev.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> touchListener?.invoke()
            }
            return super.dispatchTouchEvent(ev)
        }
    }
}
