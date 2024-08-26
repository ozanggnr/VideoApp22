package com.example.ozan.videoapp22.Gestures

import android.view.GestureDetector
import android.view.MotionEvent

class SwipeGestureDetector(
    private val listener: OnSwipeListener
) : GestureDetector.SimpleOnGestureListener() {

    interface OnSwipeListener {
        fun onSwipeLeft()
        fun onSwipeRight()
    }

    private val SWIPE_THRESHOLD = 100
    private val SWIPE_VELOCITY_THRESHOLD = 100

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val diffX = e2.x - e1!!.x
        val diffY = e2.y - e1.y
        if (Math.abs(diffX) > Math.abs(diffY)) {
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    listener.onSwipeRight()
                } else {
                    listener.onSwipeLeft()
                }
                return true
            }
        }
        return false
    }
}
